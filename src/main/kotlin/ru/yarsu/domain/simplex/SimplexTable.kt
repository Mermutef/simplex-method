package ru.yarsu.domain.simplex

import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.TaskType
import ru.yarsu.domain.simplex.GaussMethod.solveGauss

class SimplexTable(
    val matrix: Matrix,
    val function: Function,
    val taskType: TaskType = TaskType.MIN,
) {
    /**
     * Очередной шаг симплекс-метода
     *
     * @param inOutPair индексы переменных, которые вводится в базис и выводятся из базиса соответственно
     *
     * @return новую симплекс-таблицу - результат очередного шага симплекс-метода
     */
    infix fun changeBasisBy(inOutPair: Pair<Int, Int>): SimplexTable {
        // порядковый номер переменной, вводимой в базис
        val sIdx = matrix.fullIndices.indexOf(inOutPair.first)
        // порядковый номер переменной, выводимой из базиса
        val rIdx = matrix.fullIndices.indexOf(inOutPair.second)
        // новый порядок переменных

        val newVariablesIndices = matrix.fullIndices.swap(rIdx, sIdx)
        val newBasis = newVariablesIndices.slice(0..<matrix.m)
        val newFree = newVariablesIndices.slice(matrix.m..<matrix.n - 1)
        // приведение матрицы к диагональному виду в новом базисе (шаг симплекс метода)
        val newMatrix = matrix.solveGauss(withBasis = newBasis, withFree = newFree)

        return SimplexTable(
            matrix = newMatrix,
            function = function,
            taskType = taskType,
        )
    }

    /**
     * Список координат возможных опорных элементов для шага
     *
     * @return список координат возможных опорных элементов
     */
    fun possibleReplaces(): List<Pair<Int, Int>> {
        val functionInBasis = function.inBasisOf(matrix = matrix, taskType = taskType)
        val possibleReplaces = mutableListOf<Pair<Int, Int>>()
        matrix.free.filter { idx ->
            functionInBasis.coefficients[idx] < 0
        }.forEach { s ->
            val sIdx = matrix.fullIndices.indexOf(s)
            val candidates = mutableListOf<Triple<Fraction, Int, Int>>()
            matrix.basis.forEach { r ->
                val rIdx = matrix.fullIndices.indexOf(r)
                if (matrix.coefficients[rIdx][sIdx] > 0 && matrix.coefficients[rIdx][matrix.bIdx] >= 0) {
                    candidates.add(
                        Triple(
                            matrix.coefficients[rIdx][matrix.bIdx] / matrix.coefficients[rIdx][sIdx],
                            s,
                            r,
                        ),
                    )
                }
            }

            candidates.minByOrNull { it.first }?.let { bestCandidate ->
                possibleReplaces.addAll(
                    candidates.filter { it.first == bestCandidate.first }
                        .map { Pair(it.second, it.third) },
                )
            }
        }

        return possibleReplaces
    }

    /**
     * Координаты для выполнения холостого хода симплекс-метода
     *
     * @return пара (s, r) - индексы вводимой "натуральной" переменной и выводимой искусственной переменной
     */
    fun possibleIdleRunningReplaces(syntheticVariables: List<Int>): List<Pair<Int, Int>> {
        val possibleReplaces = mutableListOf<Pair<Int, Int>>()
        matrix.free.filter { idx ->
            idx !in syntheticVariables
        }.forEach { s ->
            val sIdx = matrix.fullIndices.indexOf(s)
            syntheticVariables.filter { it in matrix.basis }.forEach { r ->
                val rIdx = matrix.fullIndices.indexOf(r)
                if (matrix.coefficients[rIdx][sIdx] != Fraction.from(0) &&
                    matrix.coefficients[rIdx].last() == Fraction.from(0)
                ) {
                    possibleReplaces.add(Pair(s, r))
                }
            }
        }

        return possibleReplaces
    }

    override fun toString(): String {
        return "${function.inBasisOf(matrix, taskType, false)} -> ${taskType.toString().lowercase()}\n$matrix"
    }

    private fun <T> List<T>.swap(
        i: Int,
        j: Int,
    ): List<T> {
        val iItem = this[i]
        val jItem = this[j]
        val newList = mutableListOf<T>()
        newList.addAll(this)
        newList[i] = jItem
        newList[j] = iItem
        return newList
    }
}

enum class Method {
    SIMPLEX_METHOD,
    SYNTHETIC_BASIS,
}
