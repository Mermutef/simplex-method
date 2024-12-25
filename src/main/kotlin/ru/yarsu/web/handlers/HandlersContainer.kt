package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import ru.yarsu.web.context.ContextTools

class HandlersContainer(
    contextTools: ContextTools,
    mapper: ObjectMapper,
) {
    val home = HomeHandler(contextTools.render)
    val postHome = LoadFromFile(contextTools.render, mapper)
    val loadFromFile = LoadFromFile(contextTools.render, mapper)
    val kill = KillHandler()
    val ping = PingHandler()
}
