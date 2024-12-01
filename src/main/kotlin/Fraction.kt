package ru.yarsu

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min

class Fraction {
    val numerator: Long
    val denominator: Long

    // Constructors
    constructor(numerator: Long, denominator: Long) {
        require(denominator != 0L) {
            "The denominator of fraction must be not null value"
        }
        this.numerator = numerator
        this.denominator = denominator
    }

    constructor(numerator: Int, denominator: Int) {
        require(denominator != 0) {
            "The denominator of fraction must be not null value"
        }
        this.numerator = numerator * 1L
        this.denominator = denominator * 1L
    }

    constructor(numerator: Long) {
        this.numerator = numerator
        this.denominator = 1L
    }

    constructor(numerator: Int) {
        this.numerator = numerator * 1L
        this.denominator = 1L
    }

    // Stringifies
    override fun toString(): String =
        when {
            this.denominator == 1L -> this.numerator.toString()
            this.numerator == 0L -> "0"
            else -> "${this.numerator}/${this.denominator}"
        }

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result = 31 * result + denominator.hashCode()
        return result
    }

    // Comparison
    override operator fun equals(other: Any?): Boolean =
        when (other) {
            is Fraction -> this.shorten().compareTo(other.shorten()) == 0

            is Int -> this.shorten().compareTo(other) == 0

            is Long -> this.shorten().compareTo(other) == 0

            else -> false
        }

    operator fun compareTo(other: Fraction): Int {
        val shortenA = this.shorten()
        val shortenB = other.shorten()
        return (shortenA - shortenB).sign()
    }

    operator fun compareTo(other: Int): Int =
        this.shorten()
            .takeIf { it.denominator == 1L }
            ?.numerator
            ?.compareTo(other * 1L)
            ?: this.toDouble().compareTo(other * 1.0)

    operator fun compareTo(other: Long): Int =
        this.shorten()
            .takeIf { it.denominator == 1L }
            ?.numerator
            ?.compareTo(other)
            ?: this.toDouble().compareTo(other * 1.0)

    // Shorten fraction
    fun shorten(): Fraction {
        val a = BigInteger.valueOf(abs(this.numerator))
        val b = BigInteger.valueOf(abs(this.denominator))
        val divider = a.gcd(b).toLong()
        val shortenFraction = Fraction(
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

    // Math operations
    operator fun plus(other: Fraction): Fraction = Fraction(
        numerator = this.numerator * other.denominator + other.numerator * this.denominator,
        denominator = this.denominator * other.denominator,
    ).shorten()

    operator fun minus(other: Fraction): Fraction = Fraction(
        numerator = this.numerator * other.denominator - other.numerator * this.denominator,
        denominator = this.denominator * other.denominator,
    ).shorten()

    operator fun times(other: Fraction): Fraction = Fraction(
        numerator = this.numerator * other.numerator,
        denominator = this.denominator * other.denominator,
    ).shorten()

    operator fun div(other: Fraction): Fraction = Fraction(
        numerator = this.numerator * other.denominator,
        denominator = this.denominator * other.numerator,
    ).shorten()

    operator fun unaryMinus(): Fraction = Fraction(-this.numerator, this.denominator)

    operator fun plus(other: Int): Fraction = this + Fraction(other)

    operator fun minus(other: Int): Fraction = this - Fraction(other)

    operator fun times(other: Int): Fraction = this * Fraction(other)

    operator fun div(other: Int): Fraction = this / Fraction(other)

    operator fun plus(other: Long): Fraction = this + Fraction(other)

    operator fun minus(other: Long): Fraction = this - Fraction(other)

    operator fun times(other: Long): Fraction = this * Fraction(other)

    operator fun div(other: Long): Fraction = this / Fraction(other)

    // Get the sign of fraction
    private fun sign(): Int =
        when {
            this.numerator * this.denominator > 0L ||
                    this.numerator * this.denominator < 0L -> (
                    min(this.numerator, this.denominator) / abs(min(this.numerator, this.denominator))
                    ).toInt()

            else -> 0
        }

    fun toDouble(): Double = (this.numerator * 1.0) / this.denominator
}

operator fun Int.div(other: Fraction): Fraction = Fraction(this) / other

operator fun Int.plus(other: Fraction): Fraction = Fraction(this) + other

operator fun Int.minus(other: Fraction): Fraction = Fraction(this) - other

operator fun Int.times(other: Fraction): Fraction = Fraction(this) * other

operator fun Long.div(other: Fraction): Fraction = Fraction(this) / other

operator fun Long.plus(other: Fraction): Fraction = Fraction(this) + other

operator fun Long.minus(other: Fraction): Fraction = Fraction(this) - other

operator fun Long.times(other: Fraction): Fraction = Fraction(this) * other

fun String.toFraction(): Fraction {
    val values = this
        .split("/")
        .map {
            it
                .trim()
                .toLongOrNull()
                ?: throw IllegalArgumentException("Invalid fraction string")
        }.takeIf { it.size in 1..2 }
        ?: throw IllegalArgumentException("Invalid fraction string")

    return when (values.size) {
        1 -> Fraction(values[0])
        else -> Fraction(values[0], values[1])
    }
}

fun String.toFractionOrNull(): Fraction? {
    val values = this
        .split("/")
        .map {
            it
                .trim()
                .toLongOrNull()
                ?: return null
        }.takeIf { it.size in 1..2 }
        ?: return null

    return when (values.size) {
        1 -> Fraction(values[0])
        else -> runCatching { Fraction(values[0], values[1]) }.getOrNull()
    }
}

operator fun Int.compareTo(other: Fraction): Int =
    other.shorten()
        .takeIf { it.denominator == 1L }
        ?.numerator
        ?.compareTo(other)
        ?: (this * 1.0).compareTo(other.toDouble())

operator fun Long.compareTo(other: Fraction): Int =
    other.shorten()
        .takeIf { it.denominator == 1L }
        ?.numerator
        ?.compareTo(other)
        ?: (this * 1.0).compareTo(other.toDouble())
