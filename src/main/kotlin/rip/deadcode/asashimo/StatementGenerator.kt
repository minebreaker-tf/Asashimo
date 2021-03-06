package rip.deadcode.asashimo

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Preconditions.checkState
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement

internal object StatementGenerator {

    private val logger = LoggerFactory.getLogger(StatementGenerator::class.java)

    fun create(conn: Connection, registry: AsashimoRegistry, sql: String, params: Map<String, Any?>): PreparedStatement {

        if (params.isEmpty()) {
            logger.debug("SQL: {}", sql)
            logger.trace("Params: -")
            return conn.prepareStatement(sql)
        }

        val tokens = lex(sql)
        checkState(!tokens.contains("?"), "Use named parameter instead of positional parameter.")

        val (sqlToExec, paramsToSet) = convertTokensToParamSet(tokens, params)
        logger.debug("SQL: {}", sqlToExec)
        logger.trace("Params: {}", paramsToSet)

        val stmt = conn.prepareStatement(sqlToExec)
        setParams(registry, stmt, paramsToSet)
        return stmt
    }

    fun createBatch(conn: Connection, registry: AsashimoRegistry, sql: String, params: Map<String, Iterator<Any?>>): PreparedStatement {

        val tokens = lex(sql)
        checkState(!tokens.contains("?"), "Use named parameter instead of positional parameter.")

        val (sqlToExec, paramNamesToSet) = retrieveParamNamesToSet(tokens, params)

        logger.debug("SQL: {}", sqlToExec)

        val stmt = conn.prepareStatement(sqlToExec)

        val paramIter = object : Iterator<List<Any?>> {
            override fun next(): List<Any?> = paramNamesToSet.map { params[it]!!.next() }
            override fun hasNext(): Boolean = params[paramNamesToSet[0]]!!.hasNext()
        }

        for (param in paramIter) {
            setParams(registry, stmt, param)
            stmt.addBatch()
        }

        return stmt
    }

    private data class ConversionResult(val sql: String, val params: List<Any?>)

    private fun convertTokensToParamSet(tokens: List<String>, params: Map<String, Any?>): ConversionResult {

        // TODO refactoring
        val resultTokens = mutableListOf<String>()
        val paramsToSet = mutableListOf<Any?>()
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

        val sqlToExec = joinTokens(resultTokens)
        return ConversionResult(sqlToExec, paramsToSet)
    }

    private data class NamingResult(val sql: String, val names: List<String>)

    private fun retrieveParamNamesToSet(tokens: List<String>, params: Map<String, Any?>): NamingResult {

        // TODO refactoring
        val resultTokens = mutableListOf<String>()
        val paramNames = mutableListOf<String>()
        for (token in tokens) {
            var found = false
            for (key in params.keys) {
                if (token == ":" + key) {
                    found = true
                    // Batch does not support collection spreading currently
                    resultTokens += "?"
                    paramNames += key
                    break
                }
            }
            if (!found) resultTokens += token
        }

        val sqlToExec = joinTokens(resultTokens)
        return NamingResult(sqlToExec, paramNames)
    }

    internal fun setParams(registry: AsashimoRegistry, stmt: PreparedStatement, paramsToSet: List<Any?>) {

        for ((i, param) in paramsToSet.withIndex()) {
            registry.setter.setValue(stmt, param, i + 1)
        }
    }

    @VisibleForTesting
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

    private fun joinTokens(tokens: List<String>): String {

        val builder = StringBuilder()

        for ((i, token) in tokens.withIndex()) {

            builder.append(token)

            if (i != tokens.lastIndex
                    && token != "("
                    && tokens[i + 1] != "("
                    && tokens[i + 1] != ")"
                    && tokens[i + 1] != ","
                    && tokens[i + 1] != ";") {
                builder.append(" ")
            }
        }

        return builder.toString()
    }

}
