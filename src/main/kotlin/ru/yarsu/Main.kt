package ru.yarsu

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.domain.config.AppConfig
import ru.yarsu.web.context.ContextTools
import ru.yarsu.web.filters.NotFoundFilter
import ru.yarsu.web.handlers.HandlersContainer
import ru.yarsu.web.notFound
import ru.yarsu.web.router
import java.awt.Desktop
import java.net.URI

fun openInBrowser(uri: URI?): Boolean {
    return runCatching { Desktop.getDesktop() }
        .getOrNull()
        ?.takeIf { it.isSupported(Desktop.Action.BROWSE) }
        ?.browse(uri) != null
}

fun main() {
    val appConfig = AppConfig.readConfiguration()
    val contextTools = ContextTools(appConfig.webConfig)
    val mapper = jacksonObjectMapper()
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    val appWithStaticResources =
        ServerFilters.InitialiseRequestContext(contextTools.appContexts)
            .then(NotFoundFilter(contextTools.render))
            .then(
                ServerFilters.CatchAll {
                    it.printStackTrace()
                    notFound()
                },
            )
            .then(
                routes(
                    router(HandlersContainer(contextTools, mapper)),
                    static(ResourceLoader.Classpath("/ru/yarsu/public")),
                ),
            )
    val server = appWithStaticResources.asServer(Netty(appConfig.webConfig.webPort)).start()
    openInBrowser(URI.create("http://localhost:${server.port()}"))
    server.block()
}
