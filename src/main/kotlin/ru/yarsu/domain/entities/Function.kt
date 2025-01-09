package ru.yarsu.domain.entities

import ru.yarsu.domain.entities.Matrix.Companion.plus
import ru.yarsu.domain.entities.Matrix.Companion.times

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
    fun inBasisOf(
        matrix: Matrix,
        taskType: TaskType,
        doTransform: Boolean = true,
        invertLast: Boolean = false,
    ): Function {
        val newCoefficients = mutableListOf<Fraction>()
        coefficients.forEachIndexed { _, _ -> newCoefficients.addLast(Fraction.from(0)) }
        matrix.coefficients.mapIndexed { rowIdx, row -> row * coefficients[matrix.basis[rowIdx]] }
            .reduce { row1, row2 -> row1 + row2 }
            .forEachIndexed { idx, pi ->
                when (val xi = (matrix.fullIndices[idx])) {
                    in matrix.free -> newCoefficients[xi] = (coefficients[xi] - pi)
                    in listOf(matrix.bIdx) -> newCoefficients[xi] = (coefficients[xi] + pi)
                    else -> {}
                }
            }

        if (invertLast) newCoefficients[newCoefficients.lastIndex] = -newCoefficients.last()
        return Function(if (taskType == TaskType.MAX && doTransform) newCoefficients.map { it * -1 } else newCoefficients)
    }

    override fun toString(): String {
        val n = coefficients.size
        var res = ""
        var isFirst = true
        coefficients.forEachIndexed { i, pi ->
            res +=
                when {
                    pi != Fraction.from(0) -> {
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
        return res.trim()
    }
}

enum class TaskType {
    MAX,
    MIN,
}
