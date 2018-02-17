package rip.deadcode.asashimo

interface OfTransactional : OfUse {

    fun <T> savepoint(name: String? = null, block: OfUse.() -> T): T?
}
