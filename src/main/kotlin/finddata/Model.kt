package finddata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.criteria.*
import java.io.Serializable
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class FILTERTYPE {
    EQ, // EQUAL
    NEQ, // NOT EQUAL
    LIKE,
    GT, // GREATER THAN
    LT, // LESS THAN
    GTE, // GREATER THAN EQUAL
    LTE, // LESS THAN EQUAL
    ISNULL,
    ISNOTNULL,
    IN,
    NOTIN
}

class FindData : Serializable {
    var field: String? = null
    var criteria: FILTERTYPE? = null
    var v: String? = null
    var valueVarargs: Array<String>? = null
    var or: Array<FindData>? = null
    var and: Array<FindData>? = null
    var f: Array<String>? = null
        set(value) {
            if (value != null && value.size >= 3) {
                this.field = value[0]
                this.criteria = FILTERTYPE.valueOf(value[1])
                this.v = value[2]

                this.valueVarargs = when {
                    value.size == 3 -> arrayOf(value[2])
                    value.size > 3 -> {
                        val sliceArray = value.sliceArray(2..< value.size)
                        sliceArray
                    }
                    else -> null
                }
            }
            field = value
        }

    private var joinMap: MutableList<JoinObject> = mutableListOf()
    fun <Y> addJoin(name: String, clazz: Class<Y>) = apply {
        joinMap.add(JoinObject(name, clazz))
    }

    private var joinMapQuery: MutableList<JoinObjectQuery> = mutableListOf()
    fun <Y> addJoin(name: String, clazz: Class<Y>, from: String, to: String) =
        apply { joinMapQuery.add(JoinObjectQuery(name, clazz, from, to)) }

    fun toJson(): String {
        val jsonMapper = jacksonObjectMapper()
        return jsonMapper.writeValueAsString(this)
    }

    fun <T> toPredicate(root: Root<T>, cq: CriteriaQuery<*>, cb: CriteriaBuilder, cls: Class<T>): Predicate? {
        return FindDataBuilder(cls, joinMap).buildPredicate(root, cb, this)
    }

    fun <T> toPredicateQuery(cls: Class<T>): String? {
        return FindDataQueryBuilder(cls, joinMapQuery).buildPredicateQuery(this)
    }

    companion object {
        fun fromJson(json: String): FindData {
            val jsonMapper = jacksonObjectMapper()
            return jsonMapper.readValue(json)
        }

        fun filter(field: String, operator: FILTERTYPE, vararg value: String): FindData {
            val fd = FindData()
            fd.f = arrayOf(field, operator.name, *value)
            return fd
        }

        fun and(vararg filterData: FindData): FindData {
            val fd = FindData()
            fd.and = filterData.map { it }.toTypedArray()
            return fd
        }

        fun or(vararg filterData: FindData): FindData {
            val fd = FindData()
            fd.or = filterData.map { it }.toTypedArray()
            return fd
        }
    }
}

class JoinObject(val name: String, val clazz: Class<*>)

class JoinObjectQuery(val name: String, val clazz: Class<*>, val from: String = "", val to: String = "") {
    val nameClazz = clazz.name.split(".").last()
    val aliasClazzName = nameClazz.lowercase()
}

object FindDataTool {
    val formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatDatetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatTime = DateTimeFormatter.ofPattern("HH:mm:ss")
    fun <T> getAllFields(clazz: Class<T>): MutableList<Field> {
        val fields: MutableList<Field> = mutableListOf()
        fields.addAll(clazz.superclass.declaredFields)
        fields.addAll(clazz.declaredFields)
        return fields
            .filter { f ->
                !f.annotations.any { a ->
                    a.toString().contains("JsonIgnore")
                            || a.toString().contains("Transient")
                            || a.toString().contains("OneToMany")
                }
            }.toMutableList()
    }

    fun toTypeDataExpression(fi: Field, root: Root<*>, fieldStr: String): Path<out Serializable> {
        return when (fi.type) {
            Boolean::class.java -> root.get<Boolean>(fieldStr)
            String::class.java -> root.get<String>(fieldStr)
            Int::class.java -> root.get<Int>(fieldStr)
            Double::class.java -> root.get<Double>(fieldStr)
            LocalDateTime::class.java -> root.get<LocalDateTime>(fieldStr)
            LocalDate::class.java -> root.get<LocalDate>(fieldStr)
            LocalTime::class.java -> root.get<LocalTime>(fieldStr)
            else -> root.get(fieldStr)
        }
    }

    fun toTypeJoinDataExpression(
        fi: Field, root: From<out Any, out Any>, fieldStr: String
    ): Path<out Serializable> {
        return when (fi.type) {
            Boolean::class.java -> root.get<Boolean>(fieldStr.split(".").last())
            String::class.java -> root.get<String>(fieldStr.split(".").last())
            Int::class.java -> root.get<Int>(fieldStr.split(".").last())
            Double::class.java -> root.get<Double>(fieldStr.split(".").last())
            LocalDateTime::class.java -> root.get<LocalDateTime>(fieldStr.split(".").last())
            LocalDate::class.java -> root.get<LocalDate>(fieldStr.split(".").last())
            LocalTime::class.java -> root.get<LocalTime>(fieldStr.split(".").last())
            else -> root.get(fieldStr.split(".").last())
        }
    }
}