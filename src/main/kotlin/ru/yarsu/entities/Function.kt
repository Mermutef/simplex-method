package ru.yarsu.entities

import ru.yarsu.entities.Matrix.Companion.plus
import ru.yarsu.entities.Matrix.Companion.times

class Function(
    val coefficients: List<Fraction>,
) {
    /**
     * Выполняет подстановку выражений базисных переменных через свободные матрицы ограничений
     *
     * @param matrix матрица в диагональный форме
     *
     * @return функция с коэффициентами, выраженными через свободные переменные матрицы
     */
    fun inBasisOf(matrix: Matrix): Function {
        val newCoefficients = mutableListOf<Fraction>()
        coefficients.forEachIndexed { _, _ -> newCoefficients.addLast(Fraction(0)) }
        matrix.coefficients.mapIndexed { rowIdx, row -> row * coefficients[matrix.basis[rowIdx]] }
            .reduce { row1, row2 -> row1 + row2 }
            .forEachIndexed { idx, pi ->
                when (val xi = (matrix.fullIndices[idx])) {
                    in matrix.free -> newCoefficients[xi] = coefficients[xi] - pi
                    in listOf(matrix.bIdx) -> newCoefficients += coefficients[xi] + pi
                    else -> {}
                }
            }
        return Function(newCoefficients)
    }

    override fun toString(): String {
        val n = coefficients.size
        var res = ""
        var isFirst = true
        coefficients.forEachIndexed { i, pi ->
            res +=
                when {
                    pi != Fraction(0) -> {
                        if (isFirst) {
                            isFirst = false
                            if (pi < 0) "-" else ""
                        } else {
                            if (pi < 0) "- " else "+ "
                        } + "${if (!pi.abs().equals(1)) "${pi.abs()}" else ""}${if (i != n - 1) "x${i + 1}" else ""} "
                    }

                    else -> ""
                }
        }
        return "$res-> min"
    }
}
