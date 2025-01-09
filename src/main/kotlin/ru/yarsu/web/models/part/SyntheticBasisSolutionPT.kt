package ru.yarsu.web.models.part

import org.http4k.template.ViewModel

class SyntheticBasisSolutionPT(
    val simplexBasis: List<Int>,
    val unnecessaryConstraints: List<Int>,
    val hasSolution: Boolean = true,
    val cause: String = "",
) : ViewModel {
    val prettyHTMLSimplexBasis: String
        get() = "[${simplexBasis.joinToString(", ") { "x<sub>${it + 1}</sub>" }}]"
}
