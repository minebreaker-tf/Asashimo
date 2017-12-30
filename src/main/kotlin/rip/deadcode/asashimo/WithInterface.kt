package rip.deadcode.asashimo

import kotlin.reflect.KClass

@FunctionalInterface
interface WithInterface {

    fun <T : Any> fetch(sql: String, cls: KClass<T>): T

    fun <T : Any> fetchAll(sql: String, cls: KClass<T>): List<T>

    fun <T : Any> exec(sql: String, cls: KClass<T>): Long

    fun <T> use(block: (UseClause) -> T): T
    fun <T> transactional(block: (UseClause) -> T): T
}