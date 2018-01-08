package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import java.sql.Connection
import javax.sql.DataSource

internal class DefaultConnector(
        private val dataSource: DataSource,
        defaultExecutor: ListeningExecutorService) : AbstractConnector(defaultExecutor) {
    override fun getConnection(): Connection = dataSource.connection
}
