package ru.yarsu.web.models.common

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.yarsu.web.lenses.FormErrorType

@Suppress("LongParameterList")
class HomePageVM(
    val metadataForm: WebForm? = null,
    val syntheticBasisForm: WebForm? = null,
    val simplexMethodForm: WebForm? = null,
    val renderedSteps: String = "",
    val syntheticSteps: List<Triple<Int, Int, Int>> = emptyList(),
    val simplexSteps: List<Triple<Int, Int, Int>> = emptyList(),
    val nextStepForm: String = "",
    val errors: Map<FormErrorType, List<String>> = emptyMap(),
) : ViewModel {
    val syntheticStepsJs: String
        get() = renderStepsJs(syntheticSteps)

    val simplexStepsJs: String
        get() = renderStepsJs(simplexSteps)

    val functionErrors: List<String>
        get() = errors[FormErrorType.FUNCTION] ?: emptyList()

    val matrixErrors: List<String>
        get() = errors[FormErrorType.MATRIX] ?: emptyList()

    val basisErrors: List<String>
        get() = errors[FormErrorType.BASIS] ?: emptyList()

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
