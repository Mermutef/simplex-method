package ru.yarsu.web.models.part

import org.http4k.template.ViewModel
import ru.yarsu.domain.entities.Fraction

class SimplexSolutionPT(
    val vertex: List<Fraction>,
    val functionValue: Fraction,
    val hasSolution: Boolean = true,
    val cause: String = "",
) : ViewModel
