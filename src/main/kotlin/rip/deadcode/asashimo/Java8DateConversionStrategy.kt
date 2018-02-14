package rip.deadcode.asashimo

import rip.deadcode.asashimo.manipulation.Java8ConvertingRetriever
import rip.deadcode.asashimo.manipulation.Java8ConvertingSetter
import rip.deadcode.asashimo.manipulation.Retriever
import rip.deadcode.asashimo.manipulation.Setter
import rip.deadcode.asashimo.utils.Experimental

enum class Java8DateConversionStrategy {

    RAW {
        override fun getSetter(config: AsashimoConfig): Setter? = null
        override fun getRetriever(config: AsashimoConfig): Retriever? = null
    },

    @Experimental
    CONVERT {
        override fun getRetriever(config: AsashimoConfig): Retriever? = Java8ConvertingRetriever(config.databaseZoneOffset)
        override fun getSetter(config: AsashimoConfig): Setter? = Java8ConvertingSetter(config.databaseZoneOffset)
    },

    @Experimental
    CONVERT_NONLOCAL {
        override fun getRetriever(config: AsashimoConfig): Retriever? = TODO()
        override fun getSetter(config: AsashimoConfig): Setter? = TODO()
    }
    ;

    abstract fun getRetriever(config: AsashimoConfig): Retriever?
    abstract fun getSetter(config: AsashimoConfig): Setter?
}
