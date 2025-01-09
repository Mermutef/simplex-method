package ru.yarsu.web.models.part

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class SimplexStepPT(
    val stepIdx: Int,
    val stepForm: WebForm? = null,
    val isSyntheticBasisStep: Boolean = false,
) : ViewModel
