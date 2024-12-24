package ru.yarsu.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.models.HomePageVM

class HomeHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(Status.OK).with(
            render(request) of HomePageVM(),
        )
    }
}
