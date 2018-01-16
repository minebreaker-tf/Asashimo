package rip.deadcode.asashimo

import java.sql.Connection
import javax.sql.DataSource

internal class DefaultConnector(
        private val registry: AsashimoRegistry) : AbstractConnector(registry) {

    private val dataSource: DataSource = registry.dataSourceFactory()

    override fun getConnection(): Connection = dataSource.connection
}
