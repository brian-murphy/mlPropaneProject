package main

import algorithms.classifiers.Classifier
import algorithms.classifiers.RbfClassifier
import datasets.DataSet
import datasets.Instance
import datasets.PcaPropaneDataReader
import analysis.statistical.crossvalidation.SyncCrossValidation
import analysis.statistical.LearningCurve
import analysis.statistical.Result
import analysis.statistical.crossvalidation.AsyncCrossValidator
import analysis.statistical.errorfunction.AvgAbsoluteError
import datasets.PropaneDataReader
import java.util.*

fun main(args: Array<String>) {
    println(rbfError(PropaneDataReader().propaneDataSet))
}

fun rbfLearningCurve() {
    val rbf = RbfClassifier()
    rbf.setParams(createParams(15, .1, false))

    val absoluteError = {error:Double -> Math.abs(error)}

    val learningCurve = LearningCurve(PcaPropaneDataReader().propaneDataSet, rbf, absoluteError)
    val csv = learningCurve.run()

    println(csv)
}

fun runRbf(dataSet: DataSet<Instance>, cgd: Boolean, tolerance: Double, rbfCount: Int): Result {
    val params = createParams(rbfCount, tolerance, cgd)

    val rbf = RbfClassifier()

    rbf.setParams(params)

    val numFolds = 10

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    return crossValidation.run()
}


fun rbfError(dataSet: DataSet<Instance>): Double {

    val rbf = (RbfClassifier() as Classifier).javaClass

    val params = RbfClassifier.RbfParams(25, 0.1, false)

    val crossValidation = AsyncCrossValidator(dataSet, rbf, params, AvgAbsoluteError().asErrorFunction())

    return crossValidation.run().second
}

fun testConjugateGradientDescent(dataSet: DataSet<Instance>) {

    val paramsWithCgd = newDefaultParams()
    paramsWithCgd.put(RbfClassifier.KEY_CONJUGATE_GRADIENT_DESCENT, true)

    val paramsWithoutCgd = newDefaultParams()
    paramsWithoutCgd.put(RbfClassifier.KEY_CONJUGATE_GRADIENT_DESCENT, false)

    val rbf = RbfClassifier()
    rbf.setParams(paramsWithCgd)

    val numFolds = 10

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val resultsWithCgd = crossValidation.run()

    rbf.setParams(paramsWithoutCgd)

    val resultsWithoutCgd = crossValidation.run()

    println("\nvalidation error with Conjugate Gradient Descent: " + resultsWithCgd.meanValidationError)
    println("\nvalidation error without Conjugate Gradient Descent: " + resultsWithoutCgd.meanValidationError)
}

fun findBestTolerance(dataSet: DataSet<Instance>) {
    val rbf = RbfClassifier()

    val numFolds = 10

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val params = newDefaultParams()

    println("Tolerance,TrainingError,ValidationError")
    var divisor = 10
    while (divisor < 100000) {
        val tolerance = 1.0 / divisor
        params.put(RbfClassifier.KEY_TOLERANCE, tolerance)

        rbf.setParams(params)

        val result = crossValidation.run()

        println("$tolerance,${result.meanTrainingError},${result.meanValidationError}")
        divisor = divisor shl 1
    }
}

fun findBestRbfCount(dataSet: DataSet<Instance>, minValueToTest: Int, interval: Int, maxValueToTest: Int) {
    val rbf = RbfClassifier()

    val numFolds = 10

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val params = newDefaultParams()

    println("RbfCount,TrainingError,ValidationError")
    var rbfCount = minValueToTest
    while (rbfCount <= maxValueToTest) {
        params.put(RbfClassifier.KEY_NUM_RBFS, rbfCount)

        rbf.setParams(params)

        val result = crossValidation.run()

        println("$rbfCount,${result.meanTrainingError},${result.meanValidationError}")
        rbfCount += interval
    }
}

private fun newDefaultParams(): MutableMap<String, Any> {
    val params = HashMap<String, Any>()

    params.put(RbfClassifier.KEY_TOLERANCE, 0.00001)

    params.put(RbfClassifier.KEY_NUM_RBFS, 5)

    params.put(RbfClassifier.KEY_CONJUGATE_GRADIENT_DESCENT, false)

    return params
}

private fun createParams(rbfCount: Int, tolerance: Double, cgd: Boolean): Map<String, Any> {
    val params = HashMap<String, Any>()

    params.put(RbfClassifier.KEY_TOLERANCE, tolerance)

    params.put(RbfClassifier.KEY_NUM_RBFS, rbfCount)

    params.put(RbfClassifier.KEY_CONJUGATE_GRADIENT_DESCENT, cgd)

    return params
}
