package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import rip.deadcode.asashimo.resultmapper.ConstructorResultMapper
import rip.deadcode.asashimo.resultmapper.GeneralResultMapper
import java.util.concurrent.Executors
import javax.sql.DataSource

object Connectors {

    private val jvmUniqueExecutor: ListeningExecutorService by lazy {
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(8))
    }

    @JvmOverloads
    fun newInstance(
            dataSource: DataSource,
            config: AsashimoConfig = AsashimoConfig(),
            defaultResultMapper: GeneralResultMapper = ConstructorResultMapper
    ): Connector {
        return newInstance({ dataSource }, config, defaultResultMapper)
    }

    @JvmOverloads
    fun newInstance(
            dataSourceFactory: () -> DataSource,
            config: AsashimoConfig = AsashimoConfig(),
            defaultResultMapper: GeneralResultMapper = ConstructorResultMapper
    ): Connector {
        return newInstance(AsashimoRegistry(
                dataSourceFactory,
                config,
                defaultResultMapper,
                jvmUniqueExecutor
        ))
    }

    fun newInstance(registry: AsashimoRegistry): Connector {
        return if (registry.config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector(registry)
        } else {
            DefaultConnector(registry)
        }
    }

}
