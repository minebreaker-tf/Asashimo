package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

internal class UseClauseImpl(private val connection: Connection) : UseClause {

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return Runner.fetch(connection, sql, cls, resultMapper = resultMapper)
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return Runner.fetchAll(connection, sql, cls, resultMapper = resultMapper)
    }

    override fun exec(sql: String): Int {
        return Runner.exec(connection, sql)
    }

}
