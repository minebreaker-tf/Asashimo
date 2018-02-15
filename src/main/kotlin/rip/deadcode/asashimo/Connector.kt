package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.errorprone.annotations.CanIgnoreReturnValue
import rip.deadcode.asashimo.utils.Experimental
import java.sql.ResultSet
import java.util.function.Supplier
import kotlin.reflect.KClass

interface Connector {

    /**
     * Binds given map as named parameters of the SQL.
     *
     * ```
     * connector.with(mapOf("id" to 1)).fetch("select * from user where id = :id", User::class)
     * ```
     */
    fun with(params: Map<String, Any>): OfWith

    /**
     * Binds given parameters as named parameters of the SQL.
     *
     * ```
     * connector.with { it["id"] = 1 }.fetch("select * from user where id = :id", User::class)
     * ```
     */
    fun with(block: (MutableMap<String, Any?>) -> Unit): OfWith

    /**
     * Binds fields of the given entity as named parameters of the SQL.
     * If JPA annotations ([javax.persistence.Column]) is used, the name is recognized as a parameter name.
     */
    fun with(entity: Any): OfWith

    /**
     * Fetches the database using the given SQL. The result must have an exactly one row.
     *
     * ```
     * val result: User = connector.fetch("select * from user", User::class)
     * ```
     *
     * @param sql SQL to be executed
     * @param cls Class that mapped by given [resultMapper]
     * @param resultMapper An object that maps the result of the SQL.
     *                     If omitted, default [rip.deadcode.asashimo.resultmapper.GeneralResultMapper] is used.
     * @return A result of the executed SQL, which is an instance of the [cls]
     * @throws AsashimoNoResultException If the result is emtpy.
     * @throws AsashimoNonUniqueResultException If the result had 2 or more rows.
     */
    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T

    /**
     * Fetches the database using the given SQL. The returned object can be `null` if the result was empty/
     *
     * ```
     * val result: User? = connector.fetchMaybe("select * from user", User::class)
     * ```
     *
     * @param sql SQL to be executed
     * @param cls Class that mapped by given [resultMapper]
     * @param resultMapper An object that maps the result of the SQL.
     *                     If omitted, default [rip.deadcode.asashimo.resultmapper.GeneralResultMapper] is used.
     * @return A result of the executed SQL, which is an instance of the [cls]. Or `null` if the result is empty.
     * @throws AsashimoNonUniqueResultException If the result had 2 or more rows.
     */
    fun <T : Any> fetchMaybe(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): T?

    /**
     * Fetches the database using the given SQL.
     *
     * ```
     * connector.fetchAll("select * from user", User::class)
     * ```
     *
     * @param sql SQL to be executed
     * @param cls Class that mapped by given [resultMapper]
     * @param resultMapper An object that maps the result of the SQL.
     *                     If omitted, default [rip.deadcode.asashimo.resultmapper.GeneralResultMapper] is used.
     * @return A list of the result rows of the executed SQL
     */
    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): List<T>

    fun <T : Any> fetchLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<T>
    fun <T : Any> fetchAllLazy(sql: String, cls: KClass<T>, resultMapper: ((ResultSet) -> T)? = null): Supplier<List<T>>

    fun <T : Any> fetchAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null
    ): ListenableFuture<T>

    fun <T : Any> fetchAllAsync(
            sql: String,
            cls: KClass<T>,
            resultMapper: ((ResultSet) -> T)? = null,
            executorService: ListeningExecutorService? = null
    ): ListenableFuture<List<T>>

    /**
     * Executes the SQL.
     *
     * ```
     * connector.exec("insert into user values(1, 'John')")
     * ```
     *
     * @param sql The SQL executed
     */
    @CanIgnoreReturnValue
    fun exec(sql: String): Int

    @CanIgnoreReturnValue
    fun execLarge(sql: String): Long

    fun execAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Int>
    fun execLargeAsync(sql: String, executorService: ListeningExecutorService? = null): ListenableFuture<Long>

    @CanIgnoreReturnValue
    fun <T> use(block: OfUse.() -> T): T

    fun <T> useLazy(block: OfUse.() -> T): Supplier<T>
    fun <T> useAsync(executorService: ListeningExecutorService? = null, block: OfUse.() -> T): ListenableFuture<T>

    @CanIgnoreReturnValue
    fun <T> transactional(block: OfTransactional.() -> T): T

    fun <T> transactionalLazy(block: OfTransactional.() -> T): Supplier<T>
    fun <T> transactionalAsync(
            executorService: ListeningExecutorService? = null, block: OfTransactional.() -> T): ListenableFuture<T>

    @Experimental
    fun persist(entity: Any)

    @Experimental
    fun <T : Any> find(id: Any, cls: KClass<T>): T

    @Experimental
    fun batch(block: OfBatch.() -> Unit): IntArray

    @Experimental
    fun batch(sql: String): OfBatchWith

}
