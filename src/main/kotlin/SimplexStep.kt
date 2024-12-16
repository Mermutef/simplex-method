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

        return SimplexStep(
            matrix = matrix.inBasis(newBasis, newFree).straightRunning().reverseRunning(),
            function = function,
        )
    }
}

