package ru.yarsu.domain.entities

import ru.yarsu.domain.simplex.Method

class SerializedTask(
    val method: Method,
    val jsonContent: String,
)
