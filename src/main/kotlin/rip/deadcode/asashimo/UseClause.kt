package rip.deadcode.asashimo

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.sql.ResultSet
import kotlin.reflect.KClass

@FunctionalInterface
interface UseClause {

    fun with(name: String, value: Any?) {
        with(name to value)
    }

    fun with(binding: Pair<String, Any?>)
    fun with(block: (MutableMap<String, Any?>) -> Unit)

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T
    fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T?
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    @CanIgnoreReturnValue
    fun exec(sql: String): Int

    @CanIgnoreReturnValue
    fun execLarge(sql: String): Long

}
