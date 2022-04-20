package com.github.ekenstein.gibson.parser

/**
 * Describes a position in a GIB document.
 */
data class Marker(
    val startLineNumber: Int,
    val startColumn: Int,
    val endLineNumber: Int,
    val endColumn: Int
)

/**
 * A base class for all the exceptions that might be thrown during processing of a GIB document.
 */
sealed class GibException : RuntimeException() {
    /**
     * An error occurred while parsing a GIB document.
     * @param description Description of the parse error.
     * @param marker The position in the GIB document of where the parse error occurred.
     * @param cause The cause to why this parse error happened, if any.
     */
    data class ParseError(
        val description: String,
        val marker: Marker,
        override val cause: Throwable?
    ) : GibException() {
        constructor(description: String, marker: Marker) : this(description, marker, null)

        override val message: String =
            "gib parse error, on line ${marker.startLineNumber}, column ${marker.startColumn}: $description"
    }
}
