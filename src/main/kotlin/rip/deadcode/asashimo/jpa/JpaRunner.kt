package rip.deadcode.asashimo.jpa

import com.google.common.annotations.Beta
import rip.deadcode.asashimo.AsashimoRegistry
import rip.deadcode.asashimo.StatementGenerator
import rip.deadcode.asashimo.resultmapper.JpaResultMapper
import rip.deadcode.asashimo.utils.escape
import java.sql.Connection
import javax.persistence.NoResultException
import javax.persistence.NonUniqueResultException
import kotlin.reflect.KClass

object JpaRunner {

    @Beta
    fun persist(registry: AsashimoRegistry, conn: Connection, entity: Any) {

        val info = JpaIntrospector.introspect(entity)
        val allColumns = listOf(info.idName) + info.columnNames

        // TODO this is terrible and must be refactored
        val sql = "insert into ${escape(info.tableName)} (${escape(allColumns.joinToString())}) " +
                "values(${escape("?, ".repeat(info.columnNames.size))}?)"

        val stmt = conn.prepareStatement(sql)
        StatementGenerator.setParams(registry, stmt, listOf(info.id) + info.columns)

        stmt.execute()
    }

    @Beta
    fun <T : Any> find(registry: AsashimoRegistry, conn: Connection, id: Any, cls: KClass<T>): T {

        val clsInfo = JpaIntrospector.introspect(cls)

        val sql = "select ${escape(clsInfo.idName)}, ${escape(clsInfo.columnNames.joinToString(", "))} " +
                "from ${escape(clsInfo.tableName)} where ${escape(clsInfo.idName)} = ?"
        val stmt = conn.prepareStatement(sql)
        stmt.setObject(1, id)
        stmt.execute()
        val rs = stmt.resultSet

        if (!rs.next()) throw NoResultException()

        val result = JpaResultMapper.map(registry, cls, rs)
        if (rs.next()) throw NonUniqueResultException()
        return result
    }

}
