package ru.yarsu.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import kotlin.system.exitProcess

class KillHandler: HttpHandler {
    override fun invoke(request: Request): Response {
        exitProcess(0)
    }
}