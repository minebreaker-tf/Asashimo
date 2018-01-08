package rip.deadcode.asashimo

import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

internal class ResettingConnector(private val dataSourceFactory: () -> DataSource) : AbstractConnector() {

    private var dataSource = dataSourceFactory()
    private val resetDataSource = AtomicBoolean(false)

    override fun resetDataSourceCallback() {
        resetDataSource.set(true)
    }

    override fun getConnection(): Connection {
        synchronized(dataSource) {
            if (resetDataSource.get()) {
                dataSource = dataSourceFactory()
                resetDataSource.set(false)
            }
            return dataSource.connection
        }
    }

}
