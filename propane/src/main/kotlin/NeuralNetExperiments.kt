@file:JvmName("NeuralNetExperiments")

import algorithms.classifiers.Classifier
import algorithms.classifiers.NeuralNetClassifier
import analysis.statistical.crossvalidation.SyncCrossValidation
import analysis.statistical.LearningCurve
import analysis.statistical.crossvalidation.AsyncCrossValidator
import analysis.statistical.errorfunction.AvgAbsoluteError
import datasets.*
import util.Csv
import util.CsvPrinter
import util.absoluteError

fun main(args: Array<String>) {
    val nNetParams = NeuralNetClassifier.createParams(intArrayOf(9, 8), 0.006f, 500)

    val propaneDataReader = PropaneDataReader(true)

    println("csf")
    neuralNetLearningCurve(propaneDataReader.propaneDataSet, ::absoluteError, nNetParams)
}

fun nNetError(dataSet: DataSet<Instance>): Double {
    val nNet = (NeuralNetClassifier() as Classifier).javaClass

    val params = NeuralNetClassifier.NNetParams(intArrayOf(9, 8), 0.006f, 500)

    val cv  = AsyncCrossValidator(dataSet, nNet,params, AvgAbsoluteError().asErrorFunction())

    return cv.run().second
}

fun neuralNetLearningCurve(dataSet: DataSet<Instance>, errorFunction: (Double) -> Double, params: Map<String, Any>) {
    val nNet = NeuralNetClassifier()

    nNet.setParams(params)

    val learningCurve = LearningCurve(dataSet, nNet, errorFunction, 10)

    val csv = learningCurve.run()

    println(csv.toString())
}

fun findHiddenLayerLength(dataSet: DataSet<Instance>) {

    val csv = CsvPrinter(arrayOf("HiddenLayerSize", "TrainingError", "ValidationError"))

    for (hiddenLayerLength in 2..149) {
        val neuralNet = NeuralNetClassifier()

        neuralNet.setParams(NeuralNetClassifier.createParams(intArrayOf(hiddenLayerLength), .01f, 1000))

        val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 10, dataSet, neuralNet)

        val result = crossValidation.run()

        csv.addRowAndPrint(hiddenLayerLength.toDouble(), result.meanTrainingError, result.meanValidationError)
    }
}

fun findTrainingErrorThreshold(dataSet: DataSet<Instance>) {

    println("TrainingErrorThreshold,TrainingError,ValidationError")

    var divisor = 2
    while (divisor < 1025) {
        val neuralNet = NeuralNetClassifier()

        val trainingErrorThreshold = 1.0f / divisor

        neuralNet.setParams(NeuralNetClassifier.createParams(intArrayOf(9, 8), trainingErrorThreshold, 1000))

        val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 10, dataSet, neuralNet)

        val result = crossValidation.run()

        println("$trainingErrorThreshold,${result.meanTrainingError},${result.meanValidationError}")
        divisor = divisor shl 1
    }
}

fun moonshot() {
    val neuralNet = NeuralNetClassifier()

    neuralNet.setParams(NeuralNetClassifier.createParams(intArrayOf(9, 8), 0.006f, 500))

    val dataSet = PcaPropaneDataReader().propaneDataSet as DataSet<Instance>

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 60, dataSet, neuralNet)

    val result = crossValidation.run()

    println("validation Error:" + result.meanValidationError)
}

fun moonshot2() {

    val neuralNet = NeuralNetClassifier()

    neuralNet.setParams(NeuralNetClassifier.createParams(intArrayOf(100), .005f, 10000))

    val dataSet = PropaneDataReader().propaneDataSet

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 10, dataSet, neuralNet)

    val result = crossValidation.run()

    println("validation Error:" + result.meanValidationError)
}

fun gridSearchTwoLayerStructure(dataSet: DataSet<Instance>, maxFirstLayer: Int, maxSecondLayer: Int) {

    val csv = Csv("FirstHiddenLayerLength", "SecondHiddenLayerLength", "TrainingError", "ValidationError")

    for (firstLayerLength in 2..maxFirstLayer - 1) {

        var secondLayerLength = 2
        while (secondLayerLength < firstLayerLength && secondLayerLength < maxSecondLayer) {
            val neuralNet = NeuralNetClassifier()

            neuralNet.setParams(NeuralNetClassifier.createParams(intArrayOf(firstLayerLength, secondLayerLength), .006f, 10000))

            val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 10, dataSet, neuralNet)

            val result = crossValidation.run()

            csv.addRowAndPrint(firstLayerLength, secondLayerLength, result.meanTrainingError, result.meanValidationError)
            secondLayerLength++

        }
    }

    println(csv)
}
