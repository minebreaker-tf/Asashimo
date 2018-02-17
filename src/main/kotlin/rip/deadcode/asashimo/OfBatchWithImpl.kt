package rip.deadcode.asashimo

import java.sql.Connection

class OfBatchWithImpl(
        private val connection: Connection,
        private val registry: AsashimoRegistry,
        private val sql: String,
        private val connectionResetCallback: () -> Unit) : OfBatchWith {

    private val params: MutableMap<String, List<Any?>> = mutableMapOf()

    override fun with(params: Map<String, List<Any?>>): OfBatchWith {
        this.params += params
        return this
    }

    override fun exec(): IntArray {
        try {

            return Runner.execPreparedBatch(connection, registry, sql, params)

        } catch (e: Exception) {
            connectionResetCallback()
            throw e
        } finally {
            connection.close()
        }
    }
}
