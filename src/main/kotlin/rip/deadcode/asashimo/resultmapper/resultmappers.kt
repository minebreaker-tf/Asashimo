@file:Suppress("UNCHECKED_CAST")

package rip.deadcode.asashimo.resultmapper

import org.slf4j.LoggerFactory
import rip.deadcode.asashimo.AsashimoRegistry
import rip.deadcode.asashimo.manipulation.BasicRetriever
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

private val logger = LoggerFactory.getLogger(GeneralResultMapper::class.java.`package`.name)

// TODO refactoring

internal fun <T : Any> ResultSet.getUnknown(i: Int, type: KClass<out T>, registry: AsashimoRegistry): T? {
    return registry.retriever.retrieveByClass(this, type, i)
}

/**
 * JDBC型が要求されていた場合、対応するメソッドを使用して値を取得する.
 */
internal fun <T : Any> convertToBasicType(cls: KClass<T>, resultSet: ResultSet): T? {
    return try {
        val result = if (BasicRetriever.retrievable(cls)) {
            BasicRetriever.retrieveByClass(resultSet, cls, 1)
        } else {
            null
        }
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
