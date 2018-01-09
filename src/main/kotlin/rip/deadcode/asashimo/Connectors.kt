package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Executors
import javax.sql.DataSource

object Connectors {

    private val jvmUniqueExecutor: ListeningExecutorService by lazy {
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(8))
    }

    @JvmOverloads
    fun newInstance(dataSource: DataSource, config: AsashimoConfig = AsashimoConfig()): Connector {
        return if (config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector({ dataSource }, config, jvmUniqueExecutor)
        } else {
            DefaultConnector(dataSource, config, jvmUniqueExecutor)
        }
    }

    @JvmOverloads
    fun newInstance(dataSourceFactory: () -> DataSource, config: AsashimoConfig = AsashimoConfig()): Connector {
        return if (config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector(dataSourceFactory, config, jvmUniqueExecutor)
        } else {
            DefaultConnector(dataSourceFactory(), config, jvmUniqueExecutor)
        }
    }

}
