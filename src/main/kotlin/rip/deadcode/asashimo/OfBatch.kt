package rip.deadcode.asashimo

interface OfBatch {

    val sqls: List<String>

    /**
     * Add SQL to the current batch.
     * Must not contain parameters.
     */
    fun add(sql: String)

}
