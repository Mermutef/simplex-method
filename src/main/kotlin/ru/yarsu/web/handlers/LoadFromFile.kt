package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.simplex.SimplexTable
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.fileField
import ru.yarsu.web.lenses.SimplexFormLenses.fileForm
import ru.yarsu.web.models.HomePageVM
import ru.yarsu.web.notFound

class LoadFromFile(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = fileForm(request)
        if (form.errors.isNotEmpty()) return notFound()
        val json = fileField(form).content
        val initialTable = mapper.readValue<SimplexTable>(json)

        return render(request) draw HomePageVM(listOf(initialTable.toString()))
    }
}
