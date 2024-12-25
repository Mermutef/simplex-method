package ru.yarsu.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

class HomePageVM(
    val form: WebForm? = null,
) : ViewModel
