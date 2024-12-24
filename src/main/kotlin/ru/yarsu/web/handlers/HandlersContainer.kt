package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import ru.yarsu.web.context.ContextTools

class HandlersContainer(
    contextTools: ContextTools,
    mapper: ObjectMapper,
) {
    val home = HomeHandler(contextTools.render)
    val postHome = PostHomeHandler(contextTools.render, mapper)
    val ping = PingHandler()
}
