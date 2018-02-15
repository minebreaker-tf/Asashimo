package rip.deadcode.asashimo

import com.google.common.base.Preconditions.checkState
import com.google.common.collect.ImmutableList
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

        StatementGenerator.create(conn, registry, sql, params).use { stmt ->

            val execResult = stmt.execute()
            check(execResult)

            stmt.resultSet.use { rs ->
                val hadValue = rs.next()
                if (!hadValue) throw AsashimoNoResultException() // Assure successful

                val result = resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)
                if (rs.next()) throw AsashimoNonUniqueResultException()

                return result
            }
        }
    }

    fun <T : Any> fetchMaybe(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any?> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): T? {

        StatementGenerator.create(conn, registry, sql, params).use { stmt ->

            stmt.resultSet.use { rs ->

                return if (rs == null) {
                    null
                } else {
                    checkState(rs.next(), "No Result")  // Assure successful
                    resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)
                }
            }
        }
    }

    fun <T : Any> fetchAll(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any?> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): List<T> {

        StatementGenerator.create(conn, registry, sql, params).use { stmt ->

            val result = stmt.execute()
            check(result)

            stmt.resultSet.use { rs ->

                val mutableList = mutableListOf<T>()
                while (rs.next()) {
                    val row = resultMapper?.invoke(rs) ?: registry.defaultResultMapper.map(registry, cls, rs)
                    mutableList.add(row)
                }

                // Kotlinはリストをイミュータブルにする手段を提供しない...
                return ImmutableList.copyOf(mutableList)
            }
        }

    }


    fun exec(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            params: Map<String, Any?> = mapOf()): Int {

        StatementGenerator.create(conn, registry, sql, params).use {
            return it.executeUpdate()
        }

    }

    fun execLarge(
            conn: Connection,
            registry: AsashimoRegistry,
            sql: String,
            params: Map<String, Any?> = mapOf()): Long {

        StatementGenerator.create(conn, registry, sql, params).use {
            return it.executeLargeUpdate()
        }
    }

}
