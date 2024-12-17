package ru.yarsu.simplex

import ru.yarsu.entities.Fraction
import ru.yarsu.entities.Matrix
import ru.yarsu.entities.Function

class SyntheticBasis(
    val matrix: Matrix,
    val function: Function,
) {
    operator fun invoke(): SimplexTable {
        val syntheticFunction = function.coefficients.map { Fraction(0) }.toMutableList()
        syntheticFunction.removeLast()
        val syntheticBasis = mutableListOf<Int>()
        val eMatrix = mutableListOf<MutableList<Fraction>>()
        matrix.basis.forEachIndexed { i, _ ->
            syntheticFunction.addLast(Fraction(1))
            syntheticBasis.add(matrix.n + i - 1)
            val newRow = mutableListOf<Fraction>()
            matrix.basis.forEachIndexed { j, _ ->
                newRow.add(if (i == j) Fraction(1) else Fraction(0))
            }
            eMatrix.add((newRow + matrix.coefficients[i]).toMutableList())
        }
        syntheticFunction.addLast(Fraction(0))
        val syntheticFree = matrix.basis + matrix.free
        return SimplexTable(
            matrix = Matrix(
                coefficients = eMatrix,
                n = matrix.n + matrix.m,
                m = matrix.m,
                basis = syntheticBasis,
                free = syntheticFree,
            ),
            function = Function(coefficients = syntheticFunction),
        )
    }
}