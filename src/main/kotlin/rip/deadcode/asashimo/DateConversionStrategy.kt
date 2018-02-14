package rip.deadcode.asashimo

import rip.deadcode.asashimo.manipulation.*
import rip.deadcode.asashimo.utils.Experimental

enum class DateConversionStrategy {

    RAW {
        override fun getSetter(config: AsashimoConfig): Setter? = null
        override fun getRetriever(config: AsashimoConfig): Retriever? = null
    },

    @Experimental
    CONVERT_TO_CLASSIC {
        override fun getRetriever(config: AsashimoConfig): Retriever? = ConvertToClassicRetriever(config.databaseZoneOffset)
        override fun getSetter(config: AsashimoConfig): Setter? = ConvertToClassicSetter(config.databaseZoneOffset)
    },

    @Experimental
    CONVERT_NONLOCAL_TO_LOCAL {
        override fun getRetriever(config: AsashimoConfig): Retriever? = ConvertNonLocalToLocalRetriever(config.databaseZoneOffset)
        override fun getSetter(config: AsashimoConfig): Setter? = ConvertNonLocalToLocalSetter(config.databaseZoneOffset)
    };

    abstract fun getRetriever(config: AsashimoConfig): Retriever?
    abstract fun getSetter(config: AsashimoConfig): Setter?
}
