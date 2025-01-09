package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexBase
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.modeField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.models.part.SimplexSolutionPT
import ru.yarsu.web.models.part.SimplexStepPT
import ru.yarsu.web.models.part.SyntheticBasisSolutionPT

class AutoSolveHandler(
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
        val function = Function(coefficients = functionCoefficients)
        var renderedSteps = ""
        var syntheticSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexMethodTask: SimplexMethod? = null
        var syntheticBasisTask: SyntheticBasisMethod? = null

        if (method == Method.SYNTHETIC_BASIS) {
            syntheticBasisTask =
                SyntheticBasisMethod(
                    matrix = matrix,
                    function = function,
                    taskType = taskType,
                )
            if (modeField from taskMetadataForm == null) {
                syntheticBasisTask.solve()
            }

            val solution =
                syntheticBasisTask.renderSteps(
                    request = request,
                    forSyntheticBasis = true,
                )
            renderedSteps = solution.first + syntheticBasisTask.renderSolution(method = method, request = request)
            syntheticSteps = solution.second

            simplexMethodTask = syntheticBasisTask.getSimplexMethodBySolution()
        } else {
            simplexMethodTask =
                SimplexMethod(
                    matrix = matrix,
                    function = function,
                    startBasis = basis,
                    taskType = taskType,
                )
        }
        simplexMethodTask?.let {
            if (modeField from taskMetadataForm == null) {
                simplexMethodTask.solve()
            }
            val solution = simplexMethodTask.renderSteps(request)
            renderedSteps += solution.first +
                simplexMethodTask.renderSolution(
                    method = Method.SIMPLEX_METHOD,
                    request = request,
                )
            simplexSteps = solution.second
        }

        return render(request) draw
            HomePageVM(
                metadataForm =
                    taskMetadataForm
                        .with(freeField of defaultFree)
                        .let {
                            if (method == Method.SYNTHETIC_BASIS) {
                                it.minus("basisJson")
                                    .with(basisField of defaultBasis)
                            } else {
                                it
                            }
                        },
                simplexMethodForm =
                    simplexMethodTask?.let {
                        WebForm().with(simplexMethodField of it)
                    },
                syntheticBasisForm = syntheticBasisTask?.let { WebForm().with(syntheticBasisMethodField of it) },
                renderedSteps = renderedSteps,
                syntheticSteps = syntheticSteps,
                simplexSteps = simplexSteps,
                hasPreviousStep = false,
                hasNextStep = false,
            )
    }

    private fun SimplexBase.renderSteps(
        request: Request,
        forSyntheticBasis: Boolean = false,
    ): Pair<String, List<Triple<Int, Int, Int>>> {
        var renderedSteps = ""
        val methodSteps = mutableListOf<Triple<Int, Int, Int>>()
        stepsTables.forEachIndexed { i, simplexTable ->
            val webForm =
                WebForm().with(
                    basisField of simplexTable.matrix.basis,
                    freeField of simplexTable.matrix.free,
                    matrixField of simplexTable.matrix.coefficients,
                    functionField of
                        simplexTable.function.inBasisOf(
                            matrix = simplexTable.matrix,
                            taskType = taskType,
                            invertLast = true,
                        ).coefficients,
                )
            renderedSteps +=
                (
                    render(request) draw
                        SimplexStepPT(
                            stepIdx = i,
                            stepForm = webForm,
                            isSyntheticBasisStep = forSyntheticBasis,
                        )
                ).body
            methodSteps.add(
                if (i != stepsTables.lastIndex) {
                    Triple(i, stepsReplaces[i]!!.first, stepsReplaces[i]!!.second)
                } else {
                    Triple(i, -1, -1)
                },
            )
        }
        return Pair(renderedSteps, methodSteps)
    }

    private fun SimplexBase.renderSolution(
        method: Method,
        request: Request,
    ): String {
        return when (method) {
            Method.SIMPLEX_METHOD -> {
                (
                    render(request) draw
                        when (val solution = getSolution()) {
                            is Success ->
                                SimplexSolutionPT(
                                    vertex = solution.value.first,
                                    functionValue = solution.value.second,
                                )

                            is Failure ->
                                SimplexSolutionPT(
                                    hasSolution = false,
                                    cause = solution.reason.text,
                                    functionValue = Fraction(0, 1),
                                    vertex = emptyList(),
                                )
                        }
                ).bodyString()
            }

            Method.SYNTHETIC_BASIS -> {
                (
                    render(request) draw
                        when (val solution = getSolution(1)) {
                            is Success ->
                                SyntheticBasisSolutionPT(
                                    simplexBasis = solution.value.first,
                                    unnecessaryConstraints = solution.value.second,
                                )

                            is Failure ->
                                SyntheticBasisSolutionPT(
                                    hasSolution = false,
                                    cause = solution.reason.text,
                                    simplexBasis = emptyList(),
                                    unnecessaryConstraints = emptyList(),
                                )
                        }
                ).bodyString()
            }
        }
    }
}
