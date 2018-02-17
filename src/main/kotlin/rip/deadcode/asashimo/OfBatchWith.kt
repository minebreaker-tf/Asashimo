package rip.deadcode.asashimo

interface OfBatchWith {

    fun with(params: Map<String, List<Any?>>): OfBatchWith
    fun exec(): IntArray

}
