package ru.yarsu.simplex

import ru.yarsu.entities.Fraction
import ru.yarsu.entities.Function
import ru.yarsu.entities.Matrix
import ru.yarsu.entities.Matrix.Companion.swap

class SimplexTable(
    val matrix: Matrix,
    val function: Function,
) {
    /**
     * Координаты текущей вершины
     */
    val vertex: List<Fraction>
        get() {
            val values = mutableListOf<Pair<Int, Fraction>>()
            for (i in matrix.basis) {
                val iIdx = matrix.basis.indexOf(i)
                values.add(Pair(i, matrix.coefficients[iIdx][matrix.bIdx]))
            }
            for (i in matrix.free) {
                values.add(Pair(i, Fraction(0)))
            }

            return values.sortedBy { it.first }.map { it.second }
        }

    /**
     * Значение функции в текущей вершине
     */
    val functionValue: Fraction
        get() = -function.inBasisOf(matrix).coefficients.last()

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
        val newMatrix = matrix.inBasis(newBasis, newFree).straightRunning().reverseRunning()

        return SimplexTable(
            matrix = newMatrix,
            function = function,
        )
    }

    /**
     * Список координат возможных опорных элементов для шага
     *
     * @return непустой список координат возможных опорных элементов, null иначе
     */
    fun possibleReplaces(forIdleRunning: Boolean = false): List<Pair<Int, Int>>? {
        if (forIdleRunning) return idleRunningReplaces()

        val functionInBasis = function.inBasisOf(matrix)

        val possibleS =
            matrix.free.filter { idx ->
                functionInBasis.coefficients[idx] < 0
            }
        val fractions = mutableListOf<Triple<Fraction, Int, Int>>()

        for (s in possibleS) {
            val sIdx = matrix.fullIndices.indexOf(s)
            for (r in matrix.basis) {
                val rIdx = matrix.fullIndices.indexOf(r)
                if (matrix.coefficients[rIdx][sIdx] > 0) {
                    fractions.add(
                        Triple(
                            matrix.coefficients[rIdx][matrix.bIdx] / matrix.coefficients[rIdx][sIdx],
                            s,
                            r,
                        ),
                    )
                }
            }
        }

        return fractions
            .minByOrNull { it.first }
            ?.let { minValue -> fractions.filter { it.first == minValue.first } }
            ?.map { Pair(it.second, it.third) }
            ?.takeIf { it.isNotEmpty() }
    }

    /**
     * Координаты для выполнения холостого хода симплекс-метода
     *
     * @return пара (s, r) - индексы вводимой "натуральной" переменной и выводимой искусственной переменной
     */
    private fun idleRunningReplaces(): List<Pair<Int, Int>>? {
        val functionInBasis = function.inBasisOf(matrix)

        val possibleS = matrix.free.filter { functionInBasis.coefficients[it] == Fraction(0) }

        for (s in possibleS) {
            val sIdx = matrix.fullIndices.indexOf(s)
            for (r in matrix.basis) {
                val rIdx = matrix.fullIndices.indexOf(r)
                if (matrix.coefficients[rIdx][sIdx] != Fraction(0) &&
                    matrix.coefficients[rIdx][matrix.bIdx] == Fraction(0)
                ) {
                    return listOf(Pair(s, r))
                }
            }
        }

        return null
    }

    override fun toString(): String {
        return "${function.inBasisOf(matrix)}\n$matrix"
    }
}
