package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import ru.yarsu.web.context.ContextTools
import ru.yarsu.web.handlers.common.HomeHandler
import ru.yarsu.web.handlers.common.KillHandler
import ru.yarsu.web.handlers.common.PingHandler
import ru.yarsu.web.handlers.serialization.LoadFromFileHandler
import ru.yarsu.web.handlers.serialization.SaveToFileHandler
import ru.yarsu.web.handlers.solving.AutoSolveHandler
import ru.yarsu.web.handlers.solving.InitStepByStepHandler
import ru.yarsu.web.handlers.solving.NextStepHandler
import ru.yarsu.web.handlers.solving.PreviousStepHandler

class HandlersContainer(
    contextTools: ContextTools,
    mapper: ObjectMapper,
) {
    val home = HomeHandler(contextTools.render)
    val solveCalculating = AutoSolveHandler(contextTools.render)
    val initStepByStep = InitStepByStepHandler(contextTools.render)
    val nextStep = NextStepHandler(contextTools.render)

    val previousStep = PreviousStepHandler(contextTools.render)
    val loadFromFile = LoadFromFileHandler(contextTools.render, mapper)
    val saveToFile = SaveToFileHandler(contextTools.render, mapper)
    val kill = KillHandler()
    val ping = PingHandler()
}
