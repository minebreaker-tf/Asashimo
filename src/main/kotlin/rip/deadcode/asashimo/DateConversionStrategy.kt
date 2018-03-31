package rip.deadcode.asashimo

import rip.deadcode.asashimo.manipulation.*
import rip.deadcode.asashimo.utils.Experimental

/**
 * Defines the strategy how to get values from [java.sql.ResultSet],
 * or to set values to the [java.sql.PreparedStatement].
 */
enum class DateConversionStrategy {

    /**
     * Get/set values as is, using [java.sql.ResultSet.getObject] or [java.sql.PreparedStatement.setObject].
     */
    RAW {
        override fun getSetter(config: AsashimoConfig): Setter = AnySetter
        override fun getRetriever(config: AsashimoConfig): Retriever = RawRetriever
    },

    /**
     * Converts all Java 8 Date And Time API classes to its corresponding SQL type,
     * i.e. [java.sql.Date], [java.sql.Time], [java.sql.Timestamp].
     * Useful for JDBC driver that does not support Java 8 types.
     */
    @Experimental
    CONVERT_TO_CLASSIC {
        override fun getRetriever(config: AsashimoConfig): Retriever = ConvertToClassicRetriever(config.databaseZoneOffset)
        override fun getSetter(config: AsashimoConfig): Setter = ConvertToClassicSetter(config.databaseZoneOffset)
    },

    /**
     * Converts non-local Java 8 types to local ones.
     *
     * [java.time.ZonedDateTime] ⇔ [java.time.LocalDateTime],
     * [java.time.OffsetDateTime] ⇔ [java.time.LocalDateTime],
     * [java.time.OffsetTime] ⇔ [java.time.LocalTime]
     */
    @Experimental
    CONVERT_NONLOCAL_TO_LOCAL {
        override fun getRetriever(config: AsashimoConfig): Retriever = ConvertNonLocalToLocalRetriever(config.databaseZoneOffset)
        override fun getSetter(config: AsashimoConfig): Setter = ConvertNonLocalToLocalSetter(config.databaseZoneOffset)
    };

    abstract fun getRetriever(config: AsashimoConfig): Retriever
    abstract fun getSetter(config: AsashimoConfig): Setter
}
