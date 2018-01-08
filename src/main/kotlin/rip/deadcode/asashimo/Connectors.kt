package rip.deadcode.asashimo

import javax.sql.DataSource

object Connectors {
    fun newInstance(dataSource: DataSource): Connector = ConnectorImpl({ dataSource })
    fun newInstance(dataSourceFactory: () -> DataSource): Connector = ConnectorImpl(dataSourceFactory)
}
