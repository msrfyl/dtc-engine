package finddata

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FindDataQueryBuilder<T>(
    private val clazz: Class<T>,
    private var joinMap: MutableList<JoinObjectQuery> = mutableListOf()
) {

    private val allFields = FindDataTool.getAllFields(clazz)
    private val className = clazz.name.split(".").last()
    private val aliasClassName = className.lowercase()

    fun buildPredicateQuery(fd: FindData): String? {
        return try {
            when {
                fd.field != null && fd.criteria != null && fd.v != null -> {
                    var fieldRoot = allFields.firstOrNull { f -> f.name == fd.field }
                    val rootClass = if (fd.field!!.contains(".")) {
                        val fld = fd.field!!.split(".")
                        joinMap.firstOrNull { jo ->
                            jo.name == fld.first()
                        }?.let { join ->
                            val fieldJoin = FindDataTool.getAllFields(join.clazz)
                            fieldRoot = fieldJoin.firstOrNull { fj -> fj.name == fld.last() }
                            join.clazz.name.split(".").last().lowercase()
                        } ?: aliasClassName
                    } else aliasClassName

                    fieldRoot?.let { fi ->
                        when (fd.criteria) {
                            FILTERTYPE.EQ -> if (fi.type.isEnum) {
                                " ${rootClass}.${fi.name} = ${fi.type.enumConstants.first { any -> any.toString() == fd.v!! }} "
                            } else {
                                " ${rootClass}.${fi.name} = ${
                                    when (fi.type) {
                                        Boolean::class.java -> " '${fd.v!!}' "
                                        Int::class.java -> " ${fd.v!!} "
                                        Double::class.java -> " ${fd.v!!} "
                                        LocalDateTime::class.java -> " '${
                                            LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                        }' "

                                        LocalDate::class.java -> " '${
                                            LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                        }' "

                                        LocalTime::class.java -> " '${
                                            LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                        }' "

                                        else -> " '${fd.v!!}' "
                                    }
                                } "
                            }

                            FILTERTYPE.NEQ -> if (fi.type.isEnum) {
                                " ${rootClass}.${fi.name} != ${fi.type.enumConstants.first { any -> any.toString() == fd.v!! }} "
                            } else {
                                " ${rootClass}.${fi.name} != ${
                                    when (fi.type) {
                                        Boolean::class.java -> " '${fd.v!!}' "
                                        Int::class.java -> " ${fd.v!!} "
                                        Double::class.java -> " ${fd.v!!} "
                                        LocalDateTime::class.java -> " '${
                                            LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                        }' "

                                        LocalDate::class.java -> " '${
                                            LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                        }' "

                                        LocalTime::class.java -> " '${
                                            LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                        }' "

                                        else -> " '${fd.v!!}' "
                                    }
                                } "
                            }

                            FILTERTYPE.GT -> when (fi.type) {
                                LocalDateTime::class.java -> " ${rootClass}.${fi.name} > '${
                                    LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                }' "

                                LocalDate::class.java -> " ${rootClass}.${fi.name} > '${
                                    LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                }' "

                                LocalTime::class.java -> " ${rootClass}.${fi.name} > '${
                                    LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                }' "

                                Double::class.java -> " ${rootClass}.${fi.name} > ${fd.v!!} "
                                else -> " ${rootClass}.${fi.name} > ${fd.v!!} "
                            }

                            FILTERTYPE.LT -> when (fi.type) {
                                LocalDateTime::class.java -> " ${rootClass}.${fi.name} < '${
                                    LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                }' "

                                LocalDate::class.java -> " ${rootClass}.${fi.name} < '${
                                    LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                }' "

                                LocalTime::class.java -> " ${rootClass}.${fi.name} < '${
                                    LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                }' "

                                Double::class.java -> " ${rootClass}.${fi.name} < ${fd.v!!} "
                                else -> " ${rootClass}.${fi.name} < ${fd.v!!} "
                            }

                            FILTERTYPE.GTE -> when (fi.type) {
                                LocalDateTime::class.java -> " ${rootClass}.${fi.name} >= '${
                                    LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                }' "

                                LocalDate::class.java -> " ${rootClass}.${fi.name} >= '${
                                    LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                }' "

                                LocalTime::class.java -> " ${rootClass}.${fi.name} >= '${
                                    LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                }' "

                                Double::class.java -> " ${rootClass}.${fi.name} >= ${fd.v!!} "
                                else -> " ${rootClass}.${fi.name} >= ${fd.v!!} "
                            }

                            FILTERTYPE.LTE -> when (fi.type) {
                                LocalDateTime::class.java -> " ${rootClass}.${fi.name} <= '${
                                    LocalDateTime.parse(fd.v!!, FindDataTool.formatDatetime)
                                }' "

                                LocalDate::class.java -> " ${rootClass}.${fi.name} <= '${
                                    LocalDate.parse(fd.v!!, FindDataTool.formatDate)
                                }' "

                                LocalTime::class.java -> " ${rootClass}.${fi.name} <= '${
                                    LocalTime.parse(fd.v!!, FindDataTool.formatTime)
                                }' "

                                Double::class.java -> " ${rootClass}.${fi.name} <= ${fd.v!!} "
                                else -> " ${rootClass}.${fi.name} <= ${fd.v!!} "
                            }

                            FILTERTYPE.LIKE -> " ${rootClass}.${fi.name} LIKE ${fd.v!!} "
                            FILTERTYPE.ISNULL -> " ${rootClass}.${fi.name} IS NULL "
                            FILTERTYPE.ISNOTNULL -> " ${rootClass}.${fi.name} IS NOT NULL "
                            FILTERTYPE.IN -> " (${
                                fd.v!!.split(";").joinToString(" OR ") { m -> " ${rootClass}.${fi.name} = '$m' " }
                            }) "

                            FILTERTYPE.NOTIN -> " (${
                                fd.v!!.split(";").joinToString(" AND ") { m -> " ${rootClass}.${fi.name} != '$m' " }
                            }) "

                            else -> ""
                        }
                    }
                }

                fd.and != null -> " ( ${fd.and!!.map { m -> buildPredicateQuery(m) }.joinToString(" AND ")} ) "
                fd.or != null -> " ( ${fd.or!!.map { m -> buildPredicateQuery(m) }.joinToString(" OR ")} ) "
                else -> ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}