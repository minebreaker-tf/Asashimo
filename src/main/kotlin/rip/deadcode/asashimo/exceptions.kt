package rip.deadcode.asashimo

class AsashimoException(
        message: String,
        cause: Exception? = null
) : RuntimeException(message, cause)
