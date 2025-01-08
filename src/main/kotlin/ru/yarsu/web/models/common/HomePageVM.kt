package ru.yarsu.web.models.common

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class HomePageVM(
    val form: WebForm? = null,
    val renderedSteps: String = "",
    val syntheticSteps: List<Triple<Int, Int, Int>> = emptyList(),
    val trueSteps: List<Triple<Int, Int, Int>> = emptyList(),
) : ViewModel {
    val syntheticStepsJs: String
        get() = renderStepsJs(syntheticSteps)

    val trueStepsJs: String
        get() = renderStepsJs(trueSteps)

    fun renderStepsJs(steps: List<Triple<Int, Int, Int>>): String {
        var res = ""
        steps.forEach {
            res +=
                """
                {
                idx: ${it.first},
                s: ${it.second},
                r: ${it.third}
                },
                """.trimIndent()
        }
        return "[${res.dropLast(1)}]"
    }
}
