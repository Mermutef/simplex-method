package ru.yarsu.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class HomePageVM(
    val form: WebForm? = null,
    val renderedSteps: String = "",
    val stepsToRender: List<Int> = emptyList(),
) : ViewModel
