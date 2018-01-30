package rip.deadcode.asashimo.utils

import com.google.common.base.CaseFormat

fun toLowerCamel(str: String): String {
    return when {
        str.contains("_") -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str.toLowerCase())
        str.contains("-") -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, str.toLowerCase())
        str.all { it.isUpperCase() } -> str.toLowerCase()
        else -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, str)
    }
}
