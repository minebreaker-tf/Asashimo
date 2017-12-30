package rip.deadcode.asashimo

import java.sql.ResultSet
import kotlin.reflect.KClass

interface Connector {

    fun with(dsl: WithDsl): WithInterface
    fun with(dsl: Map<String, Any>): WithInterface

    fun <T : Any> fetch(sql: String, cls: KClass<T>, resultMapper: (ResultSet) -> T): T

    fun <T : Any> fetchAll(sql: String, cls: KClass<T>, resultMapper: (ResultSet) -> T): List<T>

    fun exec(sql: String): Int

    fun <T> use(block: UseClause.() -> T): T
    fun <T> transactional(block: UseClause.() -> T): T

}

