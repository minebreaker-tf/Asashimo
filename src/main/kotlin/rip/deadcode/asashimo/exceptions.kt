package rip.deadcode.asashimo

internal class AsashimoException(
        message: String,
        cause: Exception? = null
) : RuntimeException(message, cause)
