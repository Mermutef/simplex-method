package ru.yarsu.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.entities.Function
import ru.yarsu.entities.Matrix
import ru.yarsu.simplex.Method
import ru.yarsu.simplex.SimplexTable
import ru.yarsu.simplex.SyntheticBasis
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
        println(basis)
        println(free)
        println(matrixCoefficients)
        println(functionCoefficients)
        println(taskType)
        println(method)
        println()
        val n = matrixCoefficients.first().size
        val m = matrixCoefficients.size
        val matrix = Matrix(
            m = m,
            n = n,
            coefficients = matrixCoefficients,
            basis = basis,
            free = free,
        )
        val function = Function(coefficients = functionCoefficients)
        when (method) {
            Method.SIMPLEX_METHOD -> {
                SimplexTable(
                    matrix = matrix.straightRunning().reverseRunning(),
                    function = function,
                    taskType = taskType,
                ).let { println(it) }
            }

            Method.SYNTHETIC_BASIS -> {
                SyntheticBasis(
                    matrix = matrix,
                    function = function,
                ).let { println(it) }
            }
        }

        return render(request) draw HomePageVM(form = form)
    }
}