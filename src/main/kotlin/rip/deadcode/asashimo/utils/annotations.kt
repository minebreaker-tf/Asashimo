package rip.deadcode.asashimo.utils

/**
 * Indicates that annotated class or method is an experimental feature and may not work correctly.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Experimental
