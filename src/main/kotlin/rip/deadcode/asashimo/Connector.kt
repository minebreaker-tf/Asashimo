package rip.deadcode.asashimo

import kotlin.reflect.KClass

interface Connector {

    //    fun with(parameters:Map<String, SQLValue>): WithClause
    fun with(dsl: WithDsl): WithClause

    fun <T> fetch(sql: String, cls: Class<T>): T
    fun <T : Any> fetch(sql: String, cls: KClass<T>): T

    fun <T> fetchAll(sql: String, cls: Class<T>): List<T>

}

interface WithClause {

}

interface WithDsl {

}
