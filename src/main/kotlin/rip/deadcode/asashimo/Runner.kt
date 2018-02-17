package rip.deadcode.asashimo

import com.google.common.collect.ImmutableList
import rip.deadcode.asashimo.utils.Experimental
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

internal object Runner {

    fun <T : Any> fetch(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any?> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): T {

        val stmt = StatementGenerator.create(conn, registry, sql, params)
        val execResult = stmt.execute()
        check(execResult)

        val rs = stmt.resultSet
        val hadValue = rs.next()
        if (!hadValue) throw AsashimoNoResultException() // Assure successful

        val result = resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)

        if (rs.next()) throw AsashimoNonUniqueResultException()
        return result
    }

    fun <T : Any> fetchMaybe(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any?> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): T? {

        val stmt = StatementGenerator.create(conn, registry, sql, params)
        stmt.execute()
        val rs = stmt.resultSet

        val result = if (rs.next()) {
            resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)
        } else {
            null
        }

        if (rs.next()) throw AsashimoNonUniqueResultException()
        return result
    }

    fun <T : Any> fetchAll(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any?> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): List<T> {

        val stmt = StatementGenerator.create(conn, registry, sql, params)
        val result = stmt.execute()
        check(result)

        val rs = stmt.resultSet
        val mutableList = mutableListOf<T>()
        while (rs.next()) {
            val row = resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)
            mutableList.add(row)
        }

        // Kotlinはリストをイミュータブルにする手段を提供しない...
        return ImmutableList.copyOf(mutableList)
    }

    fun exec(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            params: Map<String, Any?> = mapOf()): Int {

        val stmt = StatementGenerator.create(conn, registry, sql, params)

        return stmt.executeUpdate()
    }

    fun execLarge(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            params: Map<String, Any?> = mapOf()): Long {

        val stmt = StatementGenerator.create(conn, registry, sql, params)
        return stmt.executeLargeUpdate()
    }

    @Experimental
    fun execBatch(
            conn: Connection,
            @Suppress("UNUSED_PARAMETER") registry: AsashimoRegistry,
            sqls: List<String>): IntArray {

        // TODO Fluent exception handling

        conn.autoCommit = false

        val stmt = conn.createStatement()
        for (sql in sqls) {
            stmt.addBatch(sql)
        }

        val result = stmt.executeBatch()
        conn.commit()
        return result
    }

    @Experimental
    fun execPreparedBatch(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            params: Map<String, List<Any?>>): IntArray {

        // TODO Fluent exception handling

        conn.autoCommit = false

        val stmt = StatementGenerator.createBatch(conn, registry, sql, params.mapValues { it.value.iterator() })

        val result = stmt.executeBatch()
        conn.commit()
        return result
    }

}
