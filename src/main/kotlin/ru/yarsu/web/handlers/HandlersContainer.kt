package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import ru.yarsu.web.context.ContextTools
import ru.yarsu.web.handlers.calculating.LoadFromFileHandler
import ru.yarsu.web.handlers.calculating.SaveToFileHandler
import ru.yarsu.web.handlers.calculating.SolveHandler
import ru.yarsu.web.handlers.common.HomeHandler
import ru.yarsu.web.handlers.common.KillHandler
import ru.yarsu.web.handlers.common.PingHandler

class HandlersContainer(
    contextTools: ContextTools,
    mapper: ObjectMapper,
) {
    val home = HomeHandler(contextTools.render)
    val solveCalculating = SolveHandler(contextTools.render)
    val loadFromFile = LoadFromFileHandler(contextTools.render, mapper)
    val saveToFile = SaveToFileHandler(contextTools.render, mapper)
    val kill = KillHandler()
    val ping = PingHandler()
}
