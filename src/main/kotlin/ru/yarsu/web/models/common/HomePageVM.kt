package ru.yarsu.web.models.common

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class HomePageVM(
    val metadataForm: WebForm? = null,
    val syntheticBasisForm: WebForm? = null,
    val simplexMethodForm: WebForm? = null,
    val renderedSteps: String = "",
    val syntheticSteps: List<Triple<Int, Int, Int>> = emptyList(),
    val simplexSteps: List<Triple<Int, Int, Int>> = emptyList(),
    val hasNextStep: Boolean = true,
    val hasPreviousStep: Boolean = false,
) : ViewModel {
    val syntheticStepsJs: String
        get() = renderStepsJs(syntheticSteps)

    val simplexStepsJs: String
        get() = renderStepsJs(simplexSteps)

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
