package rip.deadcode.asashimo.manipulation

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

interface Retriever {

    fun withFallback(downstream: Retriever): Retriever {
        return FallingRetriever(this, downstream)
    }

    fun <T : Any> retrievable(cls: KClass<T>): Boolean
    fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T?
}

interface Setter {

    fun withFallback(downstream: Setter): Setter {
        return FallbackSetter(this, downstream)
    }

    fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean
}

private class FallingRetriever(
        val upstream: Retriever,
        val downstream: Retriever
) : Retriever {

    override fun <T : Any> retrievable(cls: KClass<T>): Boolean {
        return upstream.retrievable(cls)
                || downstream.retrievable(cls)
    }

    override fun <T : Any> retrieveByClass(rs: ResultSet, cls: KClass<T>, index: Int): T? {
        return if (upstream.retrievable(cls)) {
            upstream.retrieveByClass(rs, cls, index)
        } else if (downstream.retrievable(cls)) {
            downstream.retrieveByClass(rs, cls, index)
        } else {
            throw RuntimeException()
        }

    }
}

private class FallbackSetter(
        val upstream: Setter,
        val downstream: Setter
) : Setter {

    override fun setValue(stmt: PreparedStatement, param: Any?, index: Int): Boolean {
        return upstream.setValue(stmt, param, index)
                || downstream.setValue(stmt, param, index)
    }
}
