package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexError
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.lensOrNull
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.replaceIndicesField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.notFound

class NextStepHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val taskMetadataForm = taskMetadataForm from request
        if (taskMetadataForm.errors.isNotEmpty()) {
            println(taskMetadataForm.errors)
            return render(request) draw HomePageVM(metadataForm = taskMetadataForm)
        }
        val matrixCoefficients = matrixField from taskMetadataForm
        val method = methodField from taskMetadataForm
        val n = matrixCoefficients.first().size
        val m = matrixCoefficients.size
        val defaultBasis = (0..<m).toList()
        val defaultFree = (m..<n - 1).toList()

        var renderedSteps = ""

        var syntheticSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexSteps: List<Triple<Int, Int, Int>> = emptyList()

        var simplexMethodTask: SimplexMethod? = null
        var syntheticBasisTask: SyntheticBasisMethod? = null
        var doSimplex = true
        val replaceIndices = replaceIndicesField from taskMetadataForm
        if (method == Method.SYNTHETIC_BASIS) {
            syntheticBasisTask = lensOrNull(syntheticBasisMethodField, (syntheticBasisMethodForm from request))
                ?: return notFound()

            val syntheticBasisResult = syntheticBasisTask.getSolution(1)
            var renderedSolution = ""
            when (syntheticBasisResult) {
                is Success -> {
                    val simplexFromRequest =
                        lensOrNull(simplexMethodField, (simplexMethodForm from request))
                    if (simplexFromRequest == null) {
                        simplexMethodTask = syntheticBasisTask.getSimplexMethodBySolution()!!
                        doSimplex = false
                    } else {
                        simplexMethodTask = simplexFromRequest
                        doSimplex = true
                    }
                    renderedSolution =
                        syntheticBasisTask.renderSolution(
                            method = method,
                            request = request,
                            render = render,
                        )
                }

                is Failure ->
                    when (syntheticBasisResult.reason) {
                        SimplexError.NOT_OPTIMAL_SOLUTION -> {
                            syntheticBasisTask.nextStep(replaceIndices).valueOrNull() ?: return notFound()
                            when (val resultAfterStep = syntheticBasisTask.getSolution(1)) {
                                is Success -> {
                                    renderedSolution =
                                        syntheticBasisTask.renderSolution(
                                            method = method,
                                            request = request,
                                            render = render,
                                        )
                                    val simplexFromRequest =
                                        lensOrNull(simplexMethodField, (simplexMethodForm from request))
                                    if (simplexFromRequest == null) {
                                        simplexMethodTask = syntheticBasisTask.getSimplexMethodBySolution()!!
                                        doSimplex = false
                                    } else {
                                        simplexMethodTask = simplexFromRequest
                                        doSimplex = true
                                    }
                                }

                                is Failure -> {
                                    when (resultAfterStep.reason) {
                                        SimplexError.NOT_OPTIMAL_SOLUTION -> {}
                                        else -> {
                                            renderedSolution =
                                                syntheticBasisTask.renderSolution(
                                                    method = method,
                                                    request = request,
                                                    render = render,
                                                )
                                            simplexMethodTask = null
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            renderedSolution =
                                syntheticBasisTask.renderSolution(
                                    method = method,
                                    request = request,
                                    render = render,
                                )
                            simplexMethodTask = null
                        }
                    }
            }

            val solution =
                syntheticBasisTask.renderSteps(
                    request = request,
                    forSyntheticBasis = true,
                    render = render,
                )
            renderedSteps = solution.first + renderedSolution
            syntheticSteps = solution.second
        } else {
            simplexMethodTask = lensOrNull(simplexMethodField, (simplexMethodForm from request)) ?: return notFound()
        }
        when {
            simplexMethodTask != null -> {
                var renderedSolution = ""
                when (val simplexResult = simplexMethodTask.getSolution()) {
                    is Success -> {
                        renderedSolution =
                            simplexMethodTask.renderSolution(
                                method = Method.SIMPLEX_METHOD,
                                request = request,
                                render = render,
                            )
                    }

                    is Failure ->
                        when (simplexResult.reason) {
                            SimplexError.NOT_OPTIMAL_SOLUTION -> {
                                if (doSimplex) {
                                    simplexMethodTask.nextStep(replaceIndices).valueOrNull() ?: return notFound()
                                    when (val resultAfterStep = simplexMethodTask.getSolution()) {
                                        is Success -> {
                                            renderedSolution =
                                                simplexMethodTask.renderSolution(
                                                    method = Method.SIMPLEX_METHOD,
                                                    request = request,
                                                    render = render,
                                                )
                                        }

                                        is Failure -> {
                                            when (resultAfterStep.reason) {
                                                SimplexError.NOT_OPTIMAL_SOLUTION -> {}
                                                else -> {
                                                    renderedSolution =
                                                        simplexMethodTask.renderSolution(
                                                            method = Method.SIMPLEX_METHOD,
                                                            request = request,
                                                            render = render,
                                                        )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                renderedSolution =
                                    simplexMethodTask.renderSolution(
                                        method = Method.SIMPLEX_METHOD,
                                        request = request,
                                        render = render,
                                    )
                            }
                        }
                }

                val solution =
                    simplexMethodTask.renderSteps(
                        request = request,
                        render = render,
                    )
                renderedSteps += solution.first + renderedSolution
                simplexSteps = solution.second
            }
        }
        return render(request) draw
            HomePageVM(
                metadataForm =
                    taskMetadataForm
                        .with(freeField of defaultFree)
                        .let {
                            if (method == Method.SYNTHETIC_BASIS) {
                                it.minus("basisJson").with(basisField of defaultBasis)
                            } else {
                                it
                            }
                        },
                simplexMethodForm = simplexMethodTask?.let { WebForm().with(simplexMethodField of it) },
                syntheticBasisForm = syntheticBasisTask?.let { WebForm().with(syntheticBasisMethodField of it) },
                renderedSteps = renderedSteps,
                syntheticSteps = syntheticSteps,
                simplexSteps = simplexSteps,
                nextStepForm =
                    simplexMethodTask?.renderPossibleReplaces(
                        render = render,
                        request = request,
                        method = Method.SIMPLEX_METHOD,
                        wasSyntheticBasis = syntheticBasisTask?.stepsReplaces?.isNotEmpty() ?: false,
                    ) ?: syntheticBasisTask?.renderPossibleReplaces(
                        render = render,
                        request = request,
                        method = Method.SYNTHETIC_BASIS,
                    ) ?: "",
            )
    }
}
