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

    private fun <T : Any> ResultSet.getUnknown(i: Int, type: KClass<out T>): T? {
        @Suppress("IMPLICIT_CAST_TO_ANY")
        return when (type) {
        // Directly provided by JDBC driver
            java.sql.Array::class -> getArray(i) as T
            BigDecimal::class -> getBigDecimal(i) as T
            InputStream::class -> getBinaryStream(i) as T
            Blob::class -> getBlob(i) as T
            Boolean::class -> getBoolean(i) as T
            Byte::class -> getByte(i) as T
            ByteArray::class -> getBytes(i) as T
            Reader::class -> getCharacterStream(i) as T
            Clob::class -> getClob(i) as T
            java.sql.Date::class -> getDate(i) as T
            Double::class -> getDouble(i) as T
            Float::class -> getFloat(i) as T
            Int::class -> getInt(i) as T
            Long::class -> getLong(i) as T
            Short::class -> getShort(i) as T
            SQLXML::class -> getSQLXML(i) as T
            String::class -> getString(i) as T
            Time::class -> getTime(i) as T
            Timestamp::class -> getTimestamp(i) as T
            URL::class -> getURL(i) as T

        // Manual conversion
            BigInteger::class -> getBigDecimal(i).toBigInteger() as T
            else -> {
                logger.trace("Could not found corresponding basic type.")
                null
            }
        }
    }

    /**
     * JDBC型が要求されていた場合、対応するメソッドを使用して値を取得する.
     */
    @VisibleForTesting
    internal fun <T : Any> convertToBasicType(cls: KClass<T>, resultSet: ResultSet): T? {
        return try {
            return resultSet.getUnknown(1, cls)
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
                        args[i] = resultSet.getUnknown<Any>(i + 1, type.kotlin) ?: resultSet.getObject(i + 1, type)
                    }
                    if (!constructor.isAccessible) constructor.isAccessible = true
                    return constructor.newInstance(*args) as T
                } catch (e: Exception) {
                    // Just ignore an exception and try next constructor
                    when (e) {
                        is SQLException -> throw e
                        else -> logger.trace("Failed to instantiate",  e)
                    }
                }
            }

            // Couldn't find the constructor to use.
            null
        } catch (e: Exception) {
            when (e) {
                is SQLException -> throw e
                else -> {
                    logger.trace("Failed to instantiate using all constructors.", e)
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
                    logger.info("Failed during invoking setters.", e)
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
