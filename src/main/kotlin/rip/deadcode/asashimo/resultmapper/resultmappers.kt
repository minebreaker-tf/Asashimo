@file:Suppress("UNCHECKED_CAST")

package rip.deadcode.asashimo.resultmapper

import org.slf4j.LoggerFactory
import rip.deadcode.asashimo.AsashimoConfig
import rip.deadcode.asashimo.Java8DateConversionStrategy.*
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.sql.*
import java.time.*
import kotlin.reflect.KClass

private val logger = LoggerFactory.getLogger(GeneralResultMapper::class.java.`package`.name)

// TODO refactoring everything
internal fun <T : Any> ResultSet.getUnknown(i: Int, type: KClass<out T>, config: AsashimoConfig): T? {
    @Suppress("IMPLICIT_CAST_TO_ANY")
    return when (type) {
    // Directly provided by JDBC driver
        java.sql.Array::class -> getArray(i) as T?
        BigDecimal::class -> getBigDecimal(i) as T?
        InputStream::class -> getBinaryStream(i) as T?
        Blob::class -> getBlob(i) as T?
        Boolean::class -> getBoolean(i) as T?
        Byte::class -> getByte(i) as T?
        ByteArray::class -> getBytes(i) as T?
        Reader::class -> getCharacterStream(i) as T?
        Clob::class -> getClob(i) as T?
        java.sql.Date::class -> getDate(i) as T?
        Double::class -> getDouble(i) as T?
        Float::class -> getFloat(i) as T?
        Int::class -> getInt(i) as T?
        Long::class -> getLong(i) as T?
        Short::class -> getShort(i) as T?
        SQLXML::class -> getSQLXML(i) as T?
        String::class -> getString(i) as T?
        Time::class -> getTime(i) as T?
        Timestamp::class -> getTimestamp(i) as T?
        URL::class -> getURL(i) as T?

    // Manual conversion
        BigInteger::class -> getBigDecimal(i).toBigInteger() as T?
        else -> {
            when (config.java8dateConversionStrategy) {
                RAW -> null
                CONVERT -> when (type) {
                // DBのゾーンは別にするほうが良いかもしれない。要改善。
                    ZonedDateTime::class -> {
                        getTimestamp(i).toLocalDateTime().atZone(config.databaseZoneOffset) as T?
                    }
                    OffsetDateTime::class -> {
                        getTimestamp(i).toLocalDateTime().atOffset(config.databaseZoneOffset) as T?
                    }
                    OffsetTime::class -> getTime(i).toLocalTime().atOffset(config.databaseZoneOffset) as T?
                    LocalDateTime::class -> getTimestamp(i).toLocalDateTime() as T?
                    LocalDate::class -> getDate(i).toLocalDate() as T?
                    LocalTime::class -> getTime(i).toLocalTime() as T?
                    Instant::class -> getTimestamp(i).toInstant() as T?
                    else -> null
                }
                CONVERT_NONLOCAL -> when (type) {
                    ZonedDateTime::class -> {
                        getObject(i, LocalDateTime::class.java).atZone(config.databaseZoneOffset) as T?
                    }
                    OffsetDateTime::class -> {
                        getObject(i, LocalDateTime::class.java).atOffset(config.databaseZoneOffset) as T?
                    }
                    OffsetTime::class -> getObject(i, LocalTime::class.java).atOffset(config.databaseZoneOffset) as T?
                    LocalDateTime::class -> getObject(i, LocalDateTime::class.java) as T?
                    LocalDate::class -> getObject(i, LocalDate::class.java) as T?
                    LocalTime::class -> getObject(i, LocalTime::class.java) as T?
                    Instant::class -> getObject(i, Instant::class.java) as T?
                    else -> {
                        null
                    }
                }
            }
        }
    }
}

/**
 * JDBC型が要求されていた場合、対応するメソッドを使用して値を取得する.
 */
internal fun <T : Any> convertToBasicType(cls: KClass<T>, resultSet: ResultSet, config: AsashimoConfig): T? {
    return try {
        val result = resultSet.getUnknown(1, cls, config)
        logger.trace("Could not found corresponding basic type.")
        result
    } catch (e: Exception) {
        when (e) {
            is SQLException -> throw e
            else -> {
                logger.trace("", e)
                null
            }
        }
    }
}
