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
            ResettingConnector({ dataSource }, jvmUniqueExecutor)
        } else {
            DefaultConnector(dataSource, jvmUniqueExecutor)
        }
    }

    @JvmOverloads
    fun newInstance(dataSourceFactory: () -> DataSource, config: AsashimoConfig = AsashimoConfig()): Connector {
        return if (config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector(dataSourceFactory, jvmUniqueExecutor)
        } else {
            DefaultConnector(dataSourceFactory(), jvmUniqueExecutor)
        }
    }

}
