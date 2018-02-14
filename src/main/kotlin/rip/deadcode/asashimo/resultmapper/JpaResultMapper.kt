package rip.deadcode.asashimo.resultmapper

import rip.deadcode.asashimo.AsashimoException
import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import javax.persistence.Column
import kotlin.reflect.KClass

// TODO merge with BeanResultMapper

object JpaResultMapper : GeneralResultMapper {

    override fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T {

        val meta = resultSet.metaData
        val columnNamesWithIndex = (1..meta.columnCount).associate { it to meta.getColumnName(it) }
        // TODO upstream mode
        val fields = cls.java.declaredFields
        val correspondingColumnNames = fields.associateBy { field ->
            field.isAccessible = true
            field.getAnnotation(Column::class.java)?.name ?: field.name
        }

        val instance = try {
            cls.java.newInstance()
        } catch (e: InstantiationException) {
            throw AsashimoException("JpaResultMapper requires default noargs constructor.", e)
        }

        for ((i, column) in columnNamesWithIndex) {
            val field = correspondingColumnNames[column.toLowerCase()] ?: continue
            field.set(instance, registry.retriever.retrieveByClass(resultSet, cls, i))
        }

        return instance
    }

}
