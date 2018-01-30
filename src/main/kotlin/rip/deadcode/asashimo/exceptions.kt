package rip.deadcode.asashimo

open class AsashimoException(
        message: String,
        cause: Exception? = null
) : RuntimeException(message, cause)

class AsashimoNoResultException : AsashimoException("ResultSet was empty.")

class AsashimoNonUniqueResultException : AsashimoException("Method `fetch` was called, but the ResultSet returned multiple rows.")
