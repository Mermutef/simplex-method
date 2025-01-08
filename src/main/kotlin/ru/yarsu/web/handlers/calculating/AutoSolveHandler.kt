package ru.yarsu.web.handlers.calculating

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.db.CurrentTask
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.simplex.Method
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.currentTaskField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.modeField
import ru.yarsu.web.lenses.SimplexFormLenses.taskForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.models.part.SimplexStepPT

class AutoSolveHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = taskForm from request
        if (form.errors.isNotEmpty()) {
            println(form.errors)
            return render(request) draw HomePageVM(form = form)
        }
        val basis = basisField(form)
        val matrixCoefficients = matrixField(form)
        val functionCoefficients = functionField(form)
        val taskType = taskTypeField(form)
        val method = methodField(form)
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

        val currentTask = CurrentTask(
            method = method,
            taskType = taskType,
            function = function,
            matrix =
                takeIf { method == Method.SIMPLEX_METHOD }?.let {
                    matrix.solveGauss(withBasis = basis)
                } ?: matrix,
        )

        println(modeField(form))
        if (modeField(form) == null) {
            currentTask.solve()
        }

        var renderedSteps = ""
        val syntheticSteps = mutableListOf<Triple<Int, Int, Int>>()
        val trueSteps = mutableListOf<Triple<Int, Int, Int>>()
        currentTask.syntheticSimplexTables.forEachIndexed { i, simplexTable ->
            val webForm =
                WebForm().with(
                    basisField of simplexTable.matrix.basis,
                    freeField of simplexTable.matrix.free,
                    matrixField of simplexTable.matrix.coefficients,
                    functionField of simplexTable.function.inBasisOf(simplexTable.matrix, taskType).coefficients,
                )
            renderedSteps +=
                (
                        render(request) draw
                                SimplexStepPT(
                                    stepIdx = i,
                                    stepForm = webForm,
                                    isLast = i == currentTask.syntheticSimplexTables.lastIndex,
                                    isSyntheticBasisStep = true,
                                )
                        ).body
            syntheticSteps.add(
                if (i != currentTask.syntheticSimplexTables.lastIndex) {
                    Triple(i, currentTask.syntheticReplaces[i]!!.first, currentTask.syntheticReplaces[i]!!.second)
                } else {
                    Triple(i, -1, -1)
                },
            )
        }

        currentTask.simplexTables.forEachIndexed { i, simplexTable ->
            val webForm =
                WebForm().with(
                    basisField of simplexTable.matrix.basis,
                    freeField of simplexTable.matrix.free,
                    matrixField of simplexTable.matrix.coefficients,
                    functionField of simplexTable.function.inBasisOf(simplexTable.matrix, taskType).coefficients,
                )
            renderedSteps +=
                (
                        render(request) draw
                                SimplexStepPT(
                                    stepIdx = i,
                                    stepForm = webForm,
                                    isLast = i == currentTask.simplexTables.lastIndex,
                                )
                        ).body
            trueSteps.add(
                if (i != currentTask.simplexTables.lastIndex) {
                    Triple(i, currentTask.simplexReplaces[i]!!.first, currentTask.simplexReplaces[i]!!.second)
                } else {
                    Triple(i, -1, -1)
                },
            )
        }
        return render(request) draw
                HomePageVM(
                    form =
                        form
                            .minus("currentTaskJson")
                            .with(currentTaskField of currentTask)
                            .with(freeField of defaultFree)
                            .let {
                                if (method == Method.SYNTHETIC_BASIS) {
                                    it.minus("basisJson").with(basisField of defaultBasis)
                                } else {
                                    it
                                }
                            },
                    renderedSteps = renderedSteps,
                    syntheticSteps = syntheticSteps,
                    trueSteps = trueSteps,
                )
    }
}