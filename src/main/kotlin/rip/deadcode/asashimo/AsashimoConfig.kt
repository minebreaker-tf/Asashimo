package rip.deadcode.asashimo

import com.google.common.annotations.Beta
import java.time.ZoneOffset

data class AsashimoConfig(
        val resetDataSourceWhenExceptionOccurred: Boolean = true,
        @Beta
        val java8dateConversionStrategy: Java8DateConversionStrategy = Java8DateConversionStrategy.RAW,
        @Beta
        val databaseZoneOffset: ZoneOffset = ZoneOffset.UTC
)
