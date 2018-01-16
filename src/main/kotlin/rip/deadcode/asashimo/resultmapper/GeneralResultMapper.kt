package rip.deadcode.asashimo.resultmapper

import rip.deadcode.asashimo.AsashimoRegistry
import java.sql.ResultSet
import kotlin.reflect.KClass

interface GeneralResultMapper {

    fun <T : Any> map(registry: AsashimoRegistry, cls: KClass<T>, resultSet: ResultSet): T
}
