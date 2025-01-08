package ru.yarsu.web.handlers.calculating

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexTable
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.fileField
import ru.yarsu.web.lenses.SimplexFormLenses.fileForm
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.notFound

class LoadFromFileHandler(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = fileForm(request)
        if (form.errors.isNotEmpty()) return notFound()
        val json = fileField(form).content
        val initialTable = mapper.readValue<SimplexTable>(json)
        val resulForm =
            WebForm().with(
                matrixField of initialTable.matrix.coefficients,
                functionField of initialTable.function.coefficients,
                basisField of initialTable.matrix.basis,
                freeField of initialTable.matrix.free,
                methodField of Method.SIMPLEX_METHOD,
                taskTypeField of initialTable.taskType,
            )

        return render(request) draw HomePageVM(form = resulForm)
    }
}
