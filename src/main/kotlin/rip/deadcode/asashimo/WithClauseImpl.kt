package rip.deadcode.asashimo

import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

class WithClauseImpl(
        private val conn: Connection,
        private val connectionResetCallback: () -> Unit,
        private val params: Map<String, Any>) : WithClause {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { Runner.fetch(conn, sql, cls, resultMapper = resultMapper, params = params) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { Runner.fetchAll(conn, sql, cls, resultMapper = resultMapper, params = params) }
    }

    override fun exec(sql: String): Int {
        return use { Runner.exec(conn, sql, params) }
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

    companion object {
        private val logger = LoggerFactory.getLogger(WithClauseImpl::class.java)
    }

}
