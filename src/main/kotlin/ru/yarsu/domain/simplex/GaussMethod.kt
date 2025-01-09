package ru.yarsu.domain.simplex

import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.Matrix.Companion.div
import ru.yarsu.domain.entities.Matrix.Companion.minus
import ru.yarsu.domain.entities.Matrix.Companion.times

object GaussMethod {
    /**
     * Прямой ход метода Гаусса
     *
     * @return новую матрицу, приведенную к верхне диагональному виду относительно базисных переменных
     */
    fun Matrix.straightRunning(): Matrix {
        val coefficients = this.coefficients.copyOf()
        coefficients.sortBy { it.countLeadZeros() }
        for (i in 0..<m) {
            coefficients.sortBy { it.countLeadZeros() }
            if (coefficients[i][i] != Fraction.from(0)) {
                coefficients[i] = (coefficients[i] / coefficients[i][i])
                for (j in i + 1..<m) {
                    coefficients[j] = (coefficients[j] - (coefficients[i] * coefficients[j][i]))
                }
            }
        }
        return Matrix(
            n = n,
            m = m,
            coefficients = coefficients,
            basis = basis,
            free = free,
        )
    }

    /**
     * Обратный ход метода Гаусса
     *
     * @return новую матрицу, приведенную к нижне диагональному виду относительно базисных переменных
     */
    fun Matrix.reverseRunning(): Matrix {
        val coefficients = this.coefficients.copyOf()
        for (i in m - 1 downTo 0) {
            if (coefficients[i][i] != Fraction.from(0)) {
                coefficients[i] = (coefficients[i] / coefficients[i][i])
                for (j in i - 1 downTo 0) {
                    coefficients[j] = (coefficients[j] - (coefficients[i] * coefficients[j][i]))
                }
            }
        }
        return Matrix(
            n = n,
            m = m,
            coefficients = coefficients,
            basis = basis,
            free = free,
        )
    }

    fun Matrix.solveGauss(
        withBasis: List<Int>? = null,
        withFree: List<Int>? = null,
    ): Matrix {
        val newBasis = withBasis ?: (0..<m).toList()
        val newFree = withFree ?: (0..<n - 1).filter { it !in newBasis }
        return this.inBasis(
            newBasis = newBasis,
            newFree = newFree,
        ).straightRunning()
            .reverseRunning()
    }

    private fun Array<Fraction>.countLeadZeros(): Long {
        var leadZeros = 0L
        for (cell in this) {
            if (cell.equals(0L)) leadZeros++ else break
        }
        return leadZeros
    }
}
