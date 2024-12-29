package ru.yarsu.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.db.CurrentTask
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexTable
import ru.yarsu.domain.simplex.SyntheticBasis
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.HomePageVM
import ru.yarsu.web.models.partials.SimplexStepPT

class RunCalculatingHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = taskForm from request
        if (form.errors.isNotEmpty()) {
            return render(request) draw HomePageVM(form = form)
        }
        val basis = basisField(form)
        val free = freeField(form)
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
        when (method) {
            Method.SIMPLEX_METHOD -> {
                SimplexTable(
                    matrix = matrix.inBasis(newBasis = basis, newFree = free).straightRunning().reverseRunning(),
                    function = function,
                    taskType = taskType,
                )
            }

            Method.SYNTHETIC_BASIS -> {
                SyntheticBasis(
                    matrix = matrix,
                    function = function,
                )
            }
        }

        val currentTask =
            CurrentTask(
                method = method,
                taskType = taskType,
                function = function,
                matrix = matrix.inBasis(newBasis = basis, newFree = free).straightRunning().reverseRunning(),
            )

        currentTask.solve()
        println("solved")

        val tables = currentTask.simplexTables
        val replaces = currentTask.replaces
        val stepsToRender = mutableListOf<Int>()
        var renderedSteps = ""
        for (i in 1..<tables.size) {
            println(tables[i].function.inBasisOf(tables[i].matrix, taskType).coefficients)
            println("+-+-+-+")
            println(tables[i].function.coefficients)
            println("----++++++")
            val webForm =
                WebForm().with(
                    basisField of tables[i].matrix.basis,
                    freeField of tables[i].matrix.free,
                    matrixField of tables[i].matrix.coefficients,
                    functionField of tables[i].function.inBasisOf(tables[i].matrix, taskType).coefficients,
                )
            renderedSteps += (render(request) draw
                    SimplexStepPT(
                        stepIdx = i,
                        stepForm = webForm,
                    )).body
        }
        return render(request) draw
                HomePageVM(
                    form = form,
                    renderedSteps = renderedSteps,
                    stepsToRender = (1..<tables.size).toList(),
                )
    }
}
