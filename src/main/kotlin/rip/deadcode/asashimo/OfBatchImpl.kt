package rip.deadcode.asashimo

class OfBatchImpl : OfBatch {

    private val _sqls: MutableList<String> = mutableListOf()

    override val sqls: List<String>
        get() = _sqls

    override fun add(sql: String) {
        _sqls += sql
    }

}
