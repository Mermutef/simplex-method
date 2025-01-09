package ru.yarsu.web.models.part

import org.http4k.template.ViewModel

class SimplexPaginationPT(
    val possibleReplaces: List<Pair<Int, Int>>,
    val hasNextStep: Boolean = true,
    val hasPreviousStep: Boolean = false,
) : ViewModel
