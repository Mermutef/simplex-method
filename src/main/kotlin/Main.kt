package ru.yarsu

import java.io.File
import kotlin.math.min


fun main(args: Array<String>) {
    val file = File(
        args
            .firstOrNull { it.startsWith("file=") }
            ?.removePrefix("file=")
            ?: "matrix.txt"
    )
    val rows = runCatching { file.readLines().filter { it.isNotBlank() || it.isNotEmpty() } }.getOrNull()
        ?: error("Файл ${file.path} не найден")

    val m = rows[0].split(" ")[0]
        .toIntOrNull()
        ?.takeIf { it > 0 }
        ?: error("Число строк должно быть натуральным числом.")

    val n = rows[0].split(" ")[1]
        .toIntOrNull()
        ?.takeIf { it > 0 && it >= m }
        ?: error("Число строк должно быть натуральным числом, не меньшим m=$m")

    val newBasis = rows[1].split(" ")
        .map { it.toIntOrNull() ?: error("Индексы базисных переменных должны быть целыми числами") }
        .distinct()

    val defaultBasis = mutableListOf<Int>()
    for (i in 0 until m) {
        defaultBasis.add(i)
    }

    val matrix = Matrix(
        n = n,
        m = m,
        coefficients = rows.slice(2..<rows.size).map { row ->
            row.split(" ")
                .map {
                    it.toFractionOrNull()
                        ?: error("Дробь должна выглядеть как '8/-15' и иметь знаменатель отличный от нуля")
                }
        },
        basis = defaultBasis,
    )
    println("\nMatrix:\n$matrix")
    val straightRunning = matrix.inBasis(newBasis).straightRunning()
    println("\nStraight running:\n$straightRunning")
    println("\nReverse running:\n${straightRunning.reverseRunning()}")
}
