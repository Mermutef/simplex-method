package ru.yarsu.web.handlers.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.SerializedTask
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.playModeField
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.lenses.SimplexFormLenses.useFractionsField
import ru.yarsu.web.models.common.HomePageVM
import java.io.ByteArrayInputStream

class SaveToFileHandler(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = taskMetadataForm from request
        if (form.errors.isNotEmpty()) {
            println(form.errors)
            return render(request) draw HomePageVM(metadataForm = form)
        }
        val method = methodField from form
        val playMode = (playModeField from form) ?: false
        val inFractions = (useFractionsField from form) ?: false
        val matrixCoefficients = matrixField from form
        val functionCoefficients = functionField from form
        val taskType = taskTypeField from form
        val n = matrixCoefficients.first().size
        val m = matrixCoefficients.size
        val startBasis = basisField from form
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
                SerializedTask(
                    method = method,
                    playMode = playMode,
                    useFractions = inFractions,
                    jsonContent =
                        mapper.writeValueAsString(
                            when (method) {
                                Method.SIMPLEX_METHOD ->
                                    SimplexMethod(
                                        matrix = matrix,
                                        function = function,
                                        taskType = taskType,
                                        startBasis = startBasis,
                                    )

                                Method.SYNTHETIC_BASIS ->
                                    SyntheticBasisMethod(
                                        matrix = matrix,
                                        function = function,
                                        taskType = taskType,
                                    )
                            },
                        ),
                ),
            )
        return Response(
            Status.OK,
        ).body(ByteArrayInputStream(byteStream))
            .header("Content-Type", "application/json")
            .header("Content-Length", byteStream.size.toString())
    }
}
