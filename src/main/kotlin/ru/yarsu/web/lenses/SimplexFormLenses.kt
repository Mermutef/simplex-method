package ru.yarsu.web.lenses

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.Lens
import org.http4k.lens.LensFailure
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.multipartForm
import org.http4k.lens.nonBlankString
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import ru.yarsu.domain.entities.Fraction.Companion.toFraction
import ru.yarsu.domain.entities.Function
import ru.yarsu.domain.entities.Matrix
import ru.yarsu.domain.entities.TaskType
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.methodField
import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField

private val jmapper = jacksonObjectMapper()

object SimplexFormLenses {
    val methodField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String -> Method.valueOf(fromForm.uppercase().replace("-", "_")) },
                { toForm: Method -> toForm.toString().lowercase().replace("_", "-") },
            ).required("method", "Необходимо выбрать один из доступных методов решения задачи.")

    val taskTypeField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String -> TaskType.valueOf(fromForm.uppercase().replace("-", "_")) },
                { toForm: TaskType -> toForm.toString().lowercase().replace("_", "-") },
            ).required("task-type", "Необходимо выбрать тип задачи")

    val matrixField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm ->
                    runCatching {
                        jmapper.readValue<Array<Array<String>>>(fromForm).map {
                            it.map { aij -> aij.toFraction() }.toTypedArray()
                        }.toTypedArray()
                    }.getOrElse { throw IllegalArgumentException() }
                },
                { toForm ->
                    jmapper.writeValueAsString(
                        toForm.map {
                            it.map { aij -> aij.toString() }
                        },
                    )
                },
            ).required(
                "matrixJson",
                "Матрица должна быть прямоугольной, коэффициенты матрицы должны быть простыми или десятичными дробями или целыми числами.",
            )

    val functionField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm ->
                    runCatching {
                        jmapper.readValue<List<String>>(fromForm).map {
                            it.toFraction()
                        }
                    }.getOrElse { throw IllegalArgumentException("") }
                },
                { toForm ->
                    jmapper.writeValueAsString(
                        toForm.map {
                            it.toString()
                        },
                    )
                },
            ).required(
                "functionJson",
                "Коэффициенты функции должны быть простыми или десятичными дробями или целыми числами.",
            )

    val basisField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm ->
                    runCatching {
                        jmapper.readValue<List<Int>>(fromForm)
                    }.getOrElse { throw IllegalArgumentException("") }
                },
                { toForm -> jmapper.writeValueAsString(toForm) },
            ).required("basisJson", "Индексы базисных переменных могут быть только целыми числами.")

    val freeField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm ->
                    runCatching {
                        jmapper.readValue<List<Int>>(fromForm)
                    }.getOrElse { throw IllegalArgumentException("") }
                },
                { toForm -> jmapper.writeValueAsString(toForm) },
            ).optional("freeJson", "Индексы свободных переменных могут быть только целыми числами.")

    val replaceIndicesField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm ->
                    fromForm.split(":").mapNotNull { it.toIntOrNull() }.takeIf { it.size == 2 }
                        ?.let { Pair(it[0], it[1]) } ?: throw IllegalArgumentException()
                },
                { toForm: Pair<Int, Int> -> "${toForm.first}:${toForm.second}" },
            ).optional("replace-pair", "Индексы замещения должны быть записаны в виде пары чисел.")

    val simplexMethodField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String ->
                    runCatching { jmapper.readValue<SimplexMethod>(fromForm) }.getOrNull()
                        ?: throw IllegalArgumentException()
                },
                { toForm: SimplexMethod? -> toForm?.let { jmapper.writeValueAsString(it) } ?: "" },
            ).optional("simplexMethodJson")

    val syntheticBasisMethodField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String ->
                    runCatching { jmapper.readValue<SyntheticBasisMethod>(fromForm) }.getOrNull()
                        ?: throw IllegalArgumentException()
                },
                { toForm: SyntheticBasisMethod? -> toForm?.let { jmapper.writeValueAsString(it) } ?: "" },
            ).optional("syntheticBasisJson")

    val playModeField =
        FormField.map(
            { fromForm: String -> fromForm == "on" },
            { toForm: Boolean -> if (toForm) "on" else "" },
        ).optional("stepByStep")
    val useFractionsField =
        FormField.map(
            { fromForm: String -> fromForm == "on" },
            { toForm: Boolean -> if (toForm) "on" else "" },
        ).optional("in-fractions")

    val taskMetadataForm =
        Body.webForm(
            Validator.Feedback,
            methodField,
            taskTypeField,
            matrixField,
            functionField,
            basisField,
            playModeField,
            replaceIndicesField,
            useFractionsField,
        ).toLens()

    val simplexMethodForm =
        Body.webForm(
            Validator.Feedback,
            simplexMethodField,
        ).toLens()

    val syntheticBasisMethodForm =
        Body.webForm(
            Validator.Feedback,
            syntheticBasisMethodField,
        ).toLens()

    val fileField = MultipartFormFile.required("file")

    val fileForm = Body.multipartForm(Validator.Feedback, fileField).toLens()

    infix fun BiDiBodyLens<WebForm>.from(request: Request) = this(request)

    infix fun BiDiBodyLens<MultipartForm>.from(request: Request) = this(request)

    infix fun <T> BiDiLens<WebForm, T>.from(form: WebForm) = this(form)

    infix fun <T> BiDiLens<MultipartForm, T>.from(form: MultipartForm) = this(form)

    fun <IN : Any, OUT> lensOrNull(
        lens: Lens<IN, OUT?>,
        value: IN,
    ) = try {
        lens.invoke(value)
    } catch (_: LensFailure) {
        null
    }
}

fun String.isBlankOrEmpty() = this.isBlank() || this.isEmpty()

fun String.isNotBlankOrEmpty() = this.isNotBlank() && this.isNotEmpty()

fun WebForm.validate(): Result<ValidatedFormData, Map<FormErrorType, List<String>>> {
    val errors =
        mapOf(
            Pair(FormErrorType.FUNCTION, mutableListOf<String>()),
            Pair(FormErrorType.MATRIX, mutableListOf()),
            Pair(FormErrorType.BASIS, mutableListOf()),
        )
    if (this.errors.isNotEmpty()) {
        this.errors.forEach { error ->
            when (error.meta.name) {
                "functionJson" -> errors[FormErrorType.FUNCTION]!!.add(error.meta.description.toString())
                "matrixJson" -> errors[FormErrorType.MATRIX]!!.add(error.meta.description.toString())
                "basisJson" -> errors[FormErrorType.BASIS]!!.add(error.meta.description.toString())
            }
        }
        return Failure(errors)
    }

    val basis = basisField from this
    val matrixCoefficients = matrixField from this
    val functionCoefficients = functionField from this
    val taskType = taskTypeField from this
    val method = methodField from this
    val n = matrixCoefficients.first().size
    val m = matrixCoefficients.size
    if (basis.size != m && method == Method.SIMPLEX_METHOD) {
        errors[FormErrorType.BASIS]!!.add(
            "Число базисных переменных должно быть равно числу строк (передано ${basis.size}, ожидалось $m).",
        )
        return Failure(errors)
    }
    val defaultBasis = (0..<m).toList()
    val defaultFree = (m..<n - 1).toList()
    val matrix =
        runCatching {
            Matrix(
                m = m,
                n = n,
                coefficients = matrixCoefficients,
                basis = defaultBasis,
                free = defaultFree,
            )
        }.getOrElse {
            errors[FormErrorType.MATRIX]!!.add(it.message ?: "")
            return Failure(errors)
        }
    val function = Function(coefficients = functionCoefficients)
    return Success(
        ValidatedFormData(
            method = method,
            matrix = matrix,
            function = function,
            taskType = taskType,
            startBasis = basis,
        ),
    )
}

class ValidatedFormData(
    val method: Method,
    val matrix: Matrix,
    val function: Function,
    val taskType: TaskType,
    val startBasis: List<Int>,
)

enum class FormErrorType {
    FUNCTION,
    MATRIX,
    BASIS,
}
