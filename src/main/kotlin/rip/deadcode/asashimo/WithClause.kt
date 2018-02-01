package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

@FunctionalInterface
interface WithClause {

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

    /**
     * @see Connector.fetchAll
     */
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    /**
     * @see Connector.fetchLazy
     */
    fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<T>

    /**
     * @see Connector.fetchAllLazy
     */
    fun <T : Any> fetchAllLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<List<T>>

    /**
     * @see Connector.fetchAsync
     */
    fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null): ListenableFuture<T>

    /**
     * @see Connector.fetchAllAsync
     */
    fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null): ListenableFuture<List<T>>

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

    /**
     * @see Connector.execAsync
     */
    fun execAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Int>

    /**
     * @see Connector.execLargeAsync
     */
    fun execLargeAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Long>

    /**
     * @see Connector.use
     */
    @CanIgnoreReturnValue
    fun <T> use(block: UseClause.() -> T): T

    /**
     * @see Connector.useLazy
     */
    fun <T> useLazy(block: UseClause.() -> T): Supplier<T>

    /**
     * @see Connector.useAsync
     */
    fun <T> useAsync(executorService: ListeningExecutorService? = null, block: UseClause.() -> T): ListenableFuture<T>

    /**
     * @see Connector.transactional
     */
    @CanIgnoreReturnValue
    fun <T> transactional(block: UseClause.() -> T): T

    /**
     * @see Connector.transactionalLazy
     */
    fun <T> transactionalLazy(block: UseClause.() -> T): Supplier<T>

    /**
     * @see Connector.transactionalAsync
     */
    fun <T> transactionalAsync(
            executorService: ListeningExecutorService? = null, block: UseClause.() -> T): ListenableFuture<T>
}
