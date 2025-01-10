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
import ru.yarsu.domain.simplex.SimplexBase
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
import ru.yarsu.web.lenses.SimplexFormLenses.playModeField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.lenses.SimplexFormLenses.useFractionsField
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
        val simplexForm: WebForm?
        val syntheticBasisForm: WebForm?
        val basisDeserializedTask = when (serializedTask.method) {
            Method.SYNTHETIC_BASIS -> mapper.readValue<SyntheticBasisMethod>(serializedTask.jsonContent)
            Method.SIMPLEX_METHOD -> mapper.readValue<SimplexMethod>(serializedTask.jsonContent)
        }
        val basicMetadataForm = WebForm().with(
            matrixField of basisDeserializedTask.matrix.coefficients,
            functionField of basisDeserializedTask.function.coefficients,
            methodField of serializedTask.method,
            taskTypeField of basisDeserializedTask.taskType,
            playModeField of serializedTask.playMode,
            useFractionsField of serializedTask.useFractions,
        )
        when (serializedTask.method) {
            Method.SIMPLEX_METHOD -> {
                val deserializedTask = basisDeserializedTask as SimplexMethod
                metadataForm =
                    basicMetadataForm.with(
                        basisField of deserializedTask.startBasis,
                        freeField of (0..<deserializedTask.matrix.n).filter { it !in deserializedTask.startBasis },
                    )
                simplexForm = WebForm().with(simplexMethodField of deserializedTask)
                syntheticBasisForm = null
            }

            Method.SYNTHETIC_BASIS -> {
                val deserializedTask = basisDeserializedTask as SyntheticBasisMethod
                metadataForm =
                    basicMetadataForm.with(
                        basisField of deserializedTask.matrix.basis,
                        freeField of deserializedTask.matrix.free,
                    )
                syntheticBasisForm = WebForm().with(syntheticBasisMethodField of deserializedTask)
                simplexForm = null
            }
        }

        return render(request) draw
                HomePageVM(
                    metadataForm = metadataForm,
                    syntheticBasisForm = syntheticBasisForm,
                    simplexMethodForm = simplexForm,
                )
    }
}
