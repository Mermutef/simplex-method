package ru.yarsu.web

import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.web.handlers.HandlersContainer

fun router(handlers: HandlersContainer): RoutingHttpHandler =
    routes(
        "/" bind Method.GET to handlers.home,
        "/" bind Method.POST to handlers.postHome,
        "/ping" bind Method.GET to handlers.ping,
    )
