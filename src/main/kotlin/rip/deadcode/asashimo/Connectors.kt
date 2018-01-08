package rip.deadcode.asashimo

import javax.sql.DataSource

object Connectors {

    @JvmOverloads
    fun newInstance(dataSource: DataSource, config: AsashimoConfig = AsashimoConfig()): Connector {
        return if (config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector({ dataSource })
        } else {
            DefaultConnector(dataSource)
        }
    }

    @JvmOverloads
    fun newInstance(dataSourceFactory: () -> DataSource, config: AsashimoConfig = AsashimoConfig()): Connector {
        return if (config.resetDataSourceWhenExceptionOccurred) {
            ResettingConnector(dataSourceFactory)
        } else {
            DefaultConnector(dataSourceFactory())
        }
    }

}
