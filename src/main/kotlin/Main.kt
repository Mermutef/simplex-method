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

    val coffs = fn.split(" ").mapNotNull { it.toFractionOrNull() }.toMutableList()

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
        .map {
            it.toIntOrNull()?.let { idx -> idx - 1 } ?: error("Индексы базисных переменных должны быть целыми числами")
        }
        .distinct()

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
        basis = (0..<m).toList(),
        free = (m..<n - 1).toList(),
    )
    val f = Function(coffs, n)
    println("Задача:\n$f")
    println(matrix)
    val straightRunning =
        matrix.inBasis(newBasis = newBasis, newFree = (0..<n - 1).filter { it !in newBasis }).straightRunning()
    println("\nПрямой ход:\n$straightRunning")
    val reverseRunning = straightRunning.reverseRunning()
    println("\nОбратный ход:\n${reverseRunning}")
    println("\nЗадача после подстановки нового базиса:\n${f.inBasis(reverseRunning)}")
    println(SimplexStep(reverseRunning, f)(0, 3).matrix)
}
