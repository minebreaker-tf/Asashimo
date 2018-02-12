package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

internal class OfUseImpl(
        private val connection: Connection,
        private val registry: AsashimoRegistry,
        private val connectionResetCallback: () -> Unit,
        private val params: Map<String, Any?> = mapOf()) : OfUse {

    private val internalParams: MutableMap<String, Any?> = mutableMapOf()

    override fun bind(binding: Pair<String, Any?>) {
        internalParams += binding
    }

    override fun bind(block: (MutableMap<String, Any?>) -> Unit) {
        block(internalParams)
    }

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        try {
            return Runner.fetch(connection, registry, sql, cls,
                                resultMapper = resultMapper,
                                params = params + internalParams)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T? {
        try {
            return Runner.fetchMaybe(connection, registry, sql, cls,
                                     resultMapper = resultMapper,
                                     params = params + internalParams)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        try {
            return Runner.fetchAll(connection, registry, sql, cls,
                                   resultMapper = resultMapper,
                                   params = params + internalParams)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun exec(sql: String): Int {
        try {
            return Runner.exec(connection, registry, sql, params = params + internalParams)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun execLarge(sql: String): Long {
        try {
            return Runner.execLarge(connection, registry, sql, params = params + internalParams)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

}
