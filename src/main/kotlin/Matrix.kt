package ru.yarsu

data class Matrix(
    val n: Int,
    val m: Int,
    val coefficients: Array<Array<Fraction>>,
    val basis: List<Int>,
    val free: List<Int>,
) {
    constructor(
        n: Int,
        m: Int,
        coefficients: List<List<Fraction>>,
        basis: List<Int>,
        free: List<Int>,
    ) : this(
        n = n,
        m = m,
        coefficients = coefficients.map { it.toTypedArray() }.toTypedArray(),
        basis = basis,
        free = free,
    )

    init {
        require(coefficients.all { it.size == n }) {
            "Все строки матрицы должны содержать ровно n=$n коэффициентов"
        }

        require(coefficients.size == m) {
            "Матрица должна содержать ровно m=$m строк"
        }

        require(basis.size == m) {
            "Число базисных переменных должно быть равно m=$m"
        }

        require(free.size == n - m - 1) {
            "Число свободных переменных должно быть равно n-m=${n - m - 1} (${free.size})"
        }

        require(basis.all { it < n }) {
            "Индексы базисных переменных должны быть целыми числами из полуинтервала [0; $n)."
        }
        require(free.all { it < n }) {
            "Индексы свободных переменных должны быть целыми числами из полуинтервала [0; $n)."
        }
    }

    val constantIdx = n - 1
    val fullIndices = basis + free + listOf(constantIdx)

    fun inBasis(
        newBasis: List<Int>,
        newFree: List<Int>,
    ): Matrix {
        val newCoefficients = mutableListOf<MutableList<Fraction>>()
        val coefficientsIndices = newBasis + newFree + listOf(constantIdx)
        for (i in 0..<m) {
            val row = mutableListOf<Fraction>()
            for (j in fullIndices) {
                row.add(coefficients[i][coefficientsIndices.indexOf(j)])
            }
            newCoefficients.add(row)
        }

        return Matrix(
            n = n,
            m = m,
            coefficients = newCoefficients,
            basis = newBasis,
            free = newFree,
        )
    }

    fun straightRunning(): Matrix {
        val coefficients = this.coefficients.copyOf()
        coefficients.sortBy { it.countLeadZeros() }
        for (i in 0..<m) {
            coefficients.sortBy { it.countLeadZeros() }
            if (!coefficients[i][i].equals(0)) {
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

    fun reverseRunning(): Matrix {
        val coefficients = this.coefficients.copyOf()
        for (i in m - 1 downTo 0) {
            if (!coefficients[i][i].equals(0)) {
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

    override fun toString(): String {
        val maxColLength = coefficients.flatten().maxBy { it.toString().length }.toString().length
        return (listOf(
            fullIndices.map { basisIdx -> if (basisIdx + 1 != n) "x${basisIdx + 1}" else "b" })
                + coefficients.map { row -> row.map { coff -> coff.toString() } }).joinToString("\n") {
            it.joinToString("  ") { x -> "${" ".repeat(maxColLength - x.length)}$x" }
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

fun List<Int>.swap(i: Int, j: Int): List<Int> {
    val iItem = this[i]
    val jItem = this[j]
    val newList = mutableListOf<Int>()
    newList.addAll(this)
    newList[i] = jItem
    newList[j] = iItem
    return newList
}