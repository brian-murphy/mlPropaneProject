@file:JvmName("GeneralUtils")

package util

import analysis.statistical.ElapsedTime
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.*

object GeneralUtils {

    fun getResourceNamesInPackage(path: String): List<String> {
        val fileNames = mutableListOf<String>()

        try {
            val inputStream = getResourceAsStream(path)
            val br = BufferedReader(InputStreamReader(inputStream))

            var resource: String? = br.readLine()

            while (resource != null) {
                fileNames.add(resource)
                resource = br.readLine()
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
            throw RuntimeException()
        }

        return fileNames.toList()
    }

    private fun getResourceAsStream(resource: String): InputStream {
        val inputStream = getContextClassLoader().getResourceAsStream(resource);

        return inputStream ?: inputStream.javaClass.getResourceAsStream(resource)
    }

    private fun getContextClassLoader(): ClassLoader {
        return javaClass.classLoader
    }

    fun getCsvParser(filePath: String): CSVParser {

        val csvContent = readFromPackage(filePath)

        try {
            return CSVParser.parse(csvContent, CSVFormat.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        throw RuntimeException("couldn't create parser")
    }

    fun writeToFile(fileName: String, content: String) {
        val writer = File(fileName).bufferedWriter()
        writer.write(content)
        writer.flush()
        writer.close()
    }

    fun readFromPackage(filePath: String) : String {
        val inputStream = javaClass.getResourceAsStream(filePath)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val content = reader.readText()

        reader.close()
        inputStream.close()

        return content
    }

    fun timeThis(function: () -> Unit): Double {
        ElapsedTime.tic()
        function()
        return ElapsedTime.toc()
    }

    fun <T> serializeObject(fileName: String, toSerialize: T) {
        try {
            val fileOut = FileOutputStream(fileName)
            val out = ObjectOutputStream(fileOut)

            out.writeObject(toSerialize)

            out.close()
            fileOut.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun <T> deserializeObject(fileName: String): T {
        var obj: T? = null

        try {
            val fileIn = FileInputStream("/tmp/employee.ser")
            val objectInputStream = ObjectInputStream(fileIn)

            obj = objectInputStream.readObject() as T

            objectInputStream.close()
            fileIn.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return obj
    }
}


