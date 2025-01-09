package ru.yarsu.domain.simplex

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.TaskType
import ru.yarsu.domain.simplex.GaussMethod.solveGauss

class SimplexMethod(
    override val matrix: Matrix,
    override val function: Function,
    override val startBasis: List<Int>,
    override val taskType: TaskType = TaskType.MIN,
    override val stepsReplaces: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
    override val stepsTables: MutableList<SimplexTable> = mutableListOf(),
) : SimplexBase {
    init {
        require(startBasis.size == matrix.m) {
            "Недостаточно переменных передано в базис. " +
                "Должно быть ровно столько же, сколько строк в матрице ограничений. " +
                "(передано ${startBasis.size}, ожидалось ${matrix.m})."
        }
        stepsTables.add(startTable)
    }

    @get:JsonIgnore
    override val startTable: SimplexTable
        get() {
            val matrixInBasis = matrix.solveGauss(withBasis = startBasis)
            return SimplexTable(
                matrix = matrixInBasis,
                function = function,
                taskType = taskType,
            )
        }

    override fun nextStep(inOutVariables: Pair<Int, Int>?): Result<Boolean, SimplexError> {
        val possibleReplaces = stepsTables.last().possibleReplaces()
        if (possibleReplaces.isEmpty()) return Success(false)

        val replace = inOutVariables ?: possibleReplaces.first()
        if (replace !in possibleReplaces) return Failure(SimplexError.INVALID_REPLACE)

        stepsReplaces[stepsReplaces.size] = replace
        stepsTables.add(stepsTables.last() changeBasisBy replace)

        return Success(true)
    }

    override fun previousStep(): Result<Boolean, SimplexError> {
        if (stepsReplaces.isEmpty()) return Success(false)
        stepsReplaces.remove(stepsReplaces.size - 1)
        stepsTables.removeLast()
        return Success(true)
    }

    override fun solve() {
        while (nextStep().valueOrNull()?.takeIf { it } != null) continue
    }

    @JsonIgnore
    override fun getSolution(): Result<Pair<List<Fraction>, Fraction>, SimplexError> {
        val lastStepTable = stepsTables.last()
        val matrix = lastStepTable.matrix
        matrix.coefficients.mapIndexed { idx, row ->
            row[idx] == Fraction.from(1) ||
                row[idx] == Fraction.from(0)
        }
            .all { it }.takeIf { it }
            ?: return Failure(SimplexError.INCORRECT_MATRIX)

        if (lastStepTable.possibleReplaces().isNotEmpty()) return Failure(SimplexError.NOT_OPTIMAL_SOLUTION)

        val functionInBasis = function.inBasisOf(matrix, taskType)
        if (functionInBasis.coefficients.dropLast(1).any { xi -> xi < 0 }) {
            return Failure(SimplexError.UNLIMITED_FUNCTION)
        }

        val vertex =
            (
                (
                    matrix.coefficients.mapIndexed { rowIdx, row ->
                        Pair(matrix.basis[rowIdx], row.last())
                    }
                ) + (matrix.free.map { Pair(it, Fraction.from(0)) })
            )
                .sortedBy { it.first }
                .map { it.second }
        val functionValue = functionInBasis.coefficients.last()

        return Success(Pair(vertex, functionValue))
    }

    @JsonIgnore
    override fun getSolution(a: Int): Result<Pair<List<Int>, List<Int>>, SimplexError> {
        return Failure(SimplexError.IT_IS_NOT_GOOD)
    }
}
