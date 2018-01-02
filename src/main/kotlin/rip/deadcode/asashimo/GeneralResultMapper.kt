package rip.deadcode.asashimo

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.CaseFormat.*
import org.slf4j.LoggerFactory
import java.beans.Introspector
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.sql.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
object GeneralResultMapper {

    private val logger = LoggerFactory.getLogger(GeneralResultMapper::class.java)

    fun <T : Any> map(cls: KClass<T>, resultSet: ResultSet): T {
        return convertToBasicType(cls, resultSet)
                ?: convertWithAllArgsConstructor(cls, resultSet)
                ?: convertAsBean(cls, resultSet)
                ?: throw AsashimoException("Failed to map ResultSet to class '${cls}'")
    }

    /**
     * JDBC型が要求されていた場合、対応するメソッドを使用して値を取得する.
     */
    @VisibleForTesting
    internal fun <T : Any> convertToBasicType(cls: KClass<T>, resultSet: ResultSet): T? {
        return try {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            return when (cls) {
            // Directly provided by JDBC driver
                java.sql.Array::class -> resultSet.getArray(1)
                BigDecimal::class -> resultSet.getBigDecimal(1)
                InputStream::class -> resultSet.getBinaryStream(1)
                Blob::class -> resultSet.getBlob(1)
                Boolean::class -> resultSet.getBoolean(1)
                Byte::class -> resultSet.getByte(1)
                ByteArray::class -> resultSet.getBytes(1)
                Reader::class -> resultSet.getCharacterStream(1)
                Clob::class -> resultSet.getClob(1)
                java.sql.Date::class -> resultSet.getDate(1)
                Double::class -> resultSet.getDouble(1)
                Float::class -> resultSet.getFloat(1)
                Int::class -> resultSet.getInt(1)
                Long::class -> resultSet.getLong(1)
                Short::class -> resultSet.getShort(1)
                SQLXML::class -> resultSet.getSQLXML(1)
                String::class -> resultSet.getString(1)
                Time::class -> resultSet.getTime(1)
                Timestamp::class -> resultSet.getTimestamp(1)
                URL::class -> resultSet.getURL(1)

            // Manual conversion
                BigInteger::class -> resultSet.getBigDecimal(1).toBigInteger()
                else -> null
            } as T
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

    @VisibleForTesting
    internal fun <T : Any> convertWithAllArgsConstructor(cls: KClass<T>, resultSet: ResultSet): T? {

        return try {
            val resultSize = resultSet.metaData.columnCount
            // cls.constructors requires kotlin-reflect library. Use old Java reflection.
            val constructors = cls.java.declaredConstructors

            val sameSizeConstructors = constructors.filter { it.parameterCount == resultSize }
            for (constructor in sameSizeConstructors) {
                // TODO check metadata to infer appropriate constructor
                try {
                    val types = constructor.parameterTypes
                    val args = arrayOfNulls<Any>(types.size)
                    for ((i, type) in types.withIndex()) {
                        args[i] = resultSet.getObject(i + 1, type)
                    }
                    if (!constructor.isAccessible) constructor.isAccessible = true
                    return constructor.newInstance(*args) as T
                } catch (e: Exception) {
                    // Just ignore an exception and try next constructor
                    when (e) {
                        is SQLException -> throw e
                        else -> logger.trace("", e)
                    }
                }
            }

            // Couldn't find the constructor to use.
            null
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

    @VisibleForTesting
    internal fun <T : Any> convertAsBean(cls: KClass<T>, resultSet: ResultSet): T? {

        return try {
            val targetInstance = cls.java.newInstance()
            val meta = resultSet.metaData
            val columnNames = (1..meta.columnCount).map { meta.getColumnName(it) }

            // Via methods
            val properties = Introspector.getBeanInfo(cls.java).propertyDescriptors
            for (columnName in columnNames) {
                val property = properties.first { it.name == toLowerCamel(columnName) }
                val writer = property.writeMethod
                if (!writer.isAccessible) writer.isAccessible = true
                writer.invoke(targetInstance, resultSet.getObject(columnName, property.propertyType))
                // TODO 全てのフィールドが適切に設定されたかチェック
            }
            return targetInstance

            // Via fields
        } catch (e: Exception) {
            when (e) {
                is SQLException -> throw e
                else -> {
                    logger.info("", e)
                    null
                }
            }
        }
    }

    private fun toLowerCamel(str: String): String {
        return when {
            str.contains("_") -> LOWER_UNDERSCORE.to(LOWER_CAMEL, str.toLowerCase())
            str.contains("-") -> LOWER_HYPHEN.to(LOWER_CAMEL, str.toLowerCase())
            str.all { it.isUpperCase() } -> str.toLowerCase()
            else -> UPPER_CAMEL.to(LOWER_CAMEL, str)
        }
    }

}
