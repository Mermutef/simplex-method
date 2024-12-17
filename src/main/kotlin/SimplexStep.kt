package ru.yarsu

class SimplexStep(
    val matrix: Matrix,
    val function: Function,
) {
    operator fun invoke(s: Int, r: Int): SimplexStep {
        println(matrix.basis)
        val fullVector = matrix.fullIndices
        val rIdx = fullVector.indexOf(r)
        val sIdx = fullVector.indexOf(s)
        val newFullVector = fullVector.swap(rIdx, sIdx)
        val newBasis = newFullVector.slice(0..<matrix.m)
        println(newBasis)
        val newFree = newFullVector.slice(matrix.m..<matrix.n - 1)
        val newMatrix = matrix.inBasis(newBasis, newFree).straightRunning().reverseRunning()
        return SimplexStep(
            matrix = newMatrix,
            function = function,
        )
    }

    val solution: List<Fraction>
        get() {
            val values = mutableListOf<Pair<Int, Fraction>>()
            for (i in matrix.basis) {
                val iIdx = matrix.basis.indexOf(i)
                values.add(Pair(i, matrix.coefficients[iIdx][matrix.constantIdx]))
            }
            for (i in matrix.free) {
                values.add(Pair(i, Fraction(0)))
            }

            return values.sortedBy { it.first }.map { it.second }
        }

    override fun toString(): String {
        return "${function.inBasis(matrix)}\n$matrix"
    }

    fun possibleReplaces(): Triple<Fraction, Int, Int>? {
        val functionInBasis = function.inBasis(matrix)
        val possibleS = matrix.free.filter { idx ->
            functionInBasis.coefficients[idx] < 0
        }
        val fractions = mutableListOf<Triple<Fraction, Int, Int>>()
        for (s in possibleS) {
            for (r in matrix.basis) {
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

