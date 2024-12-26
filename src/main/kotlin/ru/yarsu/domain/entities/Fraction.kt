package ru.yarsu.domain.entities

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min

@Suppress("detekt:TooManyFunctions")
class Fraction(val numerator: Int, val denominator: Int) : Comparable<Fraction> {
    @Suppress("detekt:TooManyFunctions")
    // математические операции для работы с дробью как с обычным числом
    companion object {
        fun from(numerator: Int) = Fraction(numerator, 1)

        operator fun Int.div(other: Fraction): Fraction = from(this) / other

        operator fun Int.plus(other: Fraction): Fraction = from(this) + other

        operator fun Int.minus(other: Fraction): Fraction = from(this) - other

        operator fun Int.times(other: Fraction): Fraction = from(this) * other

        /**
         * Приведение строки к дроби. Строка должна иметь вид [-]a/b, где a и b - целые числа
         *
         * @return дробь
         *
         * @throws IllegalArgumentException выбрасывается, если поданная на вход строка
         * не удовлетворяет описанному формату
         */
        fun String.toFraction(): Fraction {
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
            Fraction(
                numerator = this.numerator / divider,
                denominator = this.denominator / divider,
            )
        return when {
            shortenFraction.numerator < 0 && shortenFraction.denominator < 0 ->
                Fraction(-shortenFraction.numerator, -shortenFraction.denominator)

            shortenFraction.numerator > 0 && shortenFraction.denominator < 0 ->
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

            is Long -> this.shorten().compareTo(other) == 0

            else -> false
        }

    override operator fun compareTo(other: Fraction): Int {
        val shortenA = this.shorten()
        val shortenB = other.shorten()
        return (shortenA - shortenB).sign()
    }

    operator fun compareTo(other: Int): Int =
        this.shorten()
            .takeIf { it.denominator == 1 }
            ?.numerator
            ?.compareTo(other * 1L)
            ?: this.toDouble().compareTo(other * 1.0)

    operator fun compareTo(other: Long): Int =
        this.shorten()
            .takeIf { it.denominator == 1 }
            ?.numerator
            ?.compareTo(other)
            ?: this.toDouble().compareTo(other * 1.0)

    // получение знака дроби
    private fun sign(): Int =
        when {
            this.numerator * this.denominator > 0 ||
                this.numerator * this.denominator < 0 ->
                (
                    min(this.numerator, this.denominator) / abs(min(this.numerator, this.denominator))
                ).toInt()

            else -> 0
        }
}
