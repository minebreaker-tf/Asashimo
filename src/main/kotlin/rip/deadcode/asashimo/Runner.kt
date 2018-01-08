package rip.deadcode.asashimo

import com.google.common.base.Preconditions.checkState
import com.google.common.collect.ImmutableList
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

// TODO ここで例外処理
object Runner {

    fun <T : Any> fetch(
            conn: Connection,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): T {

        val stmt = StatementGenerator.create(conn, sql, params)
        val result = stmt.execute()
        check(result)

        val rs = stmt.resultSet
        checkState(rs.next(), "No Result")  // Assure successful
        return resultMapper?.invoke(rs) ?: GeneralResultMapper.map(cls, rs)
    }

    fun <T : Any> fetchAll(
            conn: Connection,
            sql: String,
            cls: KClass<T>,
            params: Map<String, Any> = mapOf(),
            resultMapper: ((ResultSet) -> T)? = null): List<T> {

        val stmt = StatementGenerator.create(conn, sql, params)
        val result = stmt.execute()
        check(result)

        val rs = stmt.resultSet
        val mutableList = mutableListOf<T>()
        while (rs.next()) {
            val row = resultMapper?.invoke(rs) ?: GeneralResultMapper.map(cls, rs)
            mutableList.add(row)
        }

        // Kotlinはリストをイミュータブルにする手段を提供しない...
        return ImmutableList.copyOf(mutableList)
    }


    fun exec(
            conn: Connection,
            sql: String,
            params: Map<String, Any> = mapOf()): Int {

        val stmt = StatementGenerator.create(conn, sql, params)
        return stmt.executeUpdate()
    }

    fun execLarge(
            conn: Connection,
            sql: String,
            params: Map<String, Any> = mapOf()): Long {

        val stmt = StatementGenerator.create(conn, sql, params)
        return stmt.executeLargeUpdate()
    }

}