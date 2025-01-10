package ru.yarsu.web.handlers.solving

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.WebForm
import ru.yarsu.domain.entities.Fraction
import ru.yarsu.domain.simplex.Method
import ru.yarsu.domain.simplex.SimplexBase
import ru.yarsu.domain.simplex.SyntheticBasisMethod
import ru.yarsu.web.context.templates.ContextAwareViewRender
import ru.yarsu.web.draw
import ru.yarsu.web.lenses.SimplexFormLenses.basisField
import ru.yarsu.web.lenses.SimplexFormLenses.freeField
import ru.yarsu.web.lenses.SimplexFormLenses.functionField
import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
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
