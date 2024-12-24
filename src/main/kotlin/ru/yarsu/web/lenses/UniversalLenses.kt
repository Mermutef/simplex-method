package ru.yarsu.web.lenses

import org.http4k.core.Body
import org.http4k.lens.Lens
import org.http4k.lens.LensFailure
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import ru.yarsu.simplex.Method
import ru.yarsu.simplex.TaskType

object UniversalLenses {
    val methodLens = MultipartFormField.string().map(
        { fromForm: String -> Method.valueOf(fromForm.uppercase().replace("-", "_")) },
        { toForm: Method -> toForm.toString().lowercase().replace("_", "-") }
    ).required("method")

    val taskTypeLens = MultipartFormField.string().map(
        { fromForm: String -> TaskType.valueOf(fromForm.uppercase().replace("-", "_")) },
        { toForm: TaskType -> toForm.toString().lowercase().replace("_", "-") }
    ).required("task-type")

    val fileLens = MultipartFormFile.required("file")

    val taskLenses = Body.multipartForm(Validator.Feedback, methodLens, taskTypeLens, fileLens).toLens()

    fun <IN : Any, OUT> lensOrNull(
        lens: Lens<IN, OUT?>,
        value: IN,
    ) =
        try {
            lens.invoke(value)
        } catch (_: LensFailure) {
            null
        }
}
