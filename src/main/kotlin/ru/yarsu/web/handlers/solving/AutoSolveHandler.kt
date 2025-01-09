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
import ru.yarsu.domain.simplex.SimplexMethod
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
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.models.part.SimplexSolutionPT
import ru.yarsu.web.models.part.SimplexStepPT

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

        val currentTask =
            SimplexMethod(
                matrix = matrix,
                function = function,
                startBasis = basis,
                taskType = taskType,
            )

        if (modeField from taskMetadataForm == null) {
            currentTask.solve()
        }

        var renderedSteps = ""
        val trueSteps = mutableListOf<Triple<Int, Int, Int>>()

        currentTask.stepsTables.forEachIndexed { i, simplexTable ->
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
                        )
                ).body
            trueSteps.add(
                if (i != currentTask.stepsTables.lastIndex) {
                    Triple(i, currentTask.stepsReplaces[i]!!.first, currentTask.stepsReplaces[i]!!.second)
                } else {
                    Triple(i, -1, -1)
                },
            )
        }

        renderedSteps +=
            (
                render(request) draw
                    when (val solution = currentTask.getSolution()) {
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
            ).body

        return render(request) draw
            HomePageVM(
                metadataForm =
                    taskMetadataForm
                        .with(freeField of defaultFree),
//                            .let {
//                                if (method == Method.SYNTHETIC_BASIS) {
//                                    it.minus("basisJson").with(basisField of defaultBasis)
//                                } else {
//                                    it
//                                }
//                            },
                methodForm = WebForm().with(simplexMethodField of currentTask),
                renderedSteps = renderedSteps,
                syntheticSteps = listOf(),
                trueSteps = trueSteps,
                hasPreviousStep = false,
                hasNextStep = false,
            )
    }
}
