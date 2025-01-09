package ru.yarsu.web.handlers.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.entities.SerializedTask
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.fileField
import ru.yarsu.web.lenses.SimplexFormLenses.fileForm
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.notFound

class LoadFromFileHandler(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = fileForm from request
        if (form.errors.isNotEmpty()) return notFound()
        val jsonFromForm = (fileField from form).content
        val serializedTask = mapper.readValue<SerializedTask>(jsonFromForm)
        val metadataForm: WebForm
        val methodForm: WebForm
        when (serializedTask.method) {
            Method.SIMPLEX_METHOD -> {
                val deserializedTask = mapper.readValue<SimplexMethod>(serializedTask.jsonContent)
                metadataForm =
                    WebForm().with(
                        matrixField of deserializedTask.matrix.coefficients,
                        functionField of deserializedTask.function.coefficients,
                        basisField of deserializedTask.matrix.basis,
                        freeField of deserializedTask.matrix.free,
                        methodField of Method.SIMPLEX_METHOD,
                        taskTypeField of deserializedTask.taskType,
                    )
                methodForm = WebForm().with(simplexMethodField of deserializedTask)
            }

            Method.SYNTHETIC_BASIS -> {
                val deserializedTask = mapper.readValue<SyntheticBasisMethod>(serializedTask.jsonContent)
                metadataForm =
                    WebForm().with(
                        matrixField of deserializedTask.matrix.coefficients,
                        functionField of deserializedTask.function.coefficients,
                        basisField of deserializedTask.matrix.basis,
                        freeField of deserializedTask.matrix.free,
                        methodField of Method.SYNTHETIC_BASIS,
                        taskTypeField of deserializedTask.taskType,
                    )
                methodForm = WebForm().with(syntheticBasisMethodField of deserializedTask)
            }
        }

        return render(request) draw
            HomePageVM(
                metadataForm = metadataForm,
                methodForm = methodForm,
            )
    }
}
