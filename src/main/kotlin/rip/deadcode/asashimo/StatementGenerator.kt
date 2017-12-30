package rip.deadcode.asashimo

import java.sql.Connection
import java.sql.PreparedStatement

object StatementGenerator {

    fun create(conn: Connection, sql: String, params: Map<String, SqlParameter>): PreparedStatement {
        val stmt = conn.prepareStatement(sql)
        // TODO apply parameter map
        return stmt
    }

}
