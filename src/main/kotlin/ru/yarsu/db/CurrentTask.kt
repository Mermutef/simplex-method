package ru.yarsu.db

import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.TaskType
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexTable
import ru.yarsu.domain.simplex.SyntheticBasis

class CurrentTask(
    val method: Method,
    val matrix: Matrix,
    val function: Function,
    val taskType: TaskType,
) {
    val simplexTables = mutableListOf<SimplexTable>()
    val replaces = mutableMapOf<Int, Pair<Int, Int>>()
    val syntheticBasis: SyntheticBasis?

    init {
        when (method) {
            Method.SIMPLEX_METHOD -> {
                syntheticBasis = null
                simplexTables.add(
                    SimplexTable(
                        matrix = matrix,
                        function = function,
                        taskType = taskType,
                    ),
                )
            }

            Method.SYNTHETIC_BASIS -> {
                syntheticBasis =
                    SyntheticBasis(
                        matrix = matrix,
                        function = function,
                    )
                simplexTables.add(syntheticBasis.startTable)
            }
        }
    }

    fun solve() {
        while (true) {
            println("/-/-/-/-/-/-/-/-/")
            println(simplexTables.last().function.inBasisOf(simplexTables.last().matrix, taskType).coefficients)
            val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
            println(possibleValues)
            replaces[simplexTables.size] = possibleValues
            simplexTables.add(simplexTables.last() changeBasisBy possibleValues)
            println(simplexTables.last())
        }
    }

    fun extractSolution(): Pair<String, String> {
        val lastStep = simplexTables.last()
        return Pair(lastStep.functionValue.toString(), lastStep.vertex.toString())
    }
}

/*
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
 */
