package rip.deadcode.asashimo

import com.google.errorprone.annotations.CanIgnoreReturnValue
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.Savepoint

internal class OfTransactionalImpl(
        private val connection: Connection,
        registry: AsashimoRegistry,
        connectionResetCallback: () -> Unit,
        params: Map<String, Any?> = mapOf()) :

        OfUseImpl(connection, registry, connectionResetCallback, params),
        OfTransactional {

    @CanIgnoreReturnValue
    override fun <T> savepoint(name: String?, block: OfUse.() -> T): T? {

        var savepoint: Savepoint? = null
        var savepointStr = "[null]"  // String representation of the savepoint to log

        try {
            if (name != null) {
                savepoint = connection.setSavepoint(name)
                savepointStr = "[Name: ${savepoint.savepointName} (${savepoint})]"
            } else {
                savepoint = connection.setSavepoint()
                savepointStr = "[ID: ${savepoint.savepointId} (${savepoint})]"
            }

            val result = this.block()
            connection.releaseSavepoint(savepoint)
            return result

        } catch (e: Exception) {

            try {
                connection.rollback(savepoint)
            } catch (e: Exception) {
                throw AsashimoException("Failed to rollback.")
            }

            logger.info("Exception in the savepoint. Rolled back to the {}.", savepointStr, e)

            return null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OfTransactionalImpl::class.java)
    }

}