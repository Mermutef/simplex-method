package ru.yarsu.web.models.partials

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class SimplexStepPT(
    val stepIdx: Int,
    val stepForm: WebForm? = null,
) : ViewModel
