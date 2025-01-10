package ru.yarsu.domain.entities

import com.fasterxml.jackson.annotation.JsonIgnore

data class Matrix(
    val n: Int,
    val m: Int,
    val coefficients: Array<Array<Fraction>>,
    val basis: List<Int>,
    val free: List<Int>,
) {
    /**
     * Индекс столбца свободного члена
     */
    @get:JsonIgnore
    val bIdx: Int
        get() = n - 1

    /**
     * Список индексов переменных (базисные + свободные + свободный член)
     */
    @get:JsonIgnore
    val fullIndices: List<Int>
        get() = basis + free + listOf(bIdx)

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
            "Все строки матрицы должны содержать ровно n=$n коэффициентов."
        }

        require(coefficients.size == m) {
            "Матрица должна содержать ровно m=$m строк."
        }

        require(basis.size == m) {
            "Число базисных переменных должно совпадать с числом строк (передано ${basis.size}, ожидалось $m)."
        }

        require(free.size == n - m - 1) {
            "Число свободных переменных должно быть равно n-1-m (передано ${free.size}, ожидалось ${n - m - 1})."
        }

        require(basis.all { it < n }) {
            "Индексы базисных переменных должны быть целыми числами из полуинтервала [0; $n)."
        }
        require(free.all { it < n }) {
            "Индексы свободных переменных должны быть целыми числами из полуинтервала [0; $n)."
        }
    }

    companion object {
        operator fun List<Fraction>.times(coff: Fraction): List<Fraction> = this.map { it * coff }

        operator fun List<Fraction>.div(coff: Fraction): List<Fraction> = this.map { it / coff }

        operator fun List<Fraction>.plus(other: Array<Fraction>): List<Fraction> = this.zip(other).map { (a, b) -> a + b }

        operator fun List<Fraction>.minus(other: Array<Fraction>): List<Fraction> = this.zip(other).map { (a, b) -> a - b }

        operator fun List<Fraction>.unaryMinus(): List<Fraction> = this * Fraction.from(-1)

        operator fun Array<Fraction>.times(coff: Fraction): Array<Fraction> =
            this
                .map { it * coff }
                .toTypedArray()

        operator fun Array<Fraction>.div(coff: Fraction): Array<Fraction> =
            this
                .map { it / coff }
                .toTypedArray()

        operator fun Array<Fraction>.plus(other: Array<Fraction>): Array<Fraction> =
            this
                .zip(other)
                .map { (a, b) -> a + b }
                .toTypedArray()

        operator fun Array<Fraction>.minus(other: Array<Fraction>): Array<Fraction> =
            this
                .zip(other)
                .map { (a, b) -> a - b }
                .toTypedArray()

        operator fun Array<Fraction>.unaryMinus(): Array<Fraction> = this * Fraction.from(-1)
    }

    /**
     * Перестановка столбцов матрицы согласно новому порядку базисных и свободных переменных
     *
     * @param newBasis новый порядок базисных переменных
     * @param newFree новый порядок свободных переменных
     *
     * @return новую матрицу со столбцами, переставленными согласно новому порядку следования переменных
     */
    fun inBasis(
        newBasis: List<Int>,
        newFree: List<Int>? = null,
    ): Matrix {
        val defaultFree = (0..<n - 1).filter { it !in newBasis }
        val newCoefficients = mutableListOf<MutableList<Fraction>>()
        val coefficientsIndices = newBasis + (newFree ?: defaultFree) + listOf(bIdx)
        coefficients.forEach { row ->
            val rowInAnotherBasis = mutableListOf<Fraction>()
            coefficientsIndices.forEach {
                val t = fullIndices.indexOf(it)
                rowInAnotherBasis.add(row[t])
            }
            newCoefficients.add(rowInAnotherBasis)
        }

        return Matrix(
            n = n,
            m = m,
            coefficients = newCoefficients,
            basis = newBasis,
            free = newFree ?: defaultFree,
        )
    }

    override fun toString(): String {
        val maxColLength = coefficients.flatten().maxBy { it.toString().length }.toString().length + 2
        return (
            listOf(
                fullIndices.map { basisIdx -> if (basisIdx + 1 != n) "x${basisIdx + 1}" else "b" },
            ) +
                coefficients.map { row -> row.map { coff -> coff.toString() } }
        ).joinToString("\n") {
            it.joinToString("  ") { x -> "${" ".repeat(maxColLength - x.length)}$x" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (!coefficients.contentDeepEquals(other.coefficients)) return false
        if (basis != other.basis) return false
        if (free != other.free) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coefficients.contentDeepHashCode()
        result = 31 * result + basis.hashCode()
        result = 31 * result + free.hashCode()
        return result
    }
}
