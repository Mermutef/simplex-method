package ru.yarsu.web.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import ru.yarsu.simplex.Method
import ru.yarsu.simplex.SimplexTable
import ru.yarsu.simplex.SyntheticBasis
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.lenses.UniversalLenses
import ru.yarsu.web.models.HomePageVM

class PostHomeHandler(
    private val render: ContextAwareViewRender,
    private val mapper: ObjectMapper,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = UniversalLenses.taskLenses(request)
        if (form.errors.isNotEmpty()) return Response(Status.NOT_FOUND)
        val json = UniversalLenses.fileLens(form)?.content ?: return Response(Status.NOT_FOUND)
        val initialTable = mapper.readValue<SimplexTable>(json)
        val lst = mutableListOf<String>()
        lst.add("Задача:\n${initialTable.function}")
        lst.add(initialTable.matrix.toString())
        val newBasis = listOf(2, 3)
        val newFree = listOf(0, 1)
        val type = UniversalLenses.taskTypeLens(form)
        val method = UniversalLenses.methodLens(form)
        when (method) {
            Method.SIMPLEX_METHOD -> {
                // временное хранилище шагов симплекс-метода
                val simplexTables = mutableListOf<SimplexTable>()
                // создание нулевого шага симплекс-метода
                simplexTables.add(
                    SimplexTable(
                        matrix =
                            initialTable.matrix
                                .inBasis(newBasis = newBasis, newFree = newFree)
                                .straightRunning()
                                .reverseRunning(),
                        function = initialTable.function,
                    ),
                )
                lst.add("\nНачальная симплекс-таблица:\n${simplexTables.last()}")
                // пока можем сделать шаг симплекс метода
                while (true) {
                    val possibleValues = simplexTables.last().possibleReplaces()?.first() ?: break
                    lst.add("\nШаг ${simplexTables.size}")
                    lst.add("Вводим в базис x${possibleValues.first + 1}, выводим x${possibleValues.second + 1}")
                    simplexTables.add(simplexTables.last() changeBasisBy possibleValues)
                    lst.add("Результат шага:\n${simplexTables.last()}")
                }
                val lastStep = simplexTables.last()
                lst.add("\nМинимальное значение f* = ${lastStep.functionValue}")
                lst.add("Достигается в точке x* = ${lastStep.vertex}")
            }

            Method.SYNTHETIC_BASIS -> {
                val simplexTables2 = mutableListOf<SimplexTable>()
                val sb = SyntheticBasis(matrix = initialTable.matrix, function = initialTable.function)
                simplexTables2.add(sb.startTable)
                lst.add("\nНачальная симплекс-таблица искусственного базиса:\n${simplexTables2.last()}")
                while (true) {
                    val possibleValues = simplexTables2.last().possibleReplaces()?.first() ?: break
                    val s = possibleValues.first
                    val r = possibleValues.second
                    lst.add("\nШаг ${simplexTables2.size}")
                    lst.add("Вводим в базис x${s + 1}, выводим x${r + 1}")
                    simplexTables2.add(simplexTables2.last() changeBasisBy possibleValues)
                    lst.add("Результат шага:\n${simplexTables2.last()}")
                }
                val lastStep2 = simplexTables2.last()
                lst.add("\nМинимальное значение f* = ${lastStep2.functionValue}")
                lst.add("Достигается в точке x* = ${lastStep2.vertex}")

                lst.add("")
                val simplexTables3 = mutableListOf<SimplexTable>()
                simplexTables3.add(sb extractSolutionFrom lastStep2)
                lst.add("\nНачальная симплекс-таблица после искусственного базиса:\n${simplexTables3.last()}")
                while (true) {
                    val possibleValues = simplexTables3.last().possibleReplaces()?.first() ?: break
                    val s = possibleValues.first
                    val r = possibleValues.second
                    lst.add("\nШаг ${simplexTables3.size}")
                    lst.add("Вводим в базис x${s + 1}, выводим x${r + 1}")
                    simplexTables3.add(simplexTables3.last() changeBasisBy possibleValues)
                    lst.add("Результат шага:\n${simplexTables3.last()}")
                }
                val lastStep3 = simplexTables3.last()
                lst.add("\nМинимальное значение f* = ${lastStep3.functionValue}")
                lst.add("Достигается в точке x* = ${lastStep3.vertex}")
            }
        }

        return Response(Status.OK).with(
            render(request) of HomePageVM(lst, form),
        )
    }
}
