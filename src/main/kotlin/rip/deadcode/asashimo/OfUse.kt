package rip.deadcode.asashimo

import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.sql.ResultSet
import kotlin.reflect.KClass

@FunctionalInterface
interface OfUse {

    fun bind(name: String, value: Any?) {
        bind(name to value)
    }

    fun bind(binding: Pair<String, Any?>)
    fun bind(block: (MutableMap<String, Any?>) -> Unit)

    /**
     * @see Connector.fetch
     */
    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T

    /**
     * @see Connector.fetchMaybe
     */
    fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T?

    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    /**
     * @see Connector.exec
     */
    @CanIgnoreReturnValue
    fun exec(sql: String): Int

    /**
     * @see Connector.execLarge
     */
    @CanIgnoreReturnValue
    fun execLarge(sql: String): Long

}
