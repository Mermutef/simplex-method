package ru.yarsu.web.handlers.error

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.models.error.NotFoundVM

class NotFoundHandler(
    private val next: HttpHandler,
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val response = next(request)
        if (response.status == Status.NOT_FOUND) {
            return response.with(
                render(request) of NotFoundVM(request.uri.toString()),
            )
        }
        return response
    }
}
