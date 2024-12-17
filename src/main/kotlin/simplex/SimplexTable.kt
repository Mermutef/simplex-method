package ru.yarsu.methods

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
                values.add(Pair(i, matrix.coefficients[iIdx][matrix.constantIdx]))
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
        get() = -function.inBasis(matrix).coefficients.last()

    /**
     * Очередной шаг симплекс-метода
     *
     * @param s индекс переменной, которая вводится в базис
     * @param r индекс переменной, которая выводится из базиса
     *
     * @return новую симплекс-таблицу - результат очередного шага симплекс-метода
     */
    operator fun invoke(s: Int, r: Int): SimplexTable {
        // порядковый номер переменной, вводимой в базис
        val sIdx = matrix.fullIndices.indexOf(s)
        // порядковый номер переменной, выводимой из базиса
        val rIdx = matrix.fullIndices.indexOf(r)
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
    fun possibleReplaces(): List<Pair<Int, Int>>? {
        val functionInBasis = function.inBasis(matrix)
        val possibleS = matrix.free.filter { idx ->
            functionInBasis.coefficients[idx] < 0
        }
        val fractions = mutableListOf<Triple<Fraction, Int, Int>>()
        for (s in possibleS) {
            for (r in matrix.basis) {
                if (matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.fullIndices.indexOf(s)] > 0) {
                    fractions.add(
                        Triple(
                            matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.n - 1] /
                                    matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.fullIndices.indexOf(s)],
                            s,
                            r,
                        )
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

    override fun toString(): String {
        return "${function.inBasis(matrix)}\n$matrix"
    }
}

