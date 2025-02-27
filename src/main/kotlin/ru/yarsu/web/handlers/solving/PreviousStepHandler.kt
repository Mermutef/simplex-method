package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.from
import ru.yarsu.web.lenses.SimplexFormLenses.lensOrNull
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodForm
import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
import ru.yarsu.web.lenses.validate
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.notFound

class PreviousStepHandler(
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

        var renderedSteps = ""

        var syntheticSteps: List<Triple<Int, Int, Int>> = emptyList()
        var simplexSteps: List<Triple<Int, Int, Int>> = emptyList()

        var simplexMethodTask: SimplexMethod? = null
        var syntheticBasisTask: SyntheticBasisMethod? = null

        if (validatedForm.method == Method.SYNTHETIC_BASIS) {
            syntheticBasisTask = lensOrNull(syntheticBasisMethodField, (syntheticBasisMethodForm from request))
                ?: return notFound()

            val syntheticBasisResult = syntheticBasisTask.getSolution(1)
            var renderedSolution = ""
            when (syntheticBasisResult) {
                is Success -> {
                    val simplexFromRequest = lensOrNull(simplexMethodField, (simplexMethodForm from request))
                    if (simplexFromRequest != null) {
                        simplexMethodTask = simplexFromRequest
                        if (simplexMethodTask.stepsReplaces.isNotEmpty()) {
                            simplexMethodTask.previousStep()
                            renderedSolution =
                                syntheticBasisTask.renderSolution(
                                    method = validatedForm.method,
                                    request = request,
                                    render = render,
                                )
                        } else {
                            simplexMethodTask = null
                            syntheticBasisTask.previousStep()
                        }
                    } else {
                        syntheticBasisTask.previousStep()
                    }
                }

                is Failure -> syntheticBasisTask.previousStep()
            }

            val solution =
                syntheticBasisTask.renderSteps(
                    request = request,
                    forSyntheticBasis = true,
                    render = render,
                )
            renderedSteps = solution.first + renderedSolution
            syntheticSteps = solution.second
            if (simplexMethodTask != null) {
                val solution1 =
                    simplexMethodTask.renderSteps(
                        request = request,
                        render = render,
                    )
                renderedSteps += solution1.first
                simplexSteps = solution1.second
            }
        } else {
            simplexMethodTask = lensOrNull(simplexMethodField, (simplexMethodForm from request)) ?: return notFound()
            simplexMethodTask.previousStep()
            val solution =
                simplexMethodTask.renderSteps(
                    request = request,
                    render = render,
                )
            renderedSteps = solution.first
            simplexSteps = solution.second
        }
        return render(request) draw
            render.taskHomePageFilledBy(
                request = request,
                metadataForm = metadataForm,
                validatedForm = validatedForm,
                simplexMethod = simplexMethodTask,
                syntheticBasisMethod = syntheticBasisTask,
                renderedSteps = renderedSteps,
                syntheticSteps = syntheticSteps,
                simplexSteps = simplexSteps,
                solve = false,
            )
    }
}
