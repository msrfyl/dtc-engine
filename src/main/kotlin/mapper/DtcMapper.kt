package mapper

import DtcEngine
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DtcMapper private constructor() {

    companion object {
        fun writeValueAsString(any: Any): String = jacksonObjectMapper().writeValueAsString(any)
        fun <T> readValue(json: String, clazz: Class<T>): T = jacksonObjectMapper().readValue(json, clazz)
        fun <T> readValue(any: Any, clazz: Class<T>): T = jacksonObjectMapper().readValue(writeValueAsString(any), clazz)
    }

    data class Data(val data: List<Serializable>) {

        private var keyHeader: MutableList<String> = mutableListOf()
        private var formatDatetime: String = "yyyy-MM-dd HH:mm:ss"
        private var formatDate: String = "yyyy-MM-dd"
        private var formatTime: String = "HH:mm:ss"
        fun header(vararg i: String) = apply { i.forEach { keyHeader.add(it) } }
        fun datetimeFormat(i: String) = apply { formatDatetime = i }
        fun dateFormat(i: String) = apply { formatDate = i }
        fun timeFormat(i: String) = apply { formatTime = i }

        fun toMap() : List<Map<String, String>> {
            return data.map { toObjectMap(it) }
        }

        private fun toObjectMap(obj: Serializable) : Map<String, String> {
            return obj.javaClass.declaredFields
                .map { f ->
                    f.isAccessible = true
                    Pair(f.name, f.get(obj))
                }.filter { p ->
                    if (keyHeader.isNotEmpty()) keyHeader.any { h -> p.first == h } else true
                }.associate { p ->
                    val v = when (val dtVal = p.second) {
                        null -> ""
                        is LocalDateTime -> dtVal.format(DtcEngine.datetimeFormatter(formatDatetime))
                        is LocalTime -> dtVal.format(DtcEngine.datetimeFormatter(formatDate))
                        is LocalDate -> dtVal.format(DtcEngine.datetimeFormatter(formatTime))
                        is Enum<*> -> dtVal.toString().trim().lowercase().replaceFirstChar { i -> i.uppercase() }
                        is Boolean -> if (dtVal.toString().trim().lowercase() == "true") "Ya" else "Tidak"
                        else -> dtVal.toString().trim()
                    }
                    Pair(p.first, v)
                }
        }

    }

}