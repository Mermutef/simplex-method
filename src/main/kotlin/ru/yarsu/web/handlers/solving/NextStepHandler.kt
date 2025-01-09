package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexError
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.lensOrNull
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.replaceIndicesField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
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
        val basis = basisField from taskMetadataForm
        val matrixCoefficients = matrixField from taskMetadataForm
        val functionCoefficients = functionField from taskMetadataForm
        val taskType = taskTypeField from taskMetadataForm
        val method = methodField from taskMetadataForm
        val n = matrixCoefficients.first().size
        val m = matrixCoefficients.size
        val defaultBasis = (0..<m).toList()
        val defaultFree = (m..<n - 1).toList()
        val matrix =
            Matrix(
                m = m,
                n = n,
                coefficients = matrixCoefficients,
                basis = defaultBasis,
                free = defaultFree,
            )
        var renderedSteps = ""
        var syntheticSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexMethodTask: SimplexMethod? = null
        var syntheticBasisTask: SyntheticBasisMethod? = null
        var doSimplexStep = true
        val replaceIndices = replaceIndicesField from taskMetadataForm
        if (method == Method.SYNTHETIC_BASIS) {
            syntheticBasisTask =
                lensOrNull(syntheticBasisMethodField, (syntheticBasisMethodForm from request)) ?: return notFound()
            var renderedSolution = ""
            val syntheticBasisResult = syntheticBasisTask.getSolution(1)
            if (syntheticBasisResult is Failure) {
                if (syntheticBasisResult.reason == SimplexError.NOT_OPTIMAL_SOLUTION) {
                    val stepResult = syntheticBasisTask.nextStep(replaceIndices)
                    if (stepResult is Failure && stepResult.reason != SimplexError.INVALID_REPLACE) {
                        return notFound()
                    }
                    doSimplexStep = false
                } else {
                    renderedSolution = syntheticBasisTask.renderSolution(
                        method = method,
                        request = request,
                        render = render
                    )
                }
            } else {
                renderedSolution = syntheticBasisTask.renderSolution(
                    method = method,
                    request = request,
                    render = render
                )
                simplexMethodTask = syntheticBasisTask.getSimplexMethodBySolution()
                doSimplexStep = true
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
        if (doSimplexStep) {
            simplexMethodTask?.let {
                val simplexResult = it.getSolution()
                var renderedSolution = ""
                if (simplexResult is Failure) {
                    if (simplexResult.reason == SimplexError.NOT_OPTIMAL_SOLUTION) {
                        val stepResult = it.nextStep(replaceIndices)
                        if (stepResult is Failure && stepResult.reason != SimplexError.INVALID_REPLACE) {
                            return notFound()
                        }
                    } else {
                        renderedSolution = simplexMethodTask.renderSolution(
                            method = method,
                            request = request,
                            render = render
                        )
                    }
                } else {
                    renderedSolution = it.renderSolution(
                        render = render,
                        request = request,
                        method = Method.SIMPLEX_METHOD,
                    )
                }
                val solution = it.renderSteps(request = request, render = render)
                renderedSteps += solution.first + renderedSolution
                simplexSteps = solution.second
            }
        }

        return render(request) draw
                HomePageVM(
                    metadataForm =
                        taskMetadataForm
                            .with(freeField of defaultFree)
                            .let
                            {
                                if (method == Method.SYNTHETIC_BASIS) {
                                    it.minus("basisJson")
                                        .with(basisField of defaultBasis)
                                } else {
                                    it
                                }
                            },
                    simplexMethodForm =
                        simplexMethodTask?.let
                        {
                            WebForm().with(simplexMethodField of it)
                        },
                    syntheticBasisForm = syntheticBasisTask?.let
                    { WebForm().with(syntheticBasisMethodField of it) },
                    renderedSteps = renderedSteps,
                    syntheticSteps = syntheticSteps,
                    simplexSteps = simplexSteps,
                    nextStepForm = simplexMethodTask?.renderPossibleReplaces(
                        render = render,
                        request = request,
                        method = method
                    ) ?: ""
                )
    }
}
