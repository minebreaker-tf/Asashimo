package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

internal class UseClauseImpl(
        private val connection: Connection,
        private val config: AsashimoConfig,
        private val connectionResetCallback: () -> Unit,
        private val params: Map<String, Any?> = mapOf()) : UseClause {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        try {
            return Runner.fetch(connection, config, sql, cls, resultMapper = resultMapper, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        try {
            return Runner.fetchAll(connection, config, sql, cls, resultMapper = resultMapper, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun exec(sql: String): Int {
        try {
            return Runner.exec(connection, config, sql, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun execLarge(sql: String): Long {
        try {
            return Runner.execLarge(connection, config, sql, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

}
