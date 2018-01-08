package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

internal class WithClauseImpl(
        private val conn: Connection,
        private val connectionResetCallback: () -> Unit,
        private val params: Map<String, Any>,
        private val defaultExecutor: ListeningExecutorService) : WithClause {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { Runner.fetch(conn, sql, cls, resultMapper = resultMapper, params = params) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { Runner.fetchAll(conn, sql, cls, resultMapper = resultMapper, params = params) }
    }

    override fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<T> {
        return Supplier { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAllLazy(
            sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<List<T>> {
        return Supplier { fetchAll(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<T> {
        return (executorService ?: defaultExecutor).submit<T> { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<List<T>> {
        return (executorService ?: defaultExecutor).submit<List<T>> { fetchAll(sql, cls, resultMapper) }
    }

    override fun exec(sql: String): Int {
        return use { Runner.exec(conn, sql, params) }
    }

    override fun execLarge(sql: String): Long {
        return use { Runner.execLarge(conn, sql, params) }
    }

    override fun execAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Int> {
        return (executorService ?: defaultExecutor).submit<Int> { exec(sql) }
    }

    override fun execLargeAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Long> {
        return (executorService ?: defaultExecutor).submit<Long> { execLarge(sql) }
    }

    override fun <T> use(block: UseClause.() -> T): T {
        try {
            conn.autoCommit = true
            return UseClauseImpl(conn, connectionResetCallback, params).block()
        } catch (e: Exception) {
            connectionResetCallback()
            throw AsashimoException("Exception in use method.", e)
        } finally {
            try {
                conn.close()
            } catch (e: Exception) {
                val message = "Failed to close connection."
                logger.warn(message)
                throw AsashimoException(message, e)
            }
        }
    }

    override fun <T> useLazy(block: UseClause.() -> T): Supplier<T> {
        return Supplier { use(block) }
    }

    override fun <T> useAsync(
            executorService: ListeningExecutorService?, block: UseClause.() -> T): ListenableFuture<T> {
        return (executorService ?: defaultExecutor).submit<T> {
            use(block)
        }
    }

    override fun <T> transactional(block: UseClause.() -> T): T {
        try {
            if (conn.transactionIsolation == Connection.TRANSACTION_NONE) {
                throw AsashimoException("Transaction is not available.")
            }
            conn.autoCommit = false
            val result = UseClauseImpl(conn, connectionResetCallback, params).block()
            conn.commit()
            return result
        } catch (e: Exception) {
            connectionResetCallback()
            try {
                conn.rollback()
            } catch (ex: Exception) {
                val message = "Failed to rollback."
                logger.warn(message)
                throw AsashimoException(message, ex)
            }
            throw AsashimoException("Exception in transaction.", e)
        } finally {
            try {
                conn.close()
            } catch (e: Exception) {
                val message = "Failed to close connection."
                logger.warn(message)
                throw AsashimoException(message, e)
            }
        }
    }

    override fun <T> transactionalLazy(block: UseClause.() -> T): Supplier<T> {
        return Supplier { transactional(block) }
    }

    override fun <T> transactionalAsync(
            executorService: ListeningExecutorService?, block: UseClause.() -> T): ListenableFuture<T> {
        return (executorService ?: defaultExecutor).submit<T> {
            transactional(block)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WithClauseImpl::class.java)
    }

}
