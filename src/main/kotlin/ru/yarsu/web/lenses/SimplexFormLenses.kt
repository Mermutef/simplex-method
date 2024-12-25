package ru.yarsu.web.lenses

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
import org.http4k.lens.multipartForm
import org.http4k.lens.string
import org.http4k.lens.webForm
import ru.yarsu.entities.TaskType
import ru.yarsu.simplex.Method

object SimplexFormLenses {
    val methodField =
        FormField.string().map(
            { fromForm: String -> Method.valueOf(fromForm.uppercase().replace("-", "_")) },
            { toForm: Method -> toForm.toString().lowercase().replace("_", "-") },
        ).required("method")

    val taskTypeField =
        FormField.string().map(
            { fromForm: String -> TaskType.valueOf(fromForm.uppercase().replace("-", "_")) },
            { toForm: TaskType -> toForm.toString().lowercase().replace("_", "-") },
        ).required("task-type")

    val fileField = MultipartFormFile.required("file")

    val taskForm = Body.webForm(Validator.Feedback, methodField, taskTypeField).toLens()
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
