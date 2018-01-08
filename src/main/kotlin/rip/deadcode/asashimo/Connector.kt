package rip.deadcode.asashimo

import java.sql.ResultSet
import kotlin.reflect.KClass

interface Connector {

    //    fun with(dsl: WithDsl): WithClause
    fun with(params: Map<String, Any>): WithClause

    fun with(block: (MutableMap<String, Any>) -> Unit): WithClause

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T

    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    fun exec(sql: String): Int
    fun execLarge(sql: String): Long

    fun <T> use(block: UseClause.() -> T): T
    fun <T> transactional(block: UseClause.() -> T): T

}

