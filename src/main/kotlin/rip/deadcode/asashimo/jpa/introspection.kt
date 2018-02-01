package rip.deadcode.asashimo.jpa

import com.google.common.collect.ImmutableList
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table
import kotlin.reflect.KClass

object JpaIntrospector {

    // TODO refactoring

    fun introspect(entity: Any): JpaIntrospectionResult {

        // TODO cache results

        val table = getTable(entity::class)
        val columns = getColumns(entity)
        return JpaIntrospectionResult(table, columns.idName, columns.id, columns.columnNames, columns.columns)
    }

    fun introspect(cls: KClass<*>): JpaIntrospectionTypeResult {
        val table = getTable(cls)
        val columns = getColumns(cls)
        return JpaIntrospectionTypeResult(table, columns.idName, columns.columnsName)
    }

    fun getBindings(entity: Any): Map<String, Any?> {

        val fields = entity::class.java.declaredFields
        val columnNames = mutableListOf<String>()
        val columns = mutableListOf<Any>()

        for (f in fields) {
            f.isAccessible = true

            val columnMark = f.getAnnotation(Column::class.java)
            val name = columnMark?.name ?: f.name

            columnNames += name
            columns += f.get(entity)
        }

        return (columnNames zip columns).associate { it }
    }

    private fun getTable(cls: KClass<*>): String {
        val mark = cls.java.getDeclaredAnnotation(Table::class.java)
        return mark?.name ?: cls.java.simpleName
    }

    private fun getColumns(entity: Any): Columns {

        val fields = entity::class.java.declaredFields
        var idName: String? = null
        var id: Any? = null
        val columnNames = mutableListOf<String>()
        val columns = mutableListOf<Any>()

        for (f in fields) {
            f.isAccessible = true

            val idMark = f.getAnnotation(Id::class.java)
            val columnMark = f.getAnnotation(Column::class.java)

            val name = columnMark?.name ?: f.name

            if (idMark != null) {
                idName = name
                id = f.get(entity)
            } else {
                columnNames += name
                columns += f.get(entity)
            }
        }

        return Columns(idName!!, id!!, ImmutableList.copyOf(columnNames), columns)
    }

    private data class Columns(
            val idName: String,
            val id: Any,
            val columnNames: List<String>,
            val columns: List<Any?>
    )

    private fun getColumns(cls: KClass<*>): ColumnsType {

        val fields = cls.java.declaredFields
        var idName: String? = null
        val columnNames = mutableListOf<String>()

        for (f in fields) {
            f.isAccessible = true

            val idMark = f.getAnnotation(Id::class.java)
            val columnMark = f.getAnnotation(Column::class.java)

            val name = columnMark?.name ?: f.name

            if (idMark != null) {
                idName = name
            } else {
                columnNames += name
            }
        }

        return ColumnsType(idName!!, ImmutableList.copyOf(columnNames))
    }

    private data class ColumnsType(
            val idName: String,
            val columnsName: List<String>
    )

}

data class JpaIntrospectionResult(
        val tableName: String,
        val idName: String,
        val id: Any,
        val columnNames: List<String>,
        val columns: List<Any?>
)

data class JpaIntrospectionTypeResult(
        val tableName: String,
        val idName: String,
        val columnNames: List<String>
)
