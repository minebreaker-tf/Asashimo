package rip.deadcode.asashimo.manipulation

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.sql.*
import kotlin.reflect.KClass

object BasicRetriever : Retriever {

    private val retrievableClass = setOf(
            java.sql.Array::class,
            BigDecimal::class,
            InputStream::class,
            Blob::class,
            Boolean::class,
            Byte::class,
            ByteArray::class,
            Reader::class,
            Clob::class,
            java.sql.Date::class,
            Double::class,
            Float::class,
            Int::class,
            Long::class,
            Short::class,
            SQLXML::class,
            String::class,
            Time::class,
            Timestamp::class,
            URL::class,

            BigInteger::class
    )

    override fun <T : Any> retrievable(cls: KClass<T>): Boolean {
        return retrievableClass.contains(cls)
    }

    override fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T? {

        @Suppress("UNCHECKED_CAST")
        return when (cls) {

        // Directly provided by JDBC driver
            java.sql.Array::class -> rs.getArray(index) as T?
            BigDecimal::class -> rs.getBigDecimal(index) as T?
            InputStream::class -> rs.getBinaryStream(index) as T?
            Blob::class -> rs.getBlob(index) as T?
            Boolean::class -> rs.getBoolean(index) as T?
            Byte::class -> rs.getByte(index) as T?
            ByteArray::class -> rs.getBytes(index) as T?
            Reader::class -> rs.getCharacterStream(index) as T?
            Clob::class -> rs.getClob(index) as T?
            java.sql.Date::class -> rs.getDate(index) as T?
            Double::class -> rs.getDouble(index) as T?
            Float::class -> rs.getFloat(index) as T?
            Int::class -> rs.getInt(index) as T?
            Long::class -> rs.getLong(index) as T?
            Short::class -> rs.getShort(index) as T?
            SQLXML::class -> rs.getSQLXML(index) as T?
            String::class -> rs.getString(index) as T?
            Time::class -> rs.getTime(index) as T?
            Timestamp::class -> rs.getTimestamp(index) as T?
            URL::class -> rs.getURL(index) as T?

        // Manual conversion
            BigInteger::class -> rs.getBigDecimal(index).toBigInteger() as T?

            else -> throw RuntimeException()
        }
    }
}

object AnyRetriever : Retriever {

    override fun <T : Any> retrievable(cls: KClass<T>): Boolean = true

    override fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T? {
        return rs.getObject(index, cls.java)
    }
}

object BasicSetter : Setter {

    override fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean {

        when (param) {

        // Directly provided by JDBC driver
            is java.sql.Array -> stmt.setArray(index, param)
            is BigDecimal -> stmt.setBigDecimal(index, param)
            is InputStream -> stmt.setBinaryStream(index, param)
            is Blob -> stmt.setBlob(index, param)
            is Boolean -> stmt.setBoolean(index, param)
            is Byte -> stmt.setByte(index, param)
            is ByteArray -> stmt.setBytes(index, param)
            is Reader -> stmt.setCharacterStream(index, param)
            is Clob -> stmt.setClob(index, param)
            is java.sql.Date -> stmt.setDate(index, param)
            is Double -> stmt.setDouble(index, param)
            is Float -> stmt.setFloat(index, param)
            is Int -> stmt.setInt(index, param)
            is Long -> stmt.setLong(index, param)
            is Short -> stmt.setShort(index, param)
            is SQLXML -> stmt.setSQLXML(index, param)
            is String -> stmt.setString(index, param)
            is Time -> stmt.setTime(index, param)
            is Timestamp -> stmt.setTimestamp(index, param)
            is URL -> stmt.setURL(index, param)

        // Manual conversion
            is BigInteger -> stmt.setBigDecimal(index, param.toBigDecimal())

            else -> return false
        }
        return true
    }

}

object AnySetter : Setter {

    override fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean {
        stmt.setObject(index, param)
        return true
    }
}
