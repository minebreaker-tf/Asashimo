package rip.deadcode.asashimo.resultmapper

import com.google.common.annotations.VisibleForTesting
import org.slf4j.LoggerFactory
import rip.deadcode.asashimo.AsashimoException
import rip.deadcode.asashimo.AsashimoRegistry
import rip.deadcode.asashimo.utils.toLowerCamel
import java.beans.Introspector
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

object BeanResultMapper : GeneralResultMapper {

    private val logger = LoggerFactory.getLogger(BeanResultMapper::class.java)

    override fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T {
        return convertToBasicType(cls, resultSet)
                ?: convertAsBean(cls, resultSet)
                ?: throw AsashimoException("Failed to map ResultSet to class '${cls.java.canonicalName}'")
    }

    @VisibleForTesting
    internal fun <T : Any> convertAsBean(cls: KClass<T>, resultSet: ResultSet): T? {

        return try {
            val constructor = cls.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val targetInstance = constructor.newInstance()
            val meta = resultSet.metaData
            val columnNames = (1..meta.columnCount).map { meta.getColumnName(it) }

            // Via methods
            val properties = Introspector.getBeanInfo(cls.java).propertyDescriptors
            for (columnName in columnNames) {
                val property = properties.first { it.name == toLowerCamel(columnName) }
                // TODO respect transient
                val writer = property.writeMethod
                writer.isAccessible = true
                writer.invoke(targetInstance, resultSet.getObject(columnName, property.propertyType))
                // TODO 全てのフィールドが適切に設定されたかチェック
            }
            return targetInstance

            // Via fields
        } catch (e: Exception) {
            when (e) {
                is SQLException -> throw e
                else -> {
                    logger.info("Failed during invoking setters.", e)
                    null
                }
            }
        }
    }

}