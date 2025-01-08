package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import ru.yarsu.web.context.ContextTools
import ru.yarsu.web.handlers.calculating.LoadFromFileHandler
import ru.yarsu.web.handlers.calculating.SaveToFileHandler
import ru.yarsu.web.handlers.calculating.AutoSolveHandler
import ru.yarsu.web.handlers.calculating.NextStepHandler
import ru.yarsu.web.handlers.calculating.PreviousStepHandler
import ru.yarsu.web.handlers.common.HomeHandler
import ru.yarsu.web.handlers.common.KillHandler
import ru.yarsu.web.handlers.common.PingHandler

class HandlersContainer(
    contextTools: ContextTools,
    mapper: ObjectMapper,
) {
    val home = HomeHandler(contextTools.render)
    val solveCalculating = AutoSolveHandler(contextTools.render)
    val nextStep = NextStepHandler(contextTools.render)
    val previousStep = PreviousStepHandler(contextTools.render)
    val loadFromFile = LoadFromFileHandler(contextTools.render, mapper)
    val saveToFile = SaveToFileHandler(contextTools.render, mapper)
    val kill = KillHandler()
    val ping = PingHandler()
}
