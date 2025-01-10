package ru.yarsu.domain.entities

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

@Suppress("detekt:TooManyFunctions")
class Fraction(val numerator: Int, val denominator: Int) : Comparable<Fraction> {
    init {
        require(denominator != 0) {
            "Знаменатель не может быть 0"
        }
    }

    @Suppress("detekt:TooManyFunctions")
    // математические операции для работы с дробью как с обычным числом
    companion object {
        fun from(numerator: Int) = Fraction(numerator, 1)

        operator fun Int.div(other: Fraction): Fraction = from(this) / other

        operator fun Int.plus(other: Fraction): Fraction = from(this) + other

        operator fun Int.minus(other: Fraction): Fraction = from(this) - other

        operator fun Int.times(other: Fraction): Fraction = from(this) * other

        /**
         * Приведение строки к дроби. Строка должна иметь вид '[-]a/b',
         * или '[-]a.b', или '[-]a,b', где a и b - целые числа.
         *
         * @return дробь
         *
         * @throws IllegalArgumentException выбрасывается, если поданная на вход строка
         * не удовлетворяет описанному формату
         */
        fun String.toFraction(): Fraction {
            this.trim().replace(",", ".").toDoubleOrNull()?.toFraction()?.let { return it }
            val values =
                this
                    .split("/")
                    .map {
                        it
                            .trim()
                            .toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid fraction string")
                    }.takeIf { it.size in 1..2 }
                    ?: throw IllegalArgumentException("Invalid fraction string")

            return when (values.size) {
                1 -> from(values[0])
                else -> Fraction(values[0], values[1])
            }
        }

        /**
         * Приведение десятичного числа к дроби.
         *
         * @return дробь
         */
        fun Double.toFraction(): Fraction {
            if (this == 0.0) {
                return from(0)
            }
            val values = this.toString().split(",", ".")
            val numerator = "${values[0]}${values[1]}".toInt()
            val denominator = 10.0.pow(values[1].length).toInt()
            return Fraction(numerator, denominator).shorten()
        }

        /**
         * Приведение строки к дроби. Строка должна иметь вид a/b, где a и b - целые числа
         *
         * @return дробь, если преобразование было успешно, иначе null
         *
         */
        fun String.toFractionOrNull(): Fraction? = runCatching { this.toFraction() }.getOrNull()

        operator fun Int.compareTo(other: Fraction): Int =
            other.shorten()
                .takeIf { it.denominator == 1 }
                ?.numerator
                ?.compareTo(other)
                ?: (this * 1.0).compareTo(other.toDouble())

        operator fun Long.compareTo(other: Fraction): Int =
            other.shorten()
                .takeIf { it.denominator == 1 }
                ?.numerator
                ?.compareTo(other)
                ?: (this * 1.0).compareTo(other.toDouble())
    }

    /**
     * Сокращение дроби
     *
     * @return новую дробь, сокращенную по правилам математики
     */
    fun shorten(): Fraction {
        val a = BigInteger.valueOf(abs(this.numerator * 1L))
        val b = BigInteger.valueOf(abs(this.denominator * 1L))
        val divider = a.gcd(b).toInt()

        val shortenFraction =
            if (divider != 0) {
                Fraction(
                    numerator = this.numerator / divider,
                    denominator = this.denominator / divider,
                )
            } else {
                this
            }

        return when {
            shortenFraction.numerator != 0 && shortenFraction.denominator < 0 ->
                Fraction(-shortenFraction.numerator, -shortenFraction.denominator)

            else -> shortenFraction
        }
    }

    /**
     * Представление дроби в виде десятичной дроби
     *
     * @return десятичную дробь - результат деление числителя на знаменатель
     */
    fun toDouble(): Double = (this.numerator * 1.0) / this.denominator

    override fun toString(): String =
        when {
            this.denominator == 1 -> this.numerator.toString()
            this.numerator == 0 -> "0"
            else -> "${this.numerator}/${this.denominator}"
        }

    // математические операции
    fun abs() = this * sign()

    operator fun plus(other: Fraction): Fraction =
        Fraction(
            numerator = this.numerator * other.denominator + other.numerator * this.denominator,
            denominator = this.denominator * other.denominator,
        ).shorten()

    operator fun minus(other: Fraction): Fraction =
        Fraction(
            numerator = this.numerator * other.denominator - other.numerator * this.denominator,
            denominator = this.denominator * other.denominator,
        ).shorten()

    operator fun times(other: Fraction): Fraction =
        Fraction(
            numerator = this.numerator * other.numerator,
            denominator = this.denominator * other.denominator,
        ).shorten()

    operator fun div(other: Fraction): Fraction =
        Fraction(
            numerator = this.numerator * other.denominator,
            denominator = this.denominator * other.numerator,
        ).shorten()

    operator fun unaryMinus(): Fraction = Fraction(-this.numerator, this.denominator)

    operator fun plus(other: Int): Fraction = this + from(other)

    operator fun minus(other: Int): Fraction = this - from(other)

    operator fun times(other: Int): Fraction = this * from(other)

    operator fun div(other: Int): Fraction = this / from(other)

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result = 31 * result + denominator.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Fraction -> this.shorten().compareTo(other.shorten()) == 0

            is Int -> this.shorten().compareTo(other) == 0

            else -> false
        }

    override operator fun compareTo(other: Fraction): Int {
        val shortenA = this.shorten()
        val shortenB = other.shorten()
        return (shortenA - shortenB).sign()
    }

    operator fun compareTo(other: Int): Int = this.compareTo(from(other))

    // получение знака дроби
    private fun sign(): Int =
        when {
            this.numerator * this.denominator > 0 ||
                this.numerator * this.denominator < 0 ->
                min(this.numerator, this.denominator) / abs(min(this.numerator, this.denominator))

            else -> 0
        }
}
