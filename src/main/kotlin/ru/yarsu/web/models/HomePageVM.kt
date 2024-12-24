package ru.yarsu.web.models

import org.http4k.lens.MultipartForm
import org.http4k.template.ViewModel

class HomePageVM(
    val strings: List<String> = emptyList(),
    val form: MultipartForm? = null,
) : ViewModel
