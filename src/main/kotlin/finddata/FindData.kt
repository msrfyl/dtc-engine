package finddata

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FindDataBuilder<T>(private val clazz: Class<T>, private var joinMap: MutableList<JoinObject> = mutableListOf()) {

    private val allFields = FindDataTool.getAllFields(clazz)
    fun buildPredicate(root: Root<T>, criteria: CriteriaBuilder, fd: FindData): Predicate? {
        return when {
            fd.field != null && fd.criteria != null && fd.v != null -> {
                var fieldRoot = allFields.firstOrNull { f -> f.name == fd.field }
                var isJoin = false

                val rootClass = if (fd.field!!.contains(".")) {
                    val fld = fd.field!!.split(".")
                    joinMap.firstOrNull { jo ->
                        jo.name == fld.first()
                    }?.let { join ->
                        isJoin = true
                        val fieldJoin = FindDataTool.getAllFields(join.clazz)
                        fieldRoot = fieldJoin.firstOrNull { fj ->
                            fj.name == fld.last()
                        }
                        root.join<Any, Any>(join.name, JoinType.LEFT)
                    } ?: root
                } else root

                fieldRoot?.let { fi ->
                    when (fd.criteria) {
                        FILTERTYPE.EQ -> if (fi.type.isEnum) {
                            criteria.equal(
                                rootClass.get<Enum<*>>(fd.field),
                                fi.type.enumConstants.first { any -> any.toString() == fd.v }
                            )
                        } else {
                            criteria.equal(
                                if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                                else FindDataTool.toTypeDataExpression(fi, root, fd.field!!),
                                when (fi.type) {
                                    Boolean::class.java -> fd.v.toBoolean()
                                    Int::class.java -> fd.v!!.toInt()
                                    Double::class.java -> fd.v!!.toDouble()
                                    LocalDateTime::class.java -> LocalDateTime.parse(fd.v, FindDataTool.formatDatetime)
                                    LocalDate::class.java -> LocalDate.parse(fd.v, FindDataTool.formatDate)
                                    LocalTime::class.java -> LocalTime.parse(fd.v, FindDataTool.formatTime)
                                    else -> fd.v
                                }
                            )
                        }

                        FILTERTYPE.NEQ -> if (fi.type.isEnum) {
                            criteria.notEqual(
                                rootClass.get<Enum<*>>(fd.field),
                                fi.type.enumConstants.first { any -> any.toString() == fd.v }
                            )
                        } else {
                            criteria.notEqual(
                                if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                                else FindDataTool.toTypeDataExpression(fi, root, fd.field!!),
                                when (fi.type) {
                                    Boolean::class.java -> fd.v.toBoolean()
                                    Int::class.java -> fd.v!!.toInt()
                                    Double::class.java -> fd.v!!.toDouble()
                                    LocalDateTime::class.java -> LocalDateTime.parse(fd.v, FindDataTool.formatDatetime)
                                    LocalDate::class.java -> LocalDate.parse(fd.v, FindDataTool.formatDate)
                                    LocalTime::class.java -> LocalTime.parse(fd.v, FindDataTool.formatTime)
                                    Enum::class.java -> fi.type.enumConstants.first { any -> any.toString() == fd.v }
                                    else -> fd.v
                                }
                            )
                        }

                        FILTERTYPE.GT -> when (fi.type) {
                            LocalDateTime::class.java -> criteria.greaterThan(
                                rootClass.get<LocalDateTime>(fd.field),
                                LocalDateTime.parse(fd.v, FindDataTool.formatDatetime)
                            )

                            LocalDate::class.java -> criteria.greaterThan(
                                rootClass.get<LocalDate>(fd.field),
                                LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                            )

                            LocalTime::class.java -> criteria.greaterThan(
                                rootClass.get<LocalTime>(fd.field),
                                LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                            )

                            Double::class.java -> criteria.greaterThan(
                                rootClass.get<Double>(fd.field),
                                fd.v!!.toDouble()
                            )

                            else -> criteria.greaterThan(rootClass.get<Int>(fd.field), fd.v!!.toInt())
                        }

                        FILTERTYPE.LT -> when (fi.type) {
                            LocalDateTime::class.java -> criteria.lessThan(
                                rootClass.get<LocalDateTime>(fd.field),
                                LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                            )

                            LocalDate::class.java -> criteria.lessThan(
                                rootClass.get<LocalDate>(fd.field),
                                LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                            )

                            LocalTime::class.java -> criteria.lessThan(
                                rootClass.get<LocalTime>(fd.field),
                                LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                            )

                            Double::class.java -> criteria.lessThan(
                                rootClass.get<Double>(fd.field),
                                fd.v!!.toDouble()
                            )

                            else -> criteria.lessThan(rootClass.get<Int>(fd.field), fd.v!!.toInt())
                        }

                        FILTERTYPE.GTE -> when (fi.type) {
                            LocalDateTime::class.java -> criteria.greaterThanOrEqualTo(
                                rootClass.get<LocalDateTime>(fd.field),
                                LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                            )

                            LocalDate::class.java -> criteria.greaterThanOrEqualTo(
                                rootClass.get<LocalDate>(fd.field),
                                LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                            )

                            LocalTime::class.java -> criteria.greaterThanOrEqualTo(
                                rootClass.get<LocalTime>(fd.field),
                                LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                            )

                            Double::class.java -> criteria.greaterThanOrEqualTo(
                                rootClass.get<Double>(fd.field),
                                fd.v!!.toDouble()
                            )

                            else -> criteria.greaterThanOrEqualTo(rootClass.get<Int>(fd.field), fd.v!!.toInt())
                        }

                        FILTERTYPE.LTE -> when (fi.type) {
                            LocalDateTime::class.java -> criteria.lessThanOrEqualTo(
                                rootClass.get<LocalDateTime>(fd.field),
                                LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                            )

                            LocalDate::class.java -> criteria.lessThanOrEqualTo(
                                rootClass.get<LocalDate>(fd.field),
                                LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                            )

                            LocalTime::class.java -> criteria.lessThanOrEqualTo(
                                rootClass.get<LocalTime>(fd.field),
                                LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                            )

                            Double::class.java -> criteria.lessThanOrEqualTo(
                                rootClass.get<Double>(fd.field),
                                fd.v!!.toDouble()
                            )

                            else -> criteria.lessThanOrEqualTo(rootClass.get<Int>(fd.field), fd.v!!.toInt())
                        }

                        FILTERTYPE.LIKE -> criteria.like(
                            criteria.lower(
                                (if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                                else FindDataTool.toTypeDataExpression(fi, root, fd.field!!)).`as`(String::class.java)
                            ), fd.v!!.lowercase()
                        )

                        FILTERTYPE.ISNULL -> criteria.isNull(
                            if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                            else FindDataTool.toTypeDataExpression(fi, root, fd.field!!)
                        )

                        FILTERTYPE.ISNOTNULL -> criteria.isNotNull(
                            if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                            else FindDataTool.toTypeDataExpression(fi, root, fd.field!!)
                        )

                        FILTERTYPE.IN -> (if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                        else FindDataTool.toTypeDataExpression(fi, root, fd.field!!)).`in`(fd.field!!.split(";"))

                        FILTERTYPE.NOTIN -> criteria.not(
                            (if (isJoin) FindDataTool.toTypeJoinDataExpression(fi, rootClass, fd.field!!)
                            else FindDataTool.toTypeDataExpression(fi, root, fd.field!!)).`in`(fd.field!!.split(";"))
                        )

                        else -> null
                    }
                }
            }

            fd.and != null -> criteria.and(*fd.and!!.map { m -> buildPredicate(root, criteria, m) }.toTypedArray())
            fd.or != null -> criteria.or(*fd.or!!.map { m -> buildPredicate(root, criteria, m) }.toTypedArray())
            else -> null
        }
    }

}