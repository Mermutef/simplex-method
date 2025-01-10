package ru.yarsu.domain.entities

import ru.yarsu.domain.simplex.Method

class SerializedTask(
    val method: Method,
    val playMode: Boolean = false,
    val useFractions: Boolean = true,
    val jsonContent: String,
)
