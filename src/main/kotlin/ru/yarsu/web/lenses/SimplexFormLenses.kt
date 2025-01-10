package ru.yarsu.web.lenses

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import ru.yarsu.domain.entities.TaskType
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod

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
                "Матрица должна быть прямоугольной, коэффициенты матрицы могут быть простыми или десятичными дробями или целыми числами."
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
                "Коэффициенты функции могут быть простыми или десятичными дробями или целыми числами."
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

    val playModeField = FormField.map(
        { fromForm: String -> fromForm == "on" },
        { toForm: Boolean -> if (toForm) "on" else "" }
    ).optional("stepByStep")
    val useFractionsField = FormField.map(
        { fromForm: String -> fromForm == "on" },
        { toForm: Boolean -> if (toForm) "on" else "" }
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

enum class FormErrorType {
    FUNCTION,
    MATRIX,
    BASIS,
}