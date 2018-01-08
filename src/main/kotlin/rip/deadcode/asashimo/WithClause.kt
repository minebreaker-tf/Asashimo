package rip.deadcode.asashimo

import java.sql.ResultSet
import kotlin.reflect.KClass

@FunctionalInterface
interface WithClause {

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>
    fun exec(sql: String): Int
    fun execLarge(sql: String): Long
    fun <T> use(block: UseClause.() -> T): T
    fun <T> transactional(block: UseClause.() -> T): T
}
