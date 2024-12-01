package ru.yarsu

data class Matrix(
    val n: Int,
    val m: Int,
    val coefficients: Array<Array<Fraction>>,
    val basis: List<Int>,
) {
    constructor(
        n: Int,
        m: Int,
        coefficients: List<List<Fraction>>,
        basis: List<Int>,
    ) : this(
        n = n,
        m = m,
        coefficients = coefficients.map { it.toTypedArray() }.toTypedArray(),
        basis = basis,
    )

    init {
        require(coefficients.all { it.size == n }) {
            "Все строки матрицы должны содержать ровно n=$n коэффициентов"
        }

        require(coefficients.size == m) {
            "Матрица должна содержать ровно m=$m строк"
        }

        require(basis.size == m) {
            "Число базисных переменных должно быть ровно m=$m"
        }

        require(basis.all { it < n }) {
            "Индексы базисных переменных должны быть целыми числами из полуинтервала [0; $n)."
        }
    }

    fun inBasis(
        newBasis: List<Int>,
    ): Matrix {
        val coefs = coefficients.clone()
        val coefficientsIndices = newBasis.toMutableList()
        for (i in 0..<n) {
            if (i !in coefficientsIndices) {
                coefficientsIndices.add(i)
            }
        }
        for (i in 0..<m) {
            for (j in 0..<n) {
                coefficients[i][j] = coefficients[i][coefficientsIndices[j]]
            }
        }

        return Matrix(
            n = n,
            m = m,
            coefficients = coefs,
            basis = newBasis,
        )
    }

    fun straightRunning(): Matrix {
        val coefficients = this.coefficients.clone()
        coefficients.sortBy { it.countLeadZeros() }
        for (i in 0..<m) {
            coefficients.sortBy { it.countLeadZeros() }
            if (!coefficients[i][i].equals(0)) {
                coefficients[i] = (coefficients[i] / coefficients[i][i])
                for (j in i + 1..<m) {
                    coefficients[j] =
                        (coefficients[j] - (coefficients[i] * coefficients[j][i]))
                }
            }
        }
        return Matrix(
            n = n,
            m = m,
            coefficients = coefficients,
            basis = basis,
        )
    }

    fun reverseRunning(): Matrix {
        val coefficients = this.coefficients.clone()
        for (i in m - 1 downTo 0) {
            if (!coefficients[i][i].equals(0)) {
                coefficients[i] = (coefficients[i] / coefficients[i][i])
                for (j in i - 1 downTo 0) {
                    coefficients[j] =
                        (coefficients[j] - (coefficients[i] * coefficients[j][i]))
                }
            }
        }
        return Matrix(
            n = n,
            m = m,
            coefficients = coefficients,
            basis = basis,
        )
    }

    override fun toString(): String {
        val maxColLength = coefficients.flatten().maxBy { it.toString().length }.toString().length
        return coefficients.joinToString("\n") {
            it.joinToString("  ") { x -> "${" ".repeat(maxColLength - "$x".length)}$x" }
        }
    }

    private fun Array<Fraction>.countLeadZeros(): Long {
        var leadZeros = 0L
        for (cell in this) {
            if (cell.equals(0L)) leadZeros++ else break
        }
        return leadZeros
    }
}

operator fun Array<Fraction>.times(coff: Fraction): Array<Fraction> = this.map { it * coff }.toTypedArray()
operator fun Array<Fraction>.div(coff: Fraction): Array<Fraction> = this.map { it / coff }.toTypedArray()
operator fun Array<Fraction>.minus(other: Array<Fraction>): Array<Fraction> =
    this.zip(other).map { (a, b) -> a - b }.toTypedArray()