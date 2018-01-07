package rip.deadcode.asashimo

import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.reflect.KClass

internal class ConnectorImpl(private val dataSource: DataSource) : Connector {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { fetchAll(sql, cls, resultMapper) }
    }

    override fun exec(sql: String): Int {
        return use { exec(sql) }
    }

//    override fun with(dsl: WithDsl): WithClause {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override fun with(params: Map<String, Any>): WithClause {
        return WithClauseImpl(getConnection(), params)
    }

    override fun <T> use(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), mapOf()).use(block)
    }

    override fun <T> transactional(block: UseClause.() -> T): T {
        return WithClauseImpl(getConnection(), mapOf()).transactional(block)
    }

    private fun getConnection() = dataSource.connection

}
