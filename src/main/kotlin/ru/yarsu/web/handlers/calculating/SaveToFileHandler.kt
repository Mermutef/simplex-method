package ru.yarsu.web.handlers.calculating

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.simplex.SimplexTable
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.taskForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import java.io.ByteArrayInputStream

class SaveToFileHandler(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = taskForm from request
        if (form.errors.isNotEmpty()) {
            println(form.errors)
            return render(request) draw HomePageVM(form = form)
        }
        val matrixCoefficients = matrixField(form)
        val functionCoefficients = functionField(form)
        val taskType = taskTypeField(form)
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
        val byteStream =
            mapper.writeValueAsBytes(
                SimplexTable(
                    matrix = matrix,
                    function = function,
                    taskType = taskType,
                ),
            )
        return Response(
            Status.OK,
        ).body(ByteArrayInputStream(byteStream))
            .header("Content-Type", "application/json")
            .header("Content-Length", byteStream.size.toString())
    }
}
