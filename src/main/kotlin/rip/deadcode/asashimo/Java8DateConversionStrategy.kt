package rip.deadcode.asashimo

import rip.deadcode.asashimo.utils.Experimental

enum class Java8DateConversionStrategy {
    RAW,
    @Experimental
    CONVERT,
    @Experimental
    CONVERT_NONLOCAL,
}
