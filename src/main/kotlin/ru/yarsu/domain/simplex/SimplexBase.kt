package ru.yarsu.domain.simplex

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.forkhandles.result4k.Result
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.TaskType

interface SimplexBase {
    val matrix: Matrix
    val function: Function
    val taskType: TaskType
    val stepsReplaces: MutableMap<Int, Pair<Int, Int>>
    val stepsTables: MutableList<SimplexTable>
    val startBasis: List<Int>

    @get:JsonIgnore
    val startTable: SimplexTable

    fun nextStep(inOutVariables: Pair<Int, Int>? = null): Result<Boolean, SimplexError>

    fun previousStep(): Boolean

    fun solve()

    @JsonIgnore
    fun getSolution(): Result<Pair<List<Fraction>, Fraction>, SimplexError>

    @JsonIgnore
    fun getSolution(a: Int = 0): Result<Pair<List<Int>, List<Int>>, SimplexError>
}

@Suppress("ktlint:standard:max-line-length", "detekt:MaxLineLength")
enum class SimplexError(val text: String) {
    UNLIMITED_FUNCTION("Функция на заданном множестве неограниченна, минимизировать (максимизировать) невозможно."),
    INCORRECT_MATRIX("Некорректная симплекс-таблица. Симплекс-таблица должна иметь диагональный вид относительно базисных переменных."),
    NOT_OPTIMAL_SOLUTION("Симплекс-таблица не является оптимальной (имеются доступные замены)."),
    INVALID_REPLACE("Данные индексы переменных не являются допустимыми для совершения шага симплекс-метода."),
    INCOMPATIBLE_CONSTRAINTS_SYSTEM("Задача не имеет решения (система ограничений несовместна)."),
    IRREPLACEABLE_SYNTHETIC_VARIABLES_IN_BASIS("Задача не имеет решения (невозможно вывести искусственную переменную из базиса)."),
    IT_IS_NOT_GOOD("Пошло не туда, чини программу."),
    EMPTY_POSSIBLE_REPLACES("Доступных замен больше нет"),
}
