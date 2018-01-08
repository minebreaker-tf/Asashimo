package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

internal class ResettingConnector(
        private val dataSourceFactory: () -> DataSource,
        defaultExecutor: ListeningExecutorService) : AbstractConnector(defaultExecutor) {

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
