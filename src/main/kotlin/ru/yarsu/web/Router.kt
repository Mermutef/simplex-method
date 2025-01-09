package ru.yarsu.web

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import ru.yarsu.web.handlers.HandlersContainer

fun router(handlers: HandlersContainer): RoutingHttpHandler =
    routes(
        "/" bind Method.GET to handlers.home,
        "/" bind Method.POST to handlers.solveCalculating,
        "/step-by-step" bind Method.POST to handlers.initStepByStep,
        "/next" bind Method.POST to handlers.nextStep,
//        "/previous" bind Method.POST to handlers.previousStep,
        "/load-from-file" bind Method.POST to handlers.loadFromFile,
        "/save-to-file" bind Method.POST to handlers.saveToFile,
        "/ping" bind Method.GET to handlers.ping,
        "kill" bind Method.GET to handlers.kill,
    )

fun notFound() = Response(Status.NOT_FOUND)

infix fun BiDiBodyLens<ViewModel>.draw(viewModel: ViewModel?) =
    viewModel
        ?.let { Response(Status.OK).with(this of it) }
        ?: notFound()
