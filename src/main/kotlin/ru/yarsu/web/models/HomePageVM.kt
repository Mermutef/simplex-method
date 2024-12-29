package ru.yarsu.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class HomePageVM(
    val form: WebForm? = null,
    val renderedSteps: String = "",
    val stepsToRender: List<Triple<Int, Int, Int>> = emptyList(),
) : ViewModel {
    val jsObject: String
        get() {
            var res = ""
            stepsToRender.forEach {
                res += """
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
