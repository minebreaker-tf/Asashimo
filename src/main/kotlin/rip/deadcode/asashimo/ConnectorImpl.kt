package rip.deadcode.asashimo

import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.reflect.KClass

internal class ConnectorImpl(private val dataSource: DataSource) : Connector {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: (ResultSet) -> T): T {
        return use { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: (ResultSet) -> T): List<T> {
        return use { fetchAll(sql, cls, resultMapper) }
    }

    override fun exec(sql: String): Int {
        return use { exec(sql) }
    }

    override fun with(dsl: WithDsl): WithInterface {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun with(dsl: Map<String, Any>): WithInterface {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T> use(block: UseClause.() -> T): T {
        return getConnection().use { conn ->
            conn.autoCommit = true
            block(UseClauseImpl(conn))
        }
    }

    override fun <T> transactional(block: UseClause.() -> T): T {
        val conn: Connection = getConnection()
        try {
            if (conn.transactionIsolation == Connection.TRANSACTION_NONE) {
                throw AsashimoException("Transaction is not available.")
            }
            conn.autoCommit = false
            return block(UseClauseImpl(conn))
        } catch (e: Exception) {
            try {
                conn.rollback()
                throw AsashimoException("Exception in transaction.", e)
            } catch (ex: Exception) {
                val message = "Failed to rollback."
                logger.warn(message)
                throw AsashimoException(message, ex)
            }
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

    private fun getConnection() = dataSource.connection

    companion object {
        private val logger = LoggerFactory.getLogger(ConnectorImpl::class.java)
    }

}