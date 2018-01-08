package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

internal abstract class AbstractConnector : Connector {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { fetchAll(sql, cls, resultMapper) }
    }

    override fun exec(sql: String): Int {
        return use { exec(sql) }
    }

    override fun with(block: (MutableMap<String, Any>) -> Unit): WithClause {
        val params = mutableMapOf<String, Any>()
        block(params)
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, params)
    }

    override fun with(params: Map<String, Any>): WithClause {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, params)
    }

    override fun <T> use(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, mapOf()).use(block)
    }

    override fun <T> transactional(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, mapOf()).transactional(block)
    }

    protected abstract fun getConnection(): Connection

    protected open fun resetDataSourceCallback() {
        // Do nothing
    }

}
