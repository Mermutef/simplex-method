package ru.yarsu.domain.simplex

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.Matrix.Companion.unaryMinus
import ru.yarsu.domain.entities.TaskType

class SyntheticBasisMethod(
    override val matrix: Matrix,
    override val function: Function,
    override val taskType: TaskType,
    override val stepsReplaces: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
    override val stepsTables: MutableList<SimplexTable> = mutableListOf(),
    override val startBasis: List<Int> = emptyList(),
) : SimplexBase {
    init {
        stepsTables.add(startTable)
    }

    /**
     * Список индексов искусственных базисных переменных
     */
    @get:JsonIgnore
    val syntheticBasis: List<Int>
        get() {
            val syntheticBasisMutable = mutableListOf<Int>()
            matrix.basis.forEachIndexed { i, _ ->
                syntheticBasisMutable.add(matrix.n + i - 1)
            }
            return syntheticBasisMutable
        }

    /**
     * Список индексов свободных переменных
     */
    @get:JsonIgnore
    val nonSyntheticVariables: List<Int>
        get() = matrix.basis + matrix.free

    /**
     * Начальная симплекс-таблица метода искусственного базиса
     */
    @get:JsonIgnore
    override val startTable: SimplexTable
        get() {
            val extendedFunctionCoefficients = function.coefficients.map { Fraction.from(0) }.toMutableList()
            val extendedMatrixCoefficients = mutableListOf<List<Fraction>>()
            matrix.basis.forEachIndexed { i, _ ->
                extendedFunctionCoefficients.add(extendedFunctionCoefficients.lastIndex, Fraction.from(1))
                val newRow = mutableListOf<Fraction>()
                matrix.basis.forEachIndexed { j, _ ->
                    newRow.add(if (i == j) Fraction.from(1) else Fraction.from(0))
                }
                val matrixRow = matrix.coefficients[i]
                val correctMatrixRow = if (matrixRow.last() >= 0) matrixRow else -matrixRow
                extendedMatrixCoefficients.add(newRow + correctMatrixRow)
            }

            return SimplexTable(
                matrix =
                    Matrix(
                        coefficients = extendedMatrixCoefficients,
                        n = matrix.n + matrix.m,
                        m = matrix.m,
                        basis = syntheticBasis,
                        free = nonSyntheticVariables,
                    ),
                function = Function(coefficients = extendedFunctionCoefficients),
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
        return Failure(SimplexError.IT_IS_NOT_GOOD)
    }

    @JsonIgnore
    override fun getSolution(a: Int): Result<Pair<List<Int>, List<Int>>, SimplexError> {
        val lastStepTable = stepsTables.last()
        val matrix = lastStepTable.matrix
        val function = lastStepTable.function
        matrix.coefficients.mapIndexed { idx, row ->
            row[idx] == Fraction.from(1) || row[idx] == Fraction.from(0)
        }.all { it }.takeIf { it }
            ?: return Failure(SimplexError.INCORRECT_MATRIX)

        if ((lastStepTable.possibleReplaces() + lastStepTable.possibleIdleRunningReplaces(syntheticBasis)).isNotEmpty()) {
            return Failure(SimplexError.NOT_OPTIMAL_SOLUTION)
        }

        val functionInBasis = function.inBasisOf(matrix)
        if (functionInBasis.coefficients.filterIndexed { idx, _ -> (idx in matrix.free || idx == matrix.bIdx) && idx !in syntheticBasis }
                .any { xi -> xi != Fraction.from(0) }
        ) {
            return Failure(SimplexError.INCOMPATIBLE_CONSTRAINTS_SYSTEM)
        }

        val unnecessaryConstraintsIndices: List<Int> =
            matrix.coefficients.mapIndexed { idx, row ->
                if (matrix.basis[idx] in syntheticBasis) {
                    if (row.last() == Fraction.from(0)) {
                        idx
                    } else {
                        return Failure(SimplexError.IRREPLACEABLE_SYNTHETIC_VARIABLES_IN_BASIS)
                    }
                } else {
                    null
                }
            }.filterNotNull().sorted()
        val simplexBasis = matrix.basis.filter { it !in syntheticBasis }.sorted()

        return Success(Pair(simplexBasis, unnecessaryConstraintsIndices))
    }

    @JsonIgnore
    fun getSimplexMethodBySolution(): SimplexMethod? =
        when (val solution = getSolution(1)) {
            is Success -> {
                val newMatrixCoefficients =
                    matrix.coefficients.mapIndexed { idx, row ->
                        if (idx !in solution.value.second) {
                            row
                        } else {
                            null
                        }
                    }.filterNotNull().toTypedArray()
                val defaultBasis = newMatrixCoefficients.indices.toList()
                val defaultFree = (defaultBasis.size..<matrix.n - 1).toList()
                SimplexMethod(
                    matrix =
                        Matrix(
                            n = matrix.n,
                            m = defaultBasis.size,
                            basis = defaultBasis,
                            free = defaultFree,
                            coefficients = newMatrixCoefficients,
                        ),
                    function = function,
                    startBasis = solution.value.first,
                    taskType = taskType,
                )
            }

            is Failure -> null
        }
}
