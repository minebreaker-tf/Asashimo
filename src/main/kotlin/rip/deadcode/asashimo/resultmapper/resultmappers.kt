@file:Suppress("UNCHECKED_CAST")

package rip.deadcode.asashimo.resultmapper

import org.slf4j.LoggerFactory
import rip.deadcode.asashimo.AsashimoRegistry
import rip.deadcode.asashimo.manipulation.BasicRetriever
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

private val logger = LoggerFactory.getLogger(GeneralResultMapper::class.java.`package`.name)

internal fun <T : Any> convertUsingRegistry(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T? {
    return try {
        // Anyへのフォールバックを防ぐため、registryのretrieverを直接は使用しない
        val retriever = BasicRetriever
                .withFallback(registry.config.dateConversionStrategy.getRetriever(registry.config))
        val result = if (retriever.retrievable(cls)) {
            retriever.retrieveByClass(resultSet, cls, 1)
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
