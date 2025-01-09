// package ru.yarsu.web.handlers.calculating
//
// import org.http4k.core.HttpHandler
// import org.http4k.core.Request
// import org.http4k.core.Response
// import org.http4k.core.with
// import org.http4k.lens.WebForm
// import ru.yarsu.domain.simplex.Method
// import ru.yarsu.web.context.templates.ContextAwareViewRender
// import ru.yarsu.web.draw
// import ru.yarsu.web.lenses.SimplexFormLenses.basisField
// import ru.yarsu.web.lenses.SimplexFormLenses.freeField
// import ru.yarsu.web.lenses.SimplexFormLenses.from
// import ru.yarsu.web.lenses.SimplexFormLenses.functionField
// import ru.yarsu.web.lenses.SimplexFormLenses.matrixField
// import ru.yarsu.web.lenses.SimplexFormLenses.methodField
// import ru.yarsu.web.lenses.SimplexFormLenses.taskMetadataForm
// import ru.yarsu.web.lenses.SimplexFormLenses.taskTypeField
// import ru.yarsu.web.models.common.HomePageVM
// import ru.yarsu.web.models.part.SimplexStepPT
// import ru.yarsu.web.notFound
//
// class NextStepHandler(
//    private val render: ContextAwareViewRender,
// ) : HttpHandler {
//    override fun invoke(request: Request): Response {
//        val metadataForm = taskMetadataForm from request
//        if (metadataForm.errors.isNotEmpty()) {
//            println(metadataForm.errors)
//            return render(request) draw HomePageVM(metadataForm = metadataForm)
//        }
//        val matrixCoefficients = matrixField from metadataForm
//        val taskType = taskTypeField from metadataForm
//        val method = methodField from metadataForm
//        val n = matrixCoefficients.first().size
//        val m = matrixCoefficients.size
//        val defaultBasis = (0..<m).toList()
//        val defaultFree = (m..<n - 1).toList()
//
//        when(method) {
//            Method.SIMPLEX_METHOD -> {
//
//            }
//            Method.SYNTHETIC_BASIS -> {
//
//            }
//        }
//        val currentTask = currentTaskField(metadataForm) ?: return notFound()
//
//        currentTask.nextStep()
//
//        var renderedSteps = ""
//        val syntheticSteps = mutableListOf<Triple<Int, Int, Int>>()
//        val trueSteps = mutableListOf<Triple<Int, Int, Int>>()
//        currentTask.syntheticSimplexTables.forEachIndexed { i, simplexTable ->
//            val webForm =
//                WebForm().with(
//                    basisField of simplexTable.matrix.basis,
//                    freeField of simplexTable.matrix.free,
//                    matrixField of simplexTable.matrix.coefficients,
//                    functionField of simplexTable.function.inBasisOf(simplexTable.matrix, taskType).coefficients,
//                )
//            renderedSteps +=
//                (
//                        render(request) draw
//                                SimplexStepPT(
//                                    stepIdx = i,
//                                    stepForm = webForm,
//                                    isLast = i == currentTask.syntheticSimplexTables.lastIndex,
//                                    isSyntheticBasisStep = true,
//                                )
//                        ).body
//            syntheticSteps.add(
//                if (i != currentTask.syntheticSimplexTables.lastIndex) {
//                    Triple(i, currentTask.syntheticReplaces[i]!!.first, currentTask.syntheticReplaces[i]!!.second)
//                } else {
//                    Triple(i, -1, -1)
//                },
//            )
//        }
//
//        currentTask.simplexTables.forEachIndexed { i, simplexTable ->
//            val webForm =
//                WebForm().with(
//                    basisField of simplexTable.matrix.basis,
//                    freeField of simplexTable.matrix.free,
//                    matrixField of simplexTable.matrix.coefficients,
//                    functionField of simplexTable.function.inBasisOf(simplexTable.matrix, taskType).coefficients,
//                )
//            renderedSteps +=
//                (
//                        render(request) draw
//                                SimplexStepPT(
//                                    stepIdx = i,
//                                    stepForm = webForm,
//                                    isLast = i == currentTask.simplexTables.lastIndex,
//                                )
//                        ).body
//            trueSteps.add(
//                if (i != currentTask.simplexTables.lastIndex) {
//                    Triple(i, currentTask.simplexReplaces[i]!!.first, currentTask.simplexReplaces[i]!!.second)
//                } else {
//                    Triple(i, -1, -1)
//                },
//            )
//        }
//        return render(request) draw
//                HomePageVM(
//                    form =
//                        metadataForm
//                            .minus("currentTaskJson")
//                            .with(currentTaskField of currentTask)
//                            .with(freeField of defaultFree)
//                            .let {
//                                if (method == Method.SYNTHETIC_BASIS) {
//                                    it.minus("basisJson").with(basisField of defaultBasis)
//                                } else {
//                                    it
//                                }
//                            },
//                    renderedSteps = renderedSteps,
//                    syntheticSteps = syntheticSteps,
//                    trueSteps = trueSteps,
//                )
//    }
// }
