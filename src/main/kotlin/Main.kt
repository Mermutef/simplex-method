package ru.yarsu

import java.io.File
import kotlin.math.min


fun main(args: Array<String>) {
    val matrixFile = File(
        args
            .firstOrNull { it.startsWith("matrix=") }
            ?.removePrefix("matrix=")
            ?: "matrix.txt"
    )

    val funFile = File(
        args
            .firstOrNull { it.startsWith("fun=") }
            ?.removePrefix("fun=")
            ?: "function.txt"
    )

    val fn = runCatching { funFile.readLines().filter { it.isNotBlank() || it.isNotEmpty() } }
        .getOrNull()
        ?.firstOrNull()
        ?: error("Файл ${funFile.path} не найден")

    val f = fn.split(" ").mapNotNull { it.toFractionOrNull() }.toMutableList()

    while (f.first() == Fraction(0)) {
        f.removeFirst()
    }

    val rows = runCatching { matrixFile.readLines().filter { it.isNotBlank() || it.isNotEmpty() } }.getOrNull()
        ?: error("Файл ${matrixFile.path} не найден")

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
    if (f.size - 1 != n) {
        error("Функция должна быть степени n=$n")
    }
    println("Задача:")
    println("${f.toStringFun()} -> min")
    println(matrix)
    val straightRunning = matrix.inBasis(newBasis).straightRunning()
    println("\nПрямой ход:\n$straightRunning")
    println("\nОбратный ход:\n${straightRunning.reverseRunning()}")
}

fun List<Fraction>.toStringFun(): String {
    val n = this.size
    var res = "${this[0]} * x${n-1}"
    for (i in 1..n-2) {
        if (this[i] != Fraction(0)) {
            res += if (this[i] < 0) {
                " - "
            } else {
                " + "
            }
            res += "${this[i].abs()} * x${n-i-1}"
        }
    }
    if (this[0] != Fraction(0)) {
        res += if (this[0] < 0) {
            " - "
        } else {
            " + "
        }
        res += "${this[n-1]}"
    }
    return res
}