package rip.deadcode.asashimo

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.sql.ResultSet
import kotlin.reflect.KClass

@FunctionalInterface
interface UseClause {

    fun bind(name: String, value: Any?) {
        bind(name to value)
    }

    fun bind(binding: Pair<String, Any?>)
    fun bind(block: (MutableMap<String, Any?>) -> Unit)

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T
    fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T?
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    @CanIgnoreReturnValue
    fun exec(sql: String): Int

    @CanIgnoreReturnValue
    fun execLarge(sql: String): Long

}
