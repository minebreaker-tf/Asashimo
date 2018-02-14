package rip.deadcode.asashimo.resultmapper

import com.google.common.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import rip.deadcode.asashimo.AsashimoException
import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

object ConstructorResultMapper : GeneralResultMapper {

    private val logger = LoggerFactory.getLogger(ConstructorResultMapper::class.java)

    override fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T {
        return convertToBasicType(cls, resultSet)
                ?: convertWithAllArgsConstructor(cls, resultSet, registry)
                ?: throw AsashimoException("Failed to map ResultSet to class '${cls.java.canonicalName}'")
    }

    @VisibleForTesting
    internal fun <T : Any> convertWithAllArgsConstructor(
            cls: KClass<T>, resultSet: ResultSet, registry: AsashimoRegistry): T? {

        return try {
            val resultSize = resultSet.metaData.columnCount
            // cls.constructors requires kotlin-reflect library. Use old Java reflection.
            val constructors = cls.java.declaredConstructors

            val sameSizeConstructors = constructors.filter { it.parameterCount == resultSize }
            for (constructor in sameSizeConstructors) {
                // TODO check metadata to infer appropriate constructor
                // needs more wise way
                try {
                    val types = constructor.parameterTypes
                    val args = arrayOfNulls<Any>(types.size)
                    for ((i, type) in types.withIndex()) {
                        args[i] = registry.retriever.retrieveByClass(resultSet, type.kotlin, i + 1)
                                ?: resultSet.getObject(i + 1, type)
                    }
                    constructor.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    return constructor.newInstance(*args) as T
                } catch (e: Exception) {
                    // Just ignore an exception and try next constructor
                    when (e) {
                        is SQLException -> throw e
                        else -> logger.trace("Failed to instantiate", e)
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

}