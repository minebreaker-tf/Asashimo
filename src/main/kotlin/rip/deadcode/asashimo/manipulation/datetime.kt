package rip.deadcode.asashimo.manipulation

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp
import java.time.*
import kotlin.reflect.KClass

class ConvertToClassicRetriever(
        val databaseOffset: ZoneOffset
) : Retriever {

    private val retrievableClass = listOf(
            ZonedDateTime::class,
            OffsetDateTime::class,
            OffsetTime::class,
            LocalDateTime::class,
            LocalTime::class,
            LocalTime::class,
            Instant::class
    )

    override fun <T : Any> retrievable(cls: KClass<T>): Boolean {
        return retrievableClass.contains(cls)
    }

    override fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T? {

        @Suppress("UNCHECKED_CAST")
        return when (cls) {
            ZonedDateTime::class -> {
                rs.getTimestamp(index).toLocalDateTime().atZone(databaseOffset) as T?
            }
            OffsetDateTime::class -> {
                rs.getTimestamp(index).toLocalDateTime().atOffset(databaseOffset) as T?
            }
            OffsetTime::class -> rs.getTime(index).toLocalTime().atOffset(databaseOffset) as T?
            LocalDateTime::class -> rs.getTimestamp(index).toLocalDateTime() as T?
            LocalDate::class -> rs.getDate(index).toLocalDate() as T?
            LocalTime::class -> rs.getTime(index).toLocalTime() as T?
            Instant::class -> rs.getTimestamp(index).toInstant() as T?
            else -> throw RuntimeException()
        }
    }

}

class ConvertToClassicSetter(val databaseOffset: ZoneOffset) : Setter {

    override fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean {
        val value: Any = when (param) {
            is ZonedDateTime -> {
                Timestamp.valueOf(
                        param.withZoneSameInstant(databaseOffset).toLocalDateTime())
            }
            is OffsetDateTime -> {
                Timestamp.valueOf(
                        param.withOffsetSameInstant(databaseOffset).toLocalDateTime())
            }
            is OffsetTime -> {
                Time.valueOf(param.withOffsetSameInstant(databaseOffset).toLocalTime())
            }
            is LocalDateTime -> Timestamp.valueOf(param)
            is LocalDate -> java.sql.Date.valueOf(param)
            is LocalTime -> Time.valueOf(param)
            is Instant -> Timestamp.from(param)
            else -> return false
        }

        stmt.setObject(index, value)
        return true
    }
}

class ConvertNonLocalToLocalRetriever(
        val databaseOffset: ZoneOffset
) : Retriever {

    private val retrievableClass = listOf(
            ZonedDateTime::class,
            OffsetDateTime::class,
            OffsetTime::class
    )

    override fun <T : Any> retrievable(cls: KClass<T>): Boolean {
        return retrievableClass.contains(cls)
    }

    override fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T? {

        @Suppress("UNCHECKED_CAST")
        return when (cls) {
            ZonedDateTime::class -> {
                rs.getObject(index, LocalDateTime::class.java).atZone(databaseOffset) as T?
            }
            OffsetDateTime::class -> {
                rs.getObject(index, LocalDateTime::class.java).atOffset(databaseOffset) as T?
            }
            OffsetTime::class -> rs.getObject(index, LocalTime::class.java).atOffset(databaseOffset) as T?
            else -> throw RuntimeException()
        }
    }
}

class ConvertNonLocalToLocalSetter(
        val databaseOffset: ZoneOffset
) : Setter {

    override fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean {
        val value: Any = when (param) {
            is ZonedDateTime -> {
                param.withZoneSameInstant(databaseOffset).toLocalDateTime()
            }
            is OffsetDateTime -> {
                param.withOffsetSameInstant(databaseOffset).toLocalDateTime()
            }
            is OffsetTime -> {
                param.withOffsetSameInstant(databaseOffset).toLocalTime()
            }
            else -> return false
        }

        stmt.setObject(index, value)
        return true
    }
}
