package ru.yarsu.simplex

import ru.yarsu.entities.Fraction
import ru.yarsu.entities.Matrix
import ru.yarsu.entities.Function
import ru.yarsu.entities.Matrix.Companion.unaryMinus

class SyntheticBasis(
    val matrix: Matrix,
    val function: Function,
) {
    /**
     * Список индексов искусственных базисных переменных
     */
    val syntheticBasis: List<Int>
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
    val free: List<Int>
        get() = matrix.basis + matrix.free

    /**
     * Начальная симплекс-таблица метода искусственного базиса
     */
    val startTable: SimplexTable
        get() {
            val extendedFunctionCoefficients = function.coefficients.map { Fraction(0) }.toMutableList()
            extendedFunctionCoefficients.removeLast()
            val extendedMatrixCoefficients = mutableListOf<MutableList<Fraction>>()
            matrix.basis.forEachIndexed { i, _ ->
                extendedFunctionCoefficients.addLast(Fraction(1))
                val newRow = mutableListOf<Fraction>()
                matrix.basis.forEachIndexed { j, _ ->
                    newRow.add(if (i == j) Fraction(1) else Fraction(0))
                }
                val matrixRow = matrix.coefficients[i]
                val correctMatrixRow = if (matrixRow[matrix.bIdx] > 0) matrixRow else -matrixRow
                extendedMatrixCoefficients.add((newRow + correctMatrixRow).toMutableList())
            }
            extendedFunctionCoefficients.addLast(Fraction(0))

            return SimplexTable(
                matrix = Matrix(
                    coefficients = extendedMatrixCoefficients,
                    n = matrix.n + matrix.m,
                    m = matrix.m,
                    basis = syntheticBasis,
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
    // todo холостой ход
    infix fun extractSolutionFrom(lastTable: SimplexTable): SimplexTable {
        if (lastTable
                .function
                .coefficients
                .filterIndexed { idx, _ -> idx in (lastTable.matrix.free - syntheticBasis.toSet()) }
                .any { it != Fraction(0) } ||
            lastTable.function.coefficients[lastTable.matrix.bIdx] != Fraction(0)
        ) {
            error("Невозможно построить начальную симплекс таблицу.")
        }

        return SimplexTable(
            matrix = Matrix(
                coefficients = lastTable.matrix.coefficients.map { row ->
                    row.filterIndexed { j, _ ->
                        lastTable.matrix.fullIndices[j] in (free + lastTable.matrix.bIdx)
                    }
                },
                basis = lastTable.matrix.basis,
                free = lastTable.matrix.free.filter { it !in syntheticBasis },
                n = matrix.n,
                m = matrix.m,
            ),
            function = function,
        )
    }
}