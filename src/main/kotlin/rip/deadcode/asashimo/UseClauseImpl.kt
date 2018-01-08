package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

internal class UseClauseImpl(
        private val connection: Connection,
        private val connectionResetCallback: () -> Unit,
        private val params: Map<String, Any> = mapOf()) : UseClause {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        try {
            return Runner.fetch(connection, sql, cls, resultMapper = resultMapper, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        try {
            return Runner.fetchAll(connection, sql, cls, resultMapper = resultMapper, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<T> {
        return Supplier { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAllLazy(
            sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<List<T>> {
        return Supplier { fetchAll(sql, cls, resultMapper) }
    }

    override fun exec(sql: String): Int {
        try {
            return Runner.exec(connection, sql, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

    override fun execLarge(sql: String): Long {
        try {
            return Runner.execLarge(connection, sql, params = params)
        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        }
    }

}
