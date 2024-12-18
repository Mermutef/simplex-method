package ru.yarsu.simplex

import ru.yarsu.entities.Fraction
import ru.yarsu.entities.Matrix
import ru.yarsu.entities.Function

class SyntheticBasis(
    val matrix: Matrix,
    val function: Function,
) {
    operator fun invoke(): SimplexTable {
        val syntheticBasis = mutableListOf<Int>()
        val syntheticFree = matrix.basis + matrix.free

        val extendedFunctionCoefficients = function.coefficients.map { Fraction(0) }.toMutableList()
        extendedFunctionCoefficients.removeLast()
        val extendedMatrixCoefficients = mutableListOf<MutableList<Fraction>>()
        matrix.basis.forEachIndexed { i, _ ->
            extendedFunctionCoefficients.addLast(Fraction(1))
            syntheticBasis.add(matrix.n + i - 1)
            val newRow = mutableListOf<Fraction>()
            matrix.basis.forEachIndexed { j, _ ->
                newRow.add(if (i == j) Fraction(1) else Fraction(0))
            }
            extendedMatrixCoefficients.add((newRow + matrix.coefficients[i]).toMutableList())
        }
        extendedFunctionCoefficients.addLast(Fraction(0))

        return SimplexTable(
            matrix = Matrix(
                coefficients = extendedMatrixCoefficients,
                n = matrix.n + matrix.m,
                m = matrix.m,
                basis = syntheticBasis,
                free = syntheticFree,
            ),
            function = Function(coefficients = extendedFunctionCoefficients),
        )
    }
}