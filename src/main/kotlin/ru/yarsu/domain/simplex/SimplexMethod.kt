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
    val matrix: Matrix,
    val function: Function,
    val startBasis: List<Int>,
    val taskType: TaskType = TaskType.MIN,
    val stepsReplaces: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
    val stepsTables: MutableList<SimplexTable> = mutableListOf(),
) {
    init {
        require(startBasis.size == matrix.m) {
            "Недостаточно переменных передано в базис. " +
                "Должно быть ровно столько же, сколько строк в матрице ограничений. " +
                "(передано ${startBasis.size}, ожидалось ${matrix.m})."
        }
        stepsTables.add(startTable)
    }

    @get:JsonIgnore
    val startTable: SimplexTable
        get() {
            val matrixInBasis = matrix.solveGauss(withBasis = startBasis)
            return SimplexTable(
                matrix = matrixInBasis,
                function = function,
                taskType = taskType,
            )
        }

    fun nextStep(inOutVariables: Pair<Int, Int>? = null): Result<Boolean, SimplexMethodError> {
        val possibleReplaces = stepsTables.last().possibleReplaces()
        if (possibleReplaces.isEmpty()) return Success(false)

        val replace = inOutVariables ?: possibleReplaces.first()
        if (replace !in possibleReplaces) return Failure(SimplexMethodError.INVALID_REPLACE)

        stepsReplaces[stepsReplaces.size] = replace
        stepsTables.add(stepsTables.last() changeBasisBy replace)

        return Success(true)
    }

    fun previousStep(): Result<Boolean, SimplexMethodError> {
        if (stepsReplaces.isEmpty()) return Success(false)
        stepsReplaces.remove(stepsReplaces.size - 1)
        stepsTables.removeLast()
        return Success(true)
    }

    fun solve() {
        while (nextStep().valueOrNull()?.takeIf { it } != null) continue
    }

    @JsonIgnore
    fun getSolution(): Result<Pair<List<Fraction>, Fraction>, SimplexMethodError> {
        val lastStepTable = stepsTables.last()
        val matrix = lastStepTable.matrix
        matrix.coefficients.mapIndexed { idx, row ->
            row[idx] == Fraction.from(1) ||
                row[idx] == Fraction.from(0)
        }
            .all { it }.takeIf { it }
            ?: return Failure(SimplexMethodError.INCORRECT_MATRIX)

        if (lastStepTable.possibleReplaces().isNotEmpty()) return Failure(SimplexMethodError.NOT_OPTIMAL_SOLUTION)

        val functionInBasis = function.inBasisOf(matrix, taskType)
        if (functionInBasis.coefficients.dropLast(1).any { xi -> xi < 0 }) {
            return Failure(SimplexMethodError.UNLIMITED_FUNCTION)
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
}

@Suppress("ktlint:standard:max-line-length", "detekt:MaxLineLength")
enum class SimplexMethodError(val text: String) {
    UNLIMITED_FUNCTION("Функция на заданном множестве неограниченна, минимизировать (максимизировать) невозможно."),
    INCORRECT_MATRIX("Некорректная симплекс-таблица. Симплекс-таблица должна иметь диагональный вид относительно базисных переменных."),
    NOT_OPTIMAL_SOLUTION("Симплекс-таблица не является оптимальной (имеются доступные замены)."),
    INVALID_REPLACE("Данные индексы переменных не являются допустимыми для совершения шага симплекс-метода."),
}
