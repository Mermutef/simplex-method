package ru.yarsu.web.lenses

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.FormField
import org.http4k.lens.Lens
import org.http4k.lens.LensFailure
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.map
import org.http4k.lens.multipartForm
import org.http4k.lens.nonBlankString
import org.http4k.lens.nonEmptyString
import org.http4k.lens.string
import org.http4k.lens.webForm
import ru.yarsu.entities.Fraction
import ru.yarsu.entities.Fraction.Companion.toFraction
import ru.yarsu.entities.TaskType
import ru.yarsu.simplex.Method

private val jmapper = jacksonObjectMapper()

object SimplexFormLenses {
    val methodField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String -> Method.valueOf(fromForm.uppercase().replace("-", "_")) },
                { toForm: Method -> toForm.toString().lowercase().replace("_", "-") },
            ).required("method")

    val taskTypeField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String -> TaskType.valueOf(fromForm.uppercase().replace("-", "_")) },
                { toForm: TaskType -> toForm.toString().lowercase().replace("_", "-") },
            ).required("task-type")

    val matrixField = FormField
        .nonBlankString()
        .nonEmptyString()
        .map(
            { fromForm ->
                runCatching {
                    jmapper.readValue<Array<Array<String>>>(fromForm).map {
                        it.map { aij -> aij.toFraction() }
                    }
                }.getOrElse { throw IllegalArgumentException("") }
            },
            { toForm ->
                jmapper.writeValueAsString(toForm.map {
                    it.map { aij -> aij.toString() }
                })
            }
        ).required("matrixJson")

    val functionField = FormField
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
                jmapper.writeValueAsString(toForm.map {
                    it.toString()
                })
            }
        ).required("functionJson")

    val freeField = FormField
        .nonBlankString()
        .nonEmptyString()
        .map(
            { fromForm ->
                runCatching {
                    jmapper.readValue<List<Int>>(fromForm)
                }.getOrElse { throw IllegalArgumentException("") }
            },
            { toForm -> jmapper.writeValueAsString(toForm) }
        ).required("freeJson")

    val basisField = FormField
        .nonBlankString()
        .nonEmptyString()
        .map(
            { fromForm ->
                runCatching {
                    jmapper.readValue<List<Int>>(fromForm)
                }.getOrElse { throw IllegalArgumentException("") }
            },
            { toForm -> jmapper.writeValueAsString(toForm) }
        ).required("basisJson")

    val inputFreeField = FormField.optional("inputFree")

    val taskForm = Body.webForm(
        Validator.Feedback,
        methodField,
        taskTypeField,
        matrixField,
        functionField,
        freeField,
        basisField,
        inputFreeField,
    ).toLens()

    val fileField = MultipartFormFile.required("file")

    val fileForm = Body.multipartForm(Validator.Feedback, fileField).toLens()

    infix fun BiDiBodyLens<WebForm>.from(request: Request) = this(request)
    infix fun BiDiBodyLens<MultipartForm>.from(request: Request) = this(request)

    fun <IN : Any, OUT> lensOrNull(
        lens: Lens<IN, OUT?>,
        value: IN,
    ) = try {
        lens.invoke(value)
    } catch (_: LensFailure) {
        null
    }
}
