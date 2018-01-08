package rip.deadcode.asashimo

import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

@FunctionalInterface
interface UseClause {

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>
    fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<T>
    fun <T : Any> fetchAllLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<List<T>>
    fun exec(sql: String): Int
    fun execLarge(sql: String): Long
}
