package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import rip.deadcode.asashimo.manipulation.*
import rip.deadcode.asashimo.resultmapper.ConstructorResultMapper
import rip.deadcode.asashimo.resultmapper.GeneralResultMapper
import java.util.concurrent.Executors
import javax.sql.DataSource

/**
 * Factory class to generate [Connector] instance.
 */
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
            defaultResultMapper: GeneralResultMapper = ConstructorResultMapper
    ): Connector {
        return newInstance(AsashimoRegistry(
                dataSourceFactory,
                config,
                defaultResultMapper,
                jvmUniqueExecutor,
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

        val base = BasicRetriever
        val java8dateTime = config.java8dateConversionStrategy.getRetriever(config)

        val resolved = if (java8dateTime != null) {
            base.withFallback(java8dateTime)
        } else {
            base
        }
        return resolved.withFallback(AnyRetriever)
    }

    private fun resolveSetters(config: AsashimoConfig): Setter {

        val base = BasicSetter
        val java8dateTime = config.java8dateConversionStrategy.getSetter(config)

        val resolved = if (java8dateTime != null) {
            base.withFallback(java8dateTime)
        } else {
            base
        }
        return resolved.withFallback(AnySetter)
    }

}
