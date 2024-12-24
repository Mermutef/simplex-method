package ru.yarsu.web.context

import org.http4k.core.*
import org.http4k.core.ContentType.Companion.TEXT_HTML
import ru.yarsu.config.WebConfig
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.context.templates.createContextAwareTemplateRenderer
import ru.yarsu.web.context.templates.createTemplateRenderer

class ContextTools(
    config: WebConfig,
) {
    val appContexts = RequestContexts()
    val vanillaRender = createTemplateRenderer(config)
    val render = ContextAwareViewRender.withContentType(
        createContextAwareTemplateRenderer(config),
        TEXT_HTML
    ).associateContextLenses(emptyMap())
}
