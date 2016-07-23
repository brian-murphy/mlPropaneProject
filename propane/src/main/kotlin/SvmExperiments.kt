@file:JvmName("SvmExperiments")

import algorithms.classifiers.Classifier
import algorithms.classifiers.SvmClassifier
import datasets.DataSet
import datasets.Instance
import datasets.PcaPropaneDataReader
import datasets.PropaneDataReader
import analysis.statistical.crossvalidation.SyncCrossValidation
import analysis.statistical.Result
import analysis.statistical.crossvalidation.AsyncCrossValidator
import analysis.statistical.errorfunction.AvgAbsoluteError

fun main(args: Array<String>) {
    println(svmError(PropaneDataReader().propaneDataSet))
}

fun runSvm(dataSet: DataSet<Instance>, kernel: SvmClassifier.Kernel, c: Double, gamma: Double) : Result {
    val svm = SvmClassifier()

    svm.setParams(SvmClassifier.createParams(kernel, c, gamma))

    val crossValidation = SyncCrossValidation(SyncCrossValidation.AbsoluteError(), 20, dataSet, svm)

    return crossValidation.run()
}


fun svmError(dataSet: DataSet<Instance>): Double {
    val svm = (SvmClassifier() as Classifier).javaClass

    val params = SvmClassifier.SvmParams(SvmClassifier.Kernel.Polynomial, 1.0, 1.0)

    val crossValidation = AsyncCrossValidator(dataSet, svm, params, AvgAbsoluteError().asErrorFunction())

    return crossValidation.run()
}

fun comparePropaneDataSets(kernel: SvmClassifier.Kernel, c: Double, gamma: Double) {
    val pcaDataResults = runSvm(PcaPropaneDataReader().propaneDataSet as DataSet<Instance>, kernel, c, gamma)
    val originalDataResults = runSvm(PropaneDataReader().propaneDataSet as DataSet<Instance>, kernel, c, gamma)

    println("error running svm with kernel $kernel, c $c, and gamma $gamma on original data:${originalDataResults.meanValidationError}")
    println("error running svm with kernel $kernel, c $c, and gamma $gamma on pca data:${pcaDataResults.meanValidationError}")
}

fun findBestC(dataSet: DataSet<Instance>) {
    println("C,TrainingError,ValidationError")

    var c = 1000000.0
    while (c >= .0000001) {
        c /= 10.0

        val result = runSvm(dataSet, SvmClassifier.Kernel.RadialBasisFunction, c, 0.1)

        println("$c,${result.meanTrainingError},${result.meanValidationError}")
    }
}