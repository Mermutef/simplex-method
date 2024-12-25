package ru.yarsu.web.context

import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.RequestContexts
import ru.yarsu.config.WebConfig
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.context.templates.createContextAwareTemplateRenderer

class ContextTools(
    config: WebConfig,
) {
    val appContexts = RequestContexts()
    val render =
        ContextAwareViewRender.withContentType(
            createContextAwareTemplateRenderer(config),
            TEXT_HTML,
        ).associateContextLenses(emptyMap())
}
