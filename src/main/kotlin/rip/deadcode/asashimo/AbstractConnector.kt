package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

internal abstract class AbstractConnector(
        private val defaultExecutor: ListeningExecutorService) : Connector {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { fetchAll(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<T> {
        return Supplier { use { fetch(sql, cls, resultMapper) } }
    }

    override fun <T : Any> fetchAllLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<List<T>> {
        return Supplier { use { fetchAll(sql, cls, resultMapper) } }
    }

    override fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<T> {
        return (executorService ?: defaultExecutor).submit<T> {
            use { fetch(sql, cls, resultMapper) }
        }
    }

    override fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<List<T>> {
        return (executorService ?: defaultExecutor).submit<List<T>> {
            use { fetchAll(sql, cls, resultMapper) }
        }
    }

    override fun exec(sql: String): Int {
        return use { exec(sql) }
    }

    override fun execLarge(sql: String): Long {
        return use { execLarge(sql) }
    }

    override fun execAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Int> {
        return (executorService ?: defaultExecutor).submit<Int> {
            use { exec(sql) }
        }
    }

    override fun execLargeAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Long> {
        return (executorService ?: defaultExecutor).submit<Long> {
            use { execLarge(sql) }
        }
    }

    override fun with(block: (MutableMap<String, Any>) -> Unit): WithClause {
        val params = mutableMapOf<String, Any>()
        block(params)
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, params, defaultExecutor)
    }

    override fun with(params: Map<String, Any>): WithClause {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, params, defaultExecutor)
    }

    override fun <T> use(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, mapOf(), defaultExecutor).use(block)
    }

    override fun <T> transactional(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), ::resetDataSourceCallback, mapOf(), defaultExecutor).transactional(block)
    }

    protected abstract fun getConnection(): Connection

    protected open fun resetDataSourceCallback() {
        // Do nothing
    }

}
