package ru.yarsu.entities

class Function(val coefficients: List<Fraction>, n: Int) {
    // проверка при инициализации
    init {
        require(coefficients.size == n) {
            error("Матрица ограничений неполна. Размерность задачи n=${coefficients.size}, размерность ограничений n=$n")
        }
    }

    /**
     * Выполняет подстановку выражений базисных переменных через свободные матрицы ограничений
     *
     * @param matrix матрица в диагональный форме
     *
     * @return функция с коэффициентами, выраженными через свободные переменные матрицы
     */
    fun inBasis(matrix: Matrix): Function {
        val allIndices = matrix.fullIndices
        val newCoefficients = mutableListOf<Fraction>()
        for (i in 0..<matrix.n - 1) {
            if (i !in matrix.basis) {
                newCoefficients.add(coefficients[i] + matrix.coefficients.mapIndexed { k, rowK ->
                    -rowK[allIndices.indexOf(i)] * coefficients[matrix.basis[k]]
                }.reduce(Fraction::plus))
            } else {
                newCoefficients.add(Fraction(0))
            }
        }
        newCoefficients.add(coefficients[matrix.n - 1] + matrix.coefficients.mapIndexed { k, rowK ->
            rowK[allIndices.indexOf(matrix.n - 1)] * coefficients[matrix.basis[k]]
        }.reduce(Fraction::plus))
        return Function(newCoefficients, matrix.n)
    }

    override fun toString(): String {
        val n = coefficients.size
        var res = ""
        var isFirst = true
        for (i in 0..<n) {
            val pi = coefficients[i]
            res += when {
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