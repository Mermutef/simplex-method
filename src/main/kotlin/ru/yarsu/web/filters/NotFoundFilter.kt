package ru.yarsu.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.handlers.error.NotFoundHandler

class NotFoundFilter(
    private val render: ContextAwareViewRender,
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        NotFoundHandler(
            next,
            render,
        )
}
