package ru.yarsu

import ru.yarsu.entities.Fraction.Companion.toFractionOrNull
import ru.yarsu.entities.Function
import ru.yarsu.entities.Matrix
import ru.yarsu.methods.SimplexTable
import java.io.File


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
            it.toIntOrNull()?.let { idx -> idx - 1 } ?: error("Индексы базисных переменных должны быть целыми числами")
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
    val f = Function(coffs, n)

    // временный вывод
    println("Задача:\n$f")
    println(matrix)
    // прямой ход Гаусса
    val straightRunning =
        matrix.inBasis(newBasis = newBasis, newFree = (0..<n - 1).filter { it !in newBasis }).straightRunning()
    println("\nПрямой ход:\n$straightRunning")
    // обратный ход Гаусса
    val reverseRunning = straightRunning.reverseRunning()
    println("\nОбратный ход:\n${reverseRunning}")
    // временное хранилище шагов симплекс-метода
    val simplexTables = mutableListOf<SimplexTable>()
    // создание нулевого шага симплекс-метода
    simplexTables.add(SimplexTable(matrix = reverseRunning, function = f))
    println("\nНачальная симплекс-таблица:\n${simplexTables.last()}")
    // пока можем сделать шаг симплекс метода
    while (true) {
        val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
        val s = possibleValues.first
        val r = possibleValues.second
        println("\nШаг ${simplexTables.size}")
        println("Вводим в базис x${s + 1}, выводим x${r + 1}")
        simplexTables.add(simplexTables.last()(s = s, r = r))
        println("Результат шага:\n${simplexTables.last()}")
    }
    val lastStep = simplexTables.last()
    println("\nМинимальное значение f* = ${lastStep.functionValue}")
    println("Достигается в точке x* = ${lastStep.vertex}")
}
