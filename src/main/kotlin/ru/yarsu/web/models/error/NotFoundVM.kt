package ru.yarsu.web.models.error

import org.http4k.template.ViewModel

class NotFoundVM(
    val description: String,
) : ViewModel
