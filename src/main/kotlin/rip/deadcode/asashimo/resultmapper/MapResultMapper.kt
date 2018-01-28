package rip.deadcode.asashimo.resultmapper

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Preconditions.checkArgument
import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import kotlin.reflect.KClass

object MapResultMapper : GeneralResultMapper {

    override fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T {
        checkArgument(cls == Map::class, "Specify 'java.util.Map' as a result type when you use MapResultMapper.")
        @Suppress("UNCHECKED_CAST")
        return convertToMap(resultSet) as T
    }

    @VisibleForTesting
    internal fun convertToMap(resultSet: ResultSet): Map<String, String> {

        val meta = resultSet.metaData
        return (1..meta.columnCount)
                // Database may be case insensitive. To avoid confusion always assures keys to be lower case.
                .associateBy({ meta.getColumnName(it).toLowerCase() }, { resultSet.getString(it) })
                .toMap()
    }

}