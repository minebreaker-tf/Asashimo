package rip.deadcode.asashimo.jpa

import com.google.common.collect.ImmutableSet
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

object JpaIntrospector {

    fun introspect(entity: Any): JpaIntrospectionResult {

        // TODO cache results

        val table = getTable(entity)
        val columns = getColumns(entity)
        return JpaIntrospectionResult(table, columns.id, columns.columns)
    }

    private fun getTable(entity: Any): String {
        val mark = entity::class.java.getDeclaredAnnotation(Entity::class.java)
        return mark?.name ?: entity::class.java.simpleName
    }

    private fun getColumns(entity: Any): Columns {

        val fields = entity::class.java.declaredFields
        var id: String? = null
        val columns = mutableSetOf<String>()

        for (f in fields) {

            val idMark = f.getAnnotation(Id::class.java)
            val columnMark = f.getAnnotation(Column::class.java)

            val name = columnMark?.name ?: f.name

            if (idMark != null) {
                id = name
            } else {
                columns += name
            }
        }

        return Columns(id!!, ImmutableSet.copyOf(columns))
    }

    private data class Columns(val id: String, val columns: Set<String>)

}

data class JpaIntrospectionResult(
        val table: String,
        val id: String,
        val columns: Set<String>
)
