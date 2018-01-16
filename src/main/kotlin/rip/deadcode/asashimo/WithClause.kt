package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.errorprone.annotations.CanIgnoreReturnValue
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

@FunctionalInterface
interface WithClause {

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T
    fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T?
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>
    fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<T>
    fun <T : Any> fetchAllLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<List<T>>

    fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null): ListenableFuture<T>

    fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null): ListenableFuture<List<T>>

    @CanIgnoreReturnValue
    fun exec(sql: String): Int

    @CanIgnoreReturnValue
    fun execLarge(sql: String): Long

    fun execAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Int>
    fun execLargeAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Long>

    @CanIgnoreReturnValue
    fun <T> use(block: UseClause.() -> T): T

    fun <T> useLazy(block: UseClause.() -> T): Supplier<T>
    fun <T> useAsync(executorService: ListeningExecutorService? = null, block: UseClause.() -> T): ListenableFuture<T>

    @CanIgnoreReturnValue
    fun <T> transactional(block: UseClause.() -> T): T

    fun <T> transactionalLazy(block: UseClause.() -> T): Supplier<T>
    fun <T> transactionalAsync(
            executorService: ListeningExecutorService? = null, block: UseClause.() -> T): ListenableFuture<T>
}
