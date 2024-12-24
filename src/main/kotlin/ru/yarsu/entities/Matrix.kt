package ru.yarsu.entities

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

    // второй конструктор, чтобы каждый раз не мучиться с переводом MutableList к Array
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

    // проверки при создании экземпляра класса
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

    // переопределенные математические операторы для удобной работы со строками
    // (умножение и деление на дробь, сложение и вычитание двух векторов, ...)
    companion object {
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

        operator fun Array<Fraction>.unaryMinus() = this * Fraction.from(-1)

        /**
         * Смена двух значений списка местами.
         *
         * @return Новый список с поменянными местами i-ым и j-ым элементами
         */
        fun <T> List<T>.swap(
            i: Int,
            j: Int,
        ): List<T> {
            val iItem = this[i]
            val jItem = this[j]
            val newList = mutableListOf<T>()
            newList.addAll(this)
            newList[i] = jItem
            newList[j] = iItem
            return newList
        }

        private fun Array<Fraction>.countLeadZeros(): Long {
            var leadZeros = 0L
            for (cell in this) {
                if (cell.equals(0L)) leadZeros++ else break
            }
            return leadZeros
        }
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
        newFree: List<Int>,
    ): Matrix {
        val newCoefficients = mutableListOf<MutableList<Fraction>>()
        val coefficientsIndices = newBasis + newFree + listOf(bIdx)
        coefficients.forEachIndexed { rowIdx, _ ->
            val rowInAnotherBasis = mutableListOf<Fraction>()
            coefficientsIndices.forEach {
                val t = fullIndices.indexOf(it)
                rowInAnotherBasis.add(coefficients[rowIdx][t])
            }
            newCoefficients.add(rowInAnotherBasis)
        }

        return Matrix(
            n = n,
            m = m,
            coefficients = newCoefficients,
            basis = newBasis,
            free = newFree,
        )
    }

    /**
     * Прямой ход метода Гаусса
     *
     * @return новую матрицу, приведенную к верхне диагональному виду относительно базисных переменных
     */
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

    /**
     * Обратный ход метода Гаусса
     *
     * @return новую матрицу, приведенную к нижне диагональному виду относительно базисных переменных
     */
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
