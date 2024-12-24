package ru.yarsu.web.context.templates

import org.http4k.template.PebbleTemplates
import org.http4k.template.TemplateRenderer
import ru.yarsu.config.WebConfig

const val TEMPLATES_DIR = "src/main/resources"

fun createContextAwareTemplateRenderer(webConfig: WebConfig): ContextAwareTemplateRenderer =
    ContextAwarePebbleTemplates().let { templates ->
        if (webConfig.hotReload) {
            templates.HotReload(TEMPLATES_DIR)
        } else {
            templates.CachingClasspath()
        }
    }

fun createTemplateRenderer(webConfig: WebConfig): TemplateRenderer =
    PebbleTemplates().let { templates ->
        if (webConfig.hotReload) {
            templates.HotReload(TEMPLATES_DIR)
        } else {
            templates.CachingClasspath()
        }
    }