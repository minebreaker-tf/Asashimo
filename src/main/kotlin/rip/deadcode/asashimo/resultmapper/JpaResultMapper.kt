package rip.deadcode.asashimo.resultmapper

import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import kotlin.reflect.KClass

object JpaResultMapper : GeneralResultMapper {

    override fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T {
        TODO("not implemented yet")
    }
}
