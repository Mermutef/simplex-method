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
            ).required("method")

    val taskTypeField =
        FormField
            .nonBlankString()
            .nonEmptyString()
            .map(
                { fromForm: String -> TaskType.valueOf(fromForm.uppercase().replace("-", "_")) },
                { toForm: TaskType -> toForm.toString().lowercase().replace("_", "-") },
            ).required("task-type")

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
                    }.getOrElse { throw IllegalArgumentException("") }
                },
                { toForm ->
                    jmapper.writeValueAsString(
                        toForm.map {
                            it.map { aij -> aij.toString() }
                        },
                    )
                },
            ).required("matrixJson")

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
            ).required("functionJson")

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
            ).required("basisJson")

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
            ).optional("freeJson")

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

    val modeField = FormField.optional("stepByStep")

    val taskMetadataForm =
        Body.webForm(
            Validator.Feedback,
            methodField,
            taskTypeField,
            matrixField,
            functionField,
            basisField,
            modeField,
        ).toLens()

    val simplexMethodForm =
        Body.webForm(
            Validator.Feedback,
            simplexMethodField,
        )

    val syntheticBasisMethodForm =
        Body.webForm(
            Validator.Feedback,
            syntheticBasisMethodField,
        )

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
