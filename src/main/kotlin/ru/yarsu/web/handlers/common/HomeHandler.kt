package ru.yarsu.web.handlers.common

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.lenses.SimplexFormLenses.useFractionsField
import ru.yarsu.web.models.common.HomePageVM

class HomeHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(Status.OK).with(
            render(request) of HomePageVM(metadataForm = WebForm().with(useFractionsField of true)),
        )
    }
}
