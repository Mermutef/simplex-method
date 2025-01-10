package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.playModeField
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.validate
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.notFound

class AutoSolveHandler(
    private val render: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val metadataForm = taskMetadataForm from request
        val metadataFormValidationResult = metadataForm.validate()
        if (metadataFormValidationResult is Failure) {
            return render(request) draw
                HomePageVM(
                    metadataForm = metadataForm,
                    errors = metadataFormValidationResult.reason,
                )
        }
        val validatedForm = metadataFormValidationResult.valueOrNull()!!
        if (playModeField from metadataForm == null) {
            return render.renderTaskHomePage(
                request = request,
                validatedForm = validatedForm,
                metadataForm = metadataForm,
                solve = true,
            )
        }
        return notFound()
    }
}
