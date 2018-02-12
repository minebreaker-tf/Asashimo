package rip.deadcode.asashimo

import rip.deadcode.asashimo.utils.Experimental
import java.time.ZoneOffset

data class AsashimoConfig(
        val resetDataSourceWhenExceptionOccurred: Boolean = true,
        @Experimental
        val java8dateConversionStrategy: Java8DateConversionStrategy = Java8DateConversionStrategy.RAW,
        @Experimental
        val databaseZoneOffset: ZoneOffset = ZoneOffset.UTC
)
