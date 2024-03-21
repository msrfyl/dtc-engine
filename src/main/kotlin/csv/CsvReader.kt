package csv

import mapper.DtcMapper
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Files

class CsvReader(private val fileCsv: File) {
    private var removeFirstLine: Boolean = false
    private var withHeader: Boolean = false
    private var header: Array<String> = arrayOf()
    private var maxSize: Long = 10000000

    fun removeFirstLine(i: Boolean) = apply { removeFirstLine = i }
    fun withHeader(i: Boolean) = apply { withHeader = i }
    fun setHeader(vararg i: String) = apply { header = arrayOf(*i) }
    fun setMaxSize(i: Long) = apply { maxSize = i }

    fun <T> read(clazz: Class<T>): List<T> {
        return when {
            String(fileCsv.readBytes()).trim().isEmpty() -> throw Exception("File is empty")
            Files.size(fileCsv.toPath()) > maxSize -> throw Exception("Maximum size file is $maxSize")
            else -> {
                val csvData = String(fileCsv.readBytes()).split("\n")
                val headerKey: List<String>? = when {
                    withHeader -> csvData[
                        if (removeFirstLine) 1 else 0
                    ].split(",").map { it.replace("\"", "") }

                    header.isNotEmpty() -> header.toList()
                    else -> null
                }
                val data = csvToMap(BufferedReader(FileReader(fileCsv)), headerKey)
                data.map {
                    DtcMapper.readValue(DtcMapper.writeValueAsString(it), clazz)
                }
            }
        }
    }

    private fun csvToMap(
        fromCsv: BufferedReader,
        header: List<String>? = null
    ): MutableList<Map<String, String>> {
        val li: MutableList<Map<String, String>> = mutableListOf()
        val head = header ?: readLine(fromCsv.readLine())
        (when {
            withHeader && removeFirstLine -> fromCsv.lines().skip(2)
            withHeader || removeFirstLine -> fromCsv.lines().skip(1)
            else -> fromCsv.lines()
        }).forEach {
            li.add(
                readLine(it).take(head.size).mapIndexed { i, s ->
                    Pair(
                        head[i].replace("\"", "")
                            .replace("\n", "")
                            .replace("\r", ""),
                        s
                    )
                }.toMap()
            )
        }
        return li
    }

    private fun readLine(str: String): List<String> {
        val li = str.split("\",\"")
        return li.mapIndexed { index, s ->
            when (index) {
                0 -> if (s.trim().first() != '"') "$s\""
                else s.substring(if (s.first() == '"') 1 else 0, s.length)

                li.size - 1 -> if (s.trim().last() != '"') "\"$s"
                else s.substring(0, if (s.last() == '"') s.length - 1 else s.length)

                else -> s
            }
        }
    }

}