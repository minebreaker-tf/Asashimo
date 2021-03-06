package rip.deadcode.asashimo

import com.google.common.util.concurrent.MoreExecutors
import rip.deadcode.asashimo.manipulation.*
import rip.deadcode.asashimo.resultmapper.ConstructorResultMapper
import rip.deadcode.asashimo.resultmapper.GeneralResultMapper
import java.util.concurrent.ExecutorService
import javax.sql.DataSource

/**
 * Factory class to generate [Connector] instance.
 */
object Connectors {

    private val defaultExecutor: ExecutorService by lazy {
        MoreExecutors.newDirectExecutorService()
    }

    @JvmOverloads
    fun newInstance(
            dataSource: DataSource,
            config: AsashimoConfig = AsashimoConfig(),
            defaultResultMapper: GeneralResultMapper = ConstructorResultMapper,
            executor: ExecutorService = defaultExecutor
    ): Connector {
        return newInstance({ dataSource }, config, defaultResultMapper, executor)
    }

    /**
     * Creates [Connector].
     *
     * @param dataSourceFactory Factory to generate [DataSource]
     * @param config Configuration object
     * @param defaultResultMapper Default result mapper, used when result set is not specified in each method
     */
    @JvmOverloads
    fun newInstance(
            dataSourceFactory: () -> DataSource,
            config: AsashimoConfig = AsashimoConfig(),
            defaultResultMapper: GeneralResultMapper = ConstructorResultMapper,
            executor: ExecutorService = defaultExecutor
    ): Connector {
        return newInstance(AsashimoRegistry(
                dataSourceFactory,
                config,
                defaultResultMapper,
                MoreExecutors.listeningDecorator(executor),
                resolveRetrievers(config),
                resolveSetters(config)
        ))
    }

    /**
     * Creates [Connector] using given registry.
     *
     * @param registry [AsashimoRegistry] used to create [Connector]
     */
    fun newInstance(registry: AsashimoRegistry): Connector {
        return if (registry.config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector(registry)
        } else {
            DefaultConnector(registry)
        }
    }

    private fun resolveRetrievers(config: AsashimoConfig): Retriever {

        return BasicRetriever
                .withFallback(config.dateConversionStrategy.getRetriever(config))
                .withFallback(AnyRetriever)
    }

    private fun resolveSetters(config: AsashimoConfig): Setter {

        return BasicSetter
                .withFallback(config.dateConversionStrategy.getSetter(config))
                .withFallback(AnySetter)
    }

}
