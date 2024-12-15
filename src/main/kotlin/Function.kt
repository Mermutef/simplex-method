package ru.yarsu

class Function(val coefficients: List<Fraction>, n: Int) {
    init {
        require(coefficients.size == n) {
            error("Матрица ограничений неполна. Размерность задачи n=${coefficients.size}, размерность ограничений n=$n")
        }
    }

    override fun toString(): String {
        val n = coefficients.size - 1
        var res = ""
        var isFirst = true
        for (i in 0..n) {
            val pi = coefficients[i]
            res += when {
                pi > 0 -> {
                    if (isFirst) {
                        isFirst = false
                        ""
                    } else {
                        "+ "
                    } + "${if (!pi.equals(1)) "$pi" else ""}${if (i != n) "x${i + 1}" else ""} "
                }

                pi < 0 -> {
                    if (isFirst) {
                        isFirst = false
                        "-"
                    } else {
                        "- "
                    } + "${if (!pi.equals(-1)) "${-pi}" else ""}${if (i != n) "x${i + 1}" else ""} "
                }

                else -> ""
            }
        }
        return "$res-> min"
    }

    fun inBasis(matrix: Matrix): Function {
        val allIndices = matrix.basis + (0..<matrix.n).filter { it !in matrix.basis }
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
}