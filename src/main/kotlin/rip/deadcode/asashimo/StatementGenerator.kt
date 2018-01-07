package rip.deadcode.asashimo

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Preconditions.checkState
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.sql.*

object StatementGenerator {

    fun create(conn: Connection, sql: String, params: Map<String, Any>): PreparedStatement {
        if (params.isEmpty()) return conn.prepareStatement(sql)

        // TODO refactoring for better readability and performance

        val tokens = lex(sql)
        checkState(!tokens.contains("?"), "Use named parameter instead of positional parameter.")

        var resultTokens = listOf<String>()
        var paramsToSet = listOf<Any?>()
        for (token in tokens) {
            var found = false
            for ((key, value) in params) {
                if (token == ":" + key) {
                    found = true
                    if (value is Collection<*>) {
                        // Spread collection
                        for ((i, eachValue) in value.withIndex()) {
                            resultTokens += "?"
                            if (i + 1 < value.size) resultTokens += ","
                            paramsToSet += eachValue
                        }
                    } else {
                        resultTokens += "?"
                        paramsToSet += value
                    }
                    break
                }
            }
            if (!found) resultTokens += token
        }

        val sqlToExec = resultTokens.joinToString(separator = " ")
        val stmt = conn.prepareStatement(sqlToExec)
        for ((i, param) in paramsToSet.withIndex()) {
            when (param) {
                is java.sql.Array -> stmt.setArray(i + 1, param)
                is BigDecimal -> stmt.setBigDecimal(i + 1, param)
                is InputStream -> stmt.setBinaryStream(i + 1, param)
                is Blob -> stmt.setBlob(i + 1, param)
                is Boolean -> stmt.setBoolean(i + 1, param)
                is Byte -> stmt.setByte(i + 1, param)
                is ByteArray -> stmt.setBytes(i + 1, param)
                is Reader -> stmt.setCharacterStream(i + 1, param)
                is Clob -> stmt.setClob(i + 1, param)
                is java.sql.Date -> stmt.setDate(i + 1, param)
                is Double -> stmt.setDouble(i + 1, param)
                is Float -> stmt.setFloat(i + 1, param)
                is Int -> stmt.setInt(i + 1, param)
                is Long -> stmt.setLong(i + 1, param)
                is Short -> stmt.setShort(i + 1, param)
                is SQLXML -> stmt.setSQLXML(i + 1, param)
                is String -> stmt.setString(i + 1, param)
                is Time -> stmt.setTime(i + 1, param)
                is Timestamp -> stmt.setTimestamp(i + 1, param)
                is URL -> stmt.setURL(i + 1, param)

            // Manual conversion
                is BigInteger -> stmt.setBigDecimal(i + 1, param.toBigDecimal())

                else -> stmt.setObject(i + 1, param)
            }
        }

        return stmt
    }

    internal data class LexResult(
            val token: String?,
            val rest: String?
    )

    /**
     * Simple stupid lexer function to lex sql, mainly to provide named parameter.
     */
    @VisibleForTesting
    internal tailrec fun lex(sql: String, analyzed: List<String> = listOf()): List<String> {
        val trimmedSql = sql.trimStart()
        if (trimmedSql.isEmpty()) {
            return analyzed
        }

        val result = lexSeparator(trimmedSql) ?: lexStringLiteral(trimmedSql) ?: lexOther(trimmedSql)

        return if (result.rest == null) {
            if (result.token == null) analyzed else analyzed + result.token
        } else {
            lex(result.rest, if (result.token == null) analyzed else analyzed + result.token)
        }

    }

    private fun lexSeparator(string: String): LexResult? {
        return when {
            string.startsWith(",") -> LexResult(",", string.substring(1))
            string.startsWith(";") -> LexResult(";", string.substring(1))
            string.startsWith("(") -> LexResult("(", string.substring(1))
            string.startsWith(")") -> LexResult(")", string.substring(1))
            else -> null
        }
    }

    private fun lexStringLiteral(string: String): LexResult? {
        if (!string.startsWith("\"") && !string.startsWith("'")) return null

        val opening = string.first()

        tailrec fun firstNonEscapedQuote(defaultStart: Int = 1): Int {
            val i = string.indexOf(opening, startIndex = defaultStart)
            checkState(i >= 0, "Failed to find matching quote literal.")
            return if (string[i - 1] != '\\') {
                i
            } else {
                firstNonEscapedQuote(i)
            }
        }

        val endingIndex = firstNonEscapedQuote()
        return LexResult(string.substring(0, endingIndex + 1), string.substring(endingIndex + 1))
    }

    private fun lexOther(string: String): LexResult {
        val nextTokenIndex = string.indexOfFirst {
            it.isWhitespace() || it == ',' || it == ';' || it == '(' || it == ')'
        }
        return if (nextTokenIndex >= 0) {
            LexResult(string.substring(0, nextTokenIndex), string.substring(nextTokenIndex))
        } else {
            LexResult(string, null)
        }
    }

    // TODO lex comments

}
