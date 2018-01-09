package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import java.sql.Connection
import javax.sql.DataSource

internal class DefaultConnector(
        private val dataSource: DataSource,
        config: AsashimoConfig,
        defaultExecutor: ListeningExecutorService) : AbstractConnector(config, defaultExecutor) {
    override fun getConnection(): Connection = dataSource.connection
}
