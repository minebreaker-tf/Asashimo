package rip.deadcode.asashimo

import java.sql.Connection
import javax.sql.DataSource

internal class DefaultConnector(private val dataSource: DataSource) : AbstractConnector() {
    override fun getConnection(): Connection = dataSource.connection
}
