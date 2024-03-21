import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


object DtcEngine {

    fun datetimeFormatter(pattern: String): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    private const val code = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private const val company = "DtCygY"
    fun randomCode(length: Int = 20): String {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd-HH-mm-ss-SS"))
        val result: MutableList<Char> = mutableListOf()
        now.split("-").map { i ->
            if (i.isNotBlank()) {
                try {
                    result.add(code[i.toInt()])
                } catch (_: Exception) {
                    result.add(code[(1..50).random()])
                }
            }
        }
        val number = (100000..999999).random()
        val random = "${String(result.toCharArray())}${number}$company"
        return shuffleString(random).take(length)
    }

    private fun shuffleString(input: String): String {
        val characters: MutableList<Char> = ArrayList()
        for (c in input.toCharArray()) {
            characters.add(c)
        }
        characters.shuffle()
        val shuffledString = StringBuilder()
        for (c in characters) {
            shuffledString.append(c)
        }
        return shuffledString.toString()
    }
    fun toLocalDate(string: String, format: String? = null) : LocalDate {
        return LocalDate.parse(string, DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd"))
    }
    fun toLocalDateTime(string: String, format: String? = null) : LocalDateTime {
        return LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd HH:mm:ss"))
    }
    fun toLocalTime(string: String, format: String? = null) : LocalTime {
        return LocalTime.parse(string, DateTimeFormatter.ofPattern(format ?: "HH:mm:ss"))
    }
    fun toString(dateTime: LocalDateTime, format: String? = null) : String {
        return dateTime.format(DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd HH:mm:ss"))
    }
    fun toString(date: LocalDate, format: String? = null) : String {
        return date.format(DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd"))
    }
    fun toString(time: LocalTime, format: String? = null) : String {
        return time.format(DateTimeFormatter.ofPattern(format ?: "HH:mm:ss"))
    }
    fun startAtDay(date: LocalDate) : LocalDateTime {
        return LocalDateTime.of(date, LocalTime.of(0,0,0))
    }
    fun endAtDay(date: LocalDate) : LocalDateTime {
        return LocalDateTime.of(date, LocalTime.of(23,59,59))
    }
}