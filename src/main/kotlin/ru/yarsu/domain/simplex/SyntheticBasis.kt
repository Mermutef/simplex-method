package ru.yarsu.domain.simplex

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.Matrix.Companion.unaryMinus
import ru.yarsu.domain.entities.TaskType

class SyntheticBasis(
    val matrix: Matrix,
    val function: Function,
    val taskType: TaskType,
) {
    /**
     * Список индексов искусственных базисных переменных
     */
    @get:JsonIgnore
    val basis: List<Int>
        get() {
            val syntheticBasisMutable = mutableListOf<Int>()
            matrix.basis.forEachIndexed { i, _ ->
                syntheticBasisMutable.add(matrix.n + i - 1)
            }
            return syntheticBasisMutable
        }

    /**
     * Список индексов свободных переменных
     */
    @get:JsonIgnore
    val free: List<Int>
        get() = matrix.basis + matrix.free

    /**
     * Начальная симплекс-таблица метода искусственного базиса
     */
    @get:JsonIgnore
    val startTable: SimplexTable
        get() {
            val extendedFunctionCoefficients = function.coefficients.map { Fraction.from(0) }.toMutableList()
            extendedFunctionCoefficients.removeLast()
            val extendedMatrixCoefficients = mutableListOf<MutableList<Fraction>>()
            matrix.basis.forEachIndexed { i, _ ->
                extendedFunctionCoefficients.addLast(Fraction.from(1))
                val newRow = mutableListOf<Fraction>()
                matrix.basis.forEachIndexed { j, _ ->
                    newRow.add(if (i == j) Fraction.from(1) else Fraction.from(0))
                }
                val matrixRow = matrix.coefficients[i]
                val correctMatrixRow = if (matrixRow[matrix.bIdx] > 0) matrixRow else -matrixRow
                extendedMatrixCoefficients.add((newRow + correctMatrixRow).toMutableList())
            }
            extendedFunctionCoefficients.addLast(Fraction.from(0))

            return SimplexTable(
                matrix =
                    Matrix(
                        coefficients = extendedMatrixCoefficients,
                        n = matrix.n + matrix.m,
                        m = matrix.m,
                        basis = basis,
                        free = free,
                    ),
                function = Function(coefficients = extendedFunctionCoefficients),
            )
        }

    /**
     * Получение начальной симплекс таблицы для классического симплекс-метода
     *
     * @return начальную симплекс-таблицу исходной задачи
     */
    infix fun extractSolutionFrom(lastTable: SimplexTable): SimplexTable {
        val functionInBasis = function.inBasisOf(lastTable.matrix, taskType)
        require(functionInBasis.coefficients.filterIndexed { idx, _ -> idx in free }.all { it == Fraction.from(0) }) {
            "Конечная симплекс-таблица искусственного базиса не имеет оптимального решения"
        }
        require(functionInBasis.coefficients.last() == Fraction.from(0)) {
            "Конечная симплекс-таблица искусственного базиса не имеет оптимального решения"
        }
        require(lastTable.matrix.basis.filter { lastTable.vertex[it] != Fraction.from(0) }.all { it in free }) {
            "Невозможно вывести искусственную переменную из базиса и она не равна нулю"
        }

//        val needlessMatrixRows

        return SimplexTable(
            matrix =
                Matrix(
                    coefficients =
                        lastTable.matrix.coefficients.map { row ->
                            row.filterIndexed { j, _ ->
                                lastTable.matrix.fullIndices[j] in (free + lastTable.matrix.bIdx)
                            }
                        },
                    basis = lastTable.matrix.basis,
                    free = lastTable.matrix.free.filter { it !in basis },
                    n = matrix.n,
                    m = matrix.m,
                ),
            function = function,
            taskType = taskType,
        )
    }
}
