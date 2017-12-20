package rip.deadcode.asashimo

import javax.sql.DataSource

object Connectors {
    fun newInstance(dataSource: DataSource): Connector = ConnectorImpl(dataSource)
}

internal class ConnectorImpl(private val dataSource: DataSource) : Connector {

}
