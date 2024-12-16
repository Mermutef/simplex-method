package ru.yarsu

class SimplexStep(
    val matrix: Matrix,
    val function: Function,
) {
    operator fun invoke(s: Int, r: Int): SimplexStep {
        val fullVector = matrix.fullIndices
        val rIdx = fullVector.indexOf(r)
        val sIdx = fullVector.indexOf(s)
        val newFullVector = fullVector.swap(rIdx, sIdx)
        val newBasis = newFullVector.slice(0..<matrix.m)
        val newFree = newFullVector.slice(matrix.m..<matrix.n - 1)
        val newMatrix = matrix.inBasis(newBasis, newFree).straightRunning().reverseRunning()
        return SimplexStep(
            matrix = newMatrix,
            function = Function(
                coefficients = function.fullFunctionCoefficients(newMatrix.fullIndices).swap(r, s),
                n = matrix.n,
            ).inBasis(newMatrix), //todo пересчет значений функции правильно сделать
        )
    }

    override fun toString(): String {
        return "$function\n$matrix"
    }

    fun possibleReplaces(): Triple<Fraction, Int, Int>? {
        val possibleS = matrix.free.filter { idx ->
            function.coefficients[idx] < 0
        }
        val fractions = mutableListOf<Triple<Fraction, Int, Int>>()
        for (s in possibleS) {
            for (r in matrix.basis) {
                println(matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.fullIndices.indexOf(s)])
                if (matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.fullIndices.indexOf(s)] > 0) {
                    fractions.add(
                        Triple(
                            matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.n - 1] /
                                    matrix.coefficients[matrix.fullIndices.indexOf(r)][matrix.fullIndices.indexOf(s)],
                            s,
                            r,
                        )
                    )
                }
            }
        }

        return fractions.minByOrNull { it.first }
    }
}

