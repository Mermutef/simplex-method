package ru.yarsu.db

import com.fasterxml.jackson.annotation.JsonIgnore
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
    @JsonIgnore
    val syntheticSimplexTables = mutableListOf<SimplexTable>()

    @JsonIgnore
    val simplexTables = mutableListOf<SimplexTable>()

    @JsonIgnore
    val syntheticReplaces = mutableMapOf<Int, Pair<Int, Int>>()

    @JsonIgnore
    val simplexReplaces = mutableMapOf<Int, Pair<Int, Int>>()

    @JsonIgnore
    val syntheticBasisTable: SyntheticBasis?

    init {
        when (method) {
            Method.SIMPLEX_METHOD -> {
                syntheticBasisTable = null
                simplexTables.add(
                    SimplexTable(
                        matrix = matrix,
                        function = function,
                        taskType = taskType,
                    ),
                )
            }

            Method.SYNTHETIC_BASIS -> {
                syntheticBasisTable =
                    SyntheticBasis(
                        matrix = matrix,
                        function = function,
                        taskType = taskType,
                    )
                syntheticSimplexTables.add(syntheticBasisTable.startTable)
            }
        }
    }

    fun solve(stepByStep: Boolean = false) {
        if (stepByStep) {
            doStep()
        }
        if (method == Method.SYNTHETIC_BASIS) {
            while (true) {
                println("До")
                println(syntheticSimplexTables.last())
                val possibleValues = syntheticSimplexTables.last().possibleReplaces()?.first() ?: break
                syntheticReplaces[syntheticReplaces.size] = possibleValues
                syntheticSimplexTables.add(syntheticSimplexTables.last() changeBasisBy possibleValues)
                println("После")
                println(syntheticSimplexTables.last())
            }
//            simplexTables.add(syntheticBasisTable!! extractSolutionFrom syntheticSimplexTables.last())
        }
//        while (true) {
//            val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
//            simplexReplaces[simplexReplaces.size] = possibleValues
//            simplexTables.add(simplexTables.last() changeBasisBy possibleValues)
//        }
    }

    fun doStep() {
        var doSimplexStep: Boolean = true
        if (method == Method.SYNTHETIC_BASIS) {
            syntheticSimplexTables.last().possibleReplaces()?.first()?.let { possibleValues ->
                syntheticReplaces[syntheticReplaces.size] = possibleValues
                syntheticSimplexTables.add(syntheticSimplexTables.last() changeBasisBy possibleValues)
                doSimplexStep = false
            } ?: simplexTables.add(syntheticBasisTable!! extractSolutionFrom syntheticSimplexTables.last())
        }
        while (true) {
            val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
            simplexReplaces[simplexReplaces.size] = possibleValues
            simplexTables.add(simplexTables.last() changeBasisBy possibleValues)
        }
    }

    fun extractSolution(): Pair<String, String> {
        val lastStep = simplexTables.last()
        return Pair(lastStep.functionValue.toString(), lastStep.vertex.toString())
    }

    override fun toString(): String {
        return """
            $taskType
            $method
            $function
            $matrix
            """.trimIndent()
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
