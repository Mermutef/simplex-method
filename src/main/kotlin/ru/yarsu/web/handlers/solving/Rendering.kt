package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexBase
import ru.yarsu.domain.simplex.SimplexMethod
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
import ru.yarsu.web.lenses.SimplexFormLenses.simplexMethodField
import ru.yarsu.web.lenses.SimplexFormLenses.syntheticBasisMethodField
import ru.yarsu.web.lenses.ValidatedFormData
import ru.yarsu.web.models.common.HomePageVM
import ru.yarsu.web.models.part.SimplexPaginationPT
import ru.yarsu.web.models.part.SimplexSolutionPT
import ru.yarsu.web.models.part.SimplexStepPT
import ru.yarsu.web.models.part.SyntheticBasisSolutionPT

fun SimplexBase.renderSteps(
    render: ContextAwareViewRender,
    request: Request,
    forSyntheticBasis: Boolean = false,
): Pair<String, List<Triple<Int, Int, Int>>> {
    var renderedSteps = ""
    val methodSteps = mutableListOf<Triple<Int, Int, Int>>()
    stepsTables.forEachIndexed { i, simplexTable ->
        val webForm =
            WebForm().with(
                basisField of simplexTable.matrix.basis,
                freeField of simplexTable.matrix.free,
                matrixField of simplexTable.matrix.coefficients,
                functionField of
                    simplexTable.function.inBasisOf(
                        matrix = simplexTable.matrix,
                        taskType = taskType,
                        invertLast = true,
                    ).coefficients,
            )
        renderedSteps +=
            (
                render(request) draw
                    SimplexStepPT(
                        stepIdx = i,
                        stepForm = webForm,
                        isSyntheticBasisStep = forSyntheticBasis,
                    )
            ).body
        methodSteps.add(
            if (i != stepsTables.lastIndex) {
                Triple(i, stepsReplaces[i]!!.first, stepsReplaces[i]!!.second)
            } else {
                Triple(i, -1, -1)
            },
        )
    }
    return Pair(renderedSteps, methodSteps)
}

fun SimplexBase.renderSolution(
    render: ContextAwareViewRender,
    request: Request,
    method: Method,
): String {
    return when (method) {
        Method.SIMPLEX_METHOD -> {
            (
                render(request) draw
                    when (val solution = getSolution()) {
                        is Success ->
                            SimplexSolutionPT(
                                vertex = solution.value.first,
                                functionValue = solution.value.second,
                            )

                        is Failure ->
                            SimplexSolutionPT(
                                hasSolution = false,
                                cause = solution.reason.text,
                                functionValue = Fraction(0, 1),
                                vertex = emptyList(),
                            )
                    }
            ).bodyString()
        }

        Method.SYNTHETIC_BASIS -> {
            (
                render(request) draw
                    when (val solution = getSolution(1)) {
                        is Success ->
                            SyntheticBasisSolutionPT(
                                simplexBasis = solution.value.first,
                                unnecessaryConstraints = solution.value.second,
                            )

                        is Failure ->
                            SyntheticBasisSolutionPT(
                                hasSolution = false,
                                cause = solution.reason.text,
                                simplexBasis = emptyList(),
                                unnecessaryConstraints = emptyList(),
                            )
                    }
            ).bodyString()
        }
    }
}

fun SimplexBase.renderPossibleReplaces(
    render: ContextAwareViewRender,
    request: Request,
    method: Method,
    wasSyntheticBasis: Boolean = false,
): String {
    return when (method) {
        Method.SIMPLEX_METHOD -> {
            val lastStep = stepsTables.last()
            val possibleReplaces = lastStep.possibleReplaces()
            (
                render(request) draw
                    SimplexPaginationPT(
                        possibleReplaces = possibleReplaces,
                        hasNextStep = possibleReplaces.isNotEmpty(),
                        hasPreviousStep = if (wasSyntheticBasis) true else stepsReplaces.isNotEmpty(),
                    )
            ).bodyString()
        }

        Method.SYNTHETIC_BASIS -> {
            val lastStep = stepsTables.last()
            val possibleReplaces =
                lastStep.possibleReplaces() +
                    lastStep
                        .possibleIdleRunningReplaces((this as SyntheticBasisMethod).syntheticBasis)
            (
                render(request) draw
                    SimplexPaginationPT(
                        possibleReplaces = possibleReplaces,
                        hasNextStep = possibleReplaces.isNotEmpty(),
                        hasPreviousStep = stepsReplaces.isNotEmpty(),
                    )
            ).bodyString()
        }
    }
}

fun ContextAwareViewRender.renderTaskHomePage(
    request: Request,
    validatedForm: ValidatedFormData,
    metadataForm: WebForm,
    solve: Boolean = false,
): Response {
    var renderedSteps = ""
    var syntheticSteps: List<Triple<Int, Int, Int>> = emptyList()
    var simplexSteps: List<Triple<Int, Int, Int>> = emptyList()
    val simplexMethodTask: SimplexMethod?
    var syntheticBasisTask: SyntheticBasisMethod? = null

    if (validatedForm.method == Method.SYNTHETIC_BASIS) {
        syntheticBasisTask =
            SyntheticBasisMethod(
                matrix = validatedForm.matrix,
                function = validatedForm.function,
                taskType = validatedForm.taskType,
            )
        if (solve) {
            syntheticBasisTask.solve()
        }
        syntheticBasisTask.renderSteps(
            request = request,
            forSyntheticBasis = true,
            render = this,
        ).let {
            renderedSteps = it.first
            if (solve) {
                renderedSteps +=
                    syntheticBasisTask.renderSolution(
                        method = validatedForm.method,
                        request = request,
                        render = this,
                    )
            }
            syntheticSteps = it.second
        }
        simplexMethodTask = syntheticBasisTask.getSimplexMethodBySolution()
    } else {
        simplexMethodTask =
            SimplexMethod(
                matrix = validatedForm.matrix,
                function = validatedForm.function,
                startBasis = validatedForm.startBasis,
                taskType = validatedForm.taskType,
            )
    }
    simplexMethodTask?.let {
        if (solve) {
            simplexMethodTask.solve()
        }
        simplexMethodTask.renderSteps(request = request, render = this).let {
            renderedSteps += it.first
            if (solve) {
                renderedSteps +=
                    simplexMethodTask.renderSolution(
                        method = Method.SIMPLEX_METHOD,
                        request = request,
                        render = this,
                    )
            }
            simplexSteps = it.second
        }
    }

    return this(request) draw
        this.taskHomePageFilledBy(
            request = request,
            metadataForm = metadataForm,
            validatedForm = validatedForm,
            simplexMethod = simplexMethodTask,
            syntheticBasisMethod = syntheticBasisTask,
            renderedSteps = renderedSteps,
            syntheticSteps = syntheticSteps,
            simplexSteps = simplexSteps,
            solve = solve,
        )
}

fun ContextAwareViewRender.taskHomePageFilledBy(
    request: Request,
    metadataForm: WebForm,
    validatedForm: ValidatedFormData,
    simplexMethod: SimplexMethod?,
    syntheticBasisMethod: SyntheticBasisMethod?,
    renderedSteps: String,
    syntheticSteps: List<Triple<Int, Int, Int>>,
    simplexSteps: List<Triple<Int, Int, Int>>,
    solve: Boolean,
): HomePageVM =
    HomePageVM(
        metadataForm =
            metadataForm
                .with(freeField of validatedForm.matrix.basis)
                .let {
                    when (validatedForm.method) {
                        Method.SYNTHETIC_BASIS ->
                            it.minus("basisJson")
                                .with(basisField of validatedForm.matrix.free)

                        Method.SIMPLEX_METHOD -> it
                    }
                },
        simplexMethodForm = simplexMethod?.let { WebForm().with(simplexMethodField of it) },
        syntheticBasisForm = syntheticBasisMethod?.let { WebForm().with(syntheticBasisMethodField of it) },
        renderedSteps = renderedSteps,
        syntheticSteps = syntheticSteps,
        simplexSteps = simplexSteps,
        nextStepForm =
            when {
                !solve ->
                    simplexMethod?.renderPossibleReplaces(
                        render = this,
                        request = request,
                        method = Method.SIMPLEX_METHOD,
                        wasSyntheticBasis = syntheticBasisMethod != null,
                    ) ?: syntheticBasisMethod?.renderPossibleReplaces(
                        render = this,
                        request = request,
                        method = Method.SYNTHETIC_BASIS,
                    )

                else -> null
            } ?: "",
    )
