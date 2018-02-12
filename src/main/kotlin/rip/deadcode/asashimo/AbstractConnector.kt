package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import rip.deadcode.asashimo.jpa.JpaIntrospector
import rip.deadcode.asashimo.jpa.JpaRunner
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

internal abstract class AbstractConnector(
        private val registry: AsashimoRegistry) : Connector {

    override fun with(block: (MutableMap<String, Any?>) -> Unit): OfWith {
        val params = mutableMapOf<String, Any?>()
        block(params)
        return OfWithImpl(getConnection(), registry, ::resetDataSourceCallback, params)
    }

    override fun with(params: Map<String, Any>): OfWith {
        return OfWithImpl(getConnection(), registry, ::resetDataSourceCallback, params)
    }

    override fun with(entity: Any): OfWith {
        return OfWithImpl(
                getConnection(), registry, ::resetDataSourceCallback, JpaIntrospector.getBindings(entity))
    }

    override fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T {
        return use { fetch(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): T? {
        return use { fetchMaybe(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): List<T> {
        return use { fetchAll(sql, cls, resultMapper) }
    }

    override fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<T> {
        return Supplier { use { fetch(sql, cls, resultMapper) } }
    }

    override fun <T : Any> fetchAllLazy(
            sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)?): Supplier<List<T>> {
        return Supplier { use { fetchAll(sql, cls, resultMapper) } }
    }

    override fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<T> {
        return (executorService ?: registry.executor).submit<T> {
            use { fetch(sql, cls, resultMapper) }
        }
    }

    override fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)?,
            executorService: ListeningExecutorService?): ListenableFuture<List<T>> {
        return (executorService ?: registry.executor).submit<List<T>> {
            use { fetchAll(sql, cls, resultMapper) }
        }
    }

    override fun exec(sql: String): Int {
        return use { exec(sql) }
    }

    override fun execLarge(sql: String): Long {
        return use { execLarge(sql) }
    }

    override fun execAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Int> {
        return (executorService ?: registry.executor).submit<Int> {
            use { exec(sql) }
        }
    }

    override fun execLargeAsync(sql: String, executorService: ListeningExecutorService?): ListenableFuture<Long> {
        return (executorService ?: registry.executor).submit<Long> {
            use { execLarge(sql) }
        }
    }

    override fun <T> use(block: OfUse.() -> T): T {
        return OfWithImpl(getConnection(), registry, ::resetDataSourceCallback, mapOf()).use(block)
    }

    override fun <T> useLazy(block: OfUse.() -> T): Supplier<T> {
        return Supplier { use(block) }
    }

    override fun <T> useAsync(
            executorService: ListeningExecutorService?, block: OfUse.() -> T): ListenableFuture<T> {
        return (executorService ?: registry.executor).submit<T> {
            use(block)
        }
    }

    override fun <T> transactional(block: OfUse.() -> T): T {
        return OfWithImpl(getConnection(), registry, ::resetDataSourceCallback, mapOf()).transactional(block)
    }

    override fun <T> transactionalLazy(block: OfUse.() -> T): Supplier<T> {
        return Supplier { transactional(block) }
    }

    override fun <T> transactionalAsync(
            executorService: ListeningExecutorService?, block: OfUse.() -> T): ListenableFuture<T> {
        return (executorService ?: registry.executor).submit<T> {
            transactional(block)
        }
    }

    override fun persist(entity: Any) {
        try {
            return JpaRunner.persist(registry, getConnection(), entity)
        } catch (e: Exception) {
            resetDataSourceCallback()
            throw AsashimoException("persist failed.", e)
        }
    }

    override fun <T : Any> find(id: Any, cls: KClass<T>): T {
        try {
            return JpaRunner.find(registry, getConnection(), id, cls)
        } catch (e: Exception) {
            resetDataSourceCallback()
            throw AsashimoException("find failed.", e)
        }
    }

    protected abstract fun getConnection(): Connection

    protected open fun resetDataSourceCallback() {
        // Do nothing
    }

}
