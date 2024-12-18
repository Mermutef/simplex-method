package ru.yarsu

import ru.yarsu.entities.Fraction.Companion.toFractionOrNull
import ru.yarsu.entities.Function
import ru.yarsu.entities.Matrix
import ru.yarsu.simplex.SimplexTable
import ru.yarsu.simplex.SyntheticBasis
import java.io.File


/**
 * Вопросы:
 * - пример, где нужен холостой ход (для отладки)
 * - условия возможности холостого хода - коэффициенты функции
 * при небазисных переменных 0, сама функция 0, в столбце b есть хотя бы один 0
 * и хотя бы в одной соответствующей строке есть хотя бы один не нулевой элемент?
 * - для простого симплекс-метода нужно, чтобы все bi были неотрицательны?
 * - насколько нужно комментировать код?
 * - можно ли сделать в виде браузерного приложения, а не настольного?
 */

// временная точка входа в приложение
fun main(args: Array<String>) {
    // файл откуда будет считана матрица, n, m, базис
    val matrixFile = File(
        args
            .firstOrNull { it.startsWith("matrix=") }
            ?.removePrefix("matrix=")
            ?: "matrix.txt"
    )

    // файл откуда будет считана функция
    val funFile = File(
        args
            .firstOrNull { it.startsWith("fun=") }
            ?.removePrefix("fun=")
            ?: "function.txt"
    )

    // попытка считать функцию, пустые строки отбрасываются
    val fn = runCatching { funFile.readLines().filter { it.isNotBlank() || it.isNotEmpty() } }
        .getOrNull()
        ?.firstOrNull()
        ?: error("Файл ${funFile.path} не найден")

    // первичная проверка коэффициентов функции и сбор в список
    val coffs = fn.split(" ").mapNotNull { it.toFractionOrNull() }.toMutableList()

    // чтение строк матрицы, n, m, базиса, пустые строки пропускаются
    val rows = runCatching { matrixFile.readLines().filter { it.isNotBlank() || it.isNotEmpty() } }.getOrNull()
        ?: error("Файл ${matrixFile.path} не найден")

    // число строк матрицы
    val m = rows[0].split(" ")[0]
        .toIntOrNull()
        ?.takeIf { it > 0 }
        ?: error("Число строк должно быть натуральным числом.")

    // число столбцов матрицы
    val n = rows[0].split(" ")[1]
        .toIntOrNull()
        ?.takeIf { it > 0 && it >= m }
        ?: error("Число строк должно быть натуральным числом, не меньшим m=$m")

    // базис, в котором будет происходить поиск решения
    val newBasis = rows[1].split(" ")
        .map {
            it.toIntOrNull() ?: error("Индексы базисных переменных должны быть целыми числами")
        }
        .distinct()

    // создание самой матрицы
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
    // создание функции
    val f = Function(coffs)

    // временный вывод
    println("Задача:\n$f")
    println(matrix.inBasis(newBasis, (0..<n - 1).filter { it !in newBasis }))
    // временное хранилище шагов симплекс-метода
    val simplexTables = mutableListOf<SimplexTable>()
    // создание нулевого шага симплекс-метода
    simplexTables.add(
        SimplexTable(
            matrix = matrix
                .inBasis(newBasis = newBasis, newFree = (0..<n - 1).filter { it !in newBasis })
                .straightRunning()
                .reverseRunning(),
            function = f,
        )
    )
    println("\nНачальная симплекс-таблица:\n${simplexTables.last()}")
    // пока можем сделать шаг симплекс метода
    while (true) {
        val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
        println("\nШаг ${simplexTables.size}")
        println("Вводим в базис x${possibleValues.first + 1}, выводим x${possibleValues.second + 1}")
        simplexTables.add(simplexTables.last() changeBasisBy possibleValues)
        println("Результат шага:\n${simplexTables.last()}")
    }
    val lastStep = simplexTables.last()
    println("\nМинимальное значение f* = ${lastStep.functionValue}")
    println("Достигается в точке x* = ${lastStep.vertex}")

    println()
    val simplexTables2 = mutableListOf<SimplexTable>()
    val sb = SyntheticBasis(matrix = matrix, function = f)
    simplexTables2.add(sb.startTable)
    println("\nНачальная симплекс-таблица искусственного базиса:\n${simplexTables2.last()}")
    while (true) {
        val possibleValues = simplexTables2.last().possibleReplaces()?.first() ?: break
        val s = possibleValues.first
        val r = possibleValues.second
        println("\nШаг ${simplexTables2.size}")
        println("Вводим в базис x${s + 1}, выводим x${r + 1}")
        simplexTables2.add(simplexTables2.last() changeBasisBy possibleValues)
        println("Результат шага:\n${simplexTables2.last()}")
    }
    val lastStep2 = simplexTables2.last()
    println("\nМинимальное значение f* = ${lastStep2.functionValue}")
    println("Достигается в точке x* = ${lastStep2.vertex}")

    println()
    val simplexTables3 = mutableListOf<SimplexTable>()
    simplexTables3.add(sb extractSolutionFrom lastStep2)
    println("\nНачальная симплекс-таблица после искусственного базиса:\n${simplexTables3.last()}")
    while (true) {
        val possibleValues = simplexTables3.last().possibleReplaces()?.first() ?: break
        val s = possibleValues.first
        val r = possibleValues.second
        println("\nШаг ${simplexTables3.size}")
        println("Вводим в базис x${s + 1}, выводим x${r + 1}")
        simplexTables3.add(simplexTables3.last() changeBasisBy possibleValues)
        println("Результат шага:\n${simplexTables3.last()}")
    }
    val lastStep3 = simplexTables3.last()
    println("\nМинимальное значение f* = ${lastStep3.functionValue}")
    println("Достигается в точке x* = ${lastStep3.vertex}")
}
