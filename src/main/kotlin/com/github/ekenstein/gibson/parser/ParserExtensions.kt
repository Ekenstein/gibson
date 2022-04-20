package com.github.ekenstein.gibson.parser

import com.github.ekenstein.gibson.GameProperty
import com.github.ekenstein.gibson.Gib
import com.github.ekenstein.gibson.GibColor
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import java.io.InputStream
import java.nio.file.Path

private val gibErrorListener = object : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        throw GibException.ParseError(msg, Marker(line, charPositionInLine, line, charPositionInLine), e)
    }
}

/**
 * Parses the given [string] into [Gib].
 *
 * If the [string] does not contain a valid GIB document, a [GibException.ParseError] will be thrown.
 * @param string The string containing the contents of a GIB file.
 * @return A structured representation of the GIB document in form of [Gib]
 * @throws [GibException.ParseError] if the string doesn't contain a valid GIB document.
 */
fun Gib.Companion.from(string: String) = from(CharStreams.fromString(string))

/**
 * Parses the GIB file located at the given [path] into [Gib].
 *
 * If the indicated file doesn't contain a valid GIB document, a [GibException.ParseError] will be thrown.
 * @param path The path to the file to parse.
 * @return A structured representation of the GIB file in form of [Gib]
 * @throws [GibException.ParseError] if the file does not contain a valid GIB document.
 */
fun Gib.Companion.from(path: Path) = from(CharStreams.fromPath(path))

/**
 * Parses the given [inputStream] into [Gib].
 *
 * If the [inputStream] does not contain a valid GIB document, a [GibException.ParseError] will be thrown.
 * @param inputStream The input stream containing the GIB document
 * @return A structured representation of the GIB document in form of [Gib]
 * @throws [GibException.ParseError] if the input stream doesn't contain a valid GIB document.
 */
fun Gib.Companion.from(inputStream: InputStream) = from(CharStreams.fromStream(inputStream))

private fun Gib.Companion.from(charStream: CharStream): Gib {
    val lexer = GibLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(gibErrorListener)
    val tokenStream = CommonTokenStream(lexer)
    val parser = GibParser(tokenStream)
    parser.removeErrorListeners()
    parser.addErrorListener(gibErrorListener)

    return extractGib(parser)
}

private fun extractGib(parser: GibParser): Gib {
    val header = extractHeader(parser.header())
    val game = extractGame(parser.game())
    return Gib(header, game)
}

private fun extractGame(ctx: GibParser.GameContext) = ctx.game_property().mapNotNull { extractGameProperty(it) }

private fun extractGameProperty(ctx: GibParser.Game_propertyContext) = when (ctx) {
    is GibParser.MoveContext -> GameProperty.STO(
        ctx.moveNumber.toInt(),
        ctx.player.toColor(),
        ctx.x.toInt(),
        ctx.y.toInt()
    )
    is GibParser.IniContext -> GameProperty.INI(
        ctx.handicap.toInt()
    )
    is GibParser.PassContext -> GameProperty.SKI(
        ctx.moveNumber.toInt()
    )
    else -> null
}

private fun extractHeader(ctx: GibParser.HeaderContext) = ctx.header_property().associate { extractHeaderProperty(it) }

private fun extractHeaderProperty(ctx: GibParser.Header_propertyContext): Pair<String, String> {
    val identifier = ctx.property_identifier().text
    val value = ctx.VALUE()?.text
    val strippedValue = value?.substring(1, value.length - 2) ?: ""

    return identifier to strippedValue
}

private fun Token.toInt(): Int = text.toIntOrNull()
    ?: throw GibException.ParseError("Expected an integer, but got $text", toMarker())

private fun Token.toColor(): GibColor {
    return when (toInt()) {
        1 -> GibColor.Black
        2 -> GibColor.White
        else -> throw GibException.ParseError("Expected either '1' or '2' but got $text", toMarker())
    }
}

private fun Token.toMarker(): Marker {
    val startColumn = charPositionInLine + 1

    return Marker(
        startLineNumber = line,
        startColumn = startColumn,
        endLineNumber = line,
        endColumn = startColumn + text.length
    )
}
