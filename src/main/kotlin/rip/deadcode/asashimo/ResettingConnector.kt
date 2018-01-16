package rip.deadcode.asashimo

import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean

internal class ResettingConnector(
        private val registry: AsashimoRegistry) : AbstractConnector(registry) {

    private var dataSource = registry.dataSourceFactory()
    private val resetDataSource = AtomicBoolean(false)

    override fun resetDataSourceCallback() {
        resetDataSource.set(true)
    }

    override fun getConnection(): Connection {
        synchronized(dataSource) {
            if (resetDataSource.get()) {
                dataSource = registry.dataSourceFactory()
                resetDataSource.set(false)
            }
            return dataSource.connection
        }
    }

}
