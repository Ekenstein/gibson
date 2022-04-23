package com.github.ekenstein.gibson

import java.time.LocalDateTime

/**
 * A structured representation of a color in a GIB document.
 */
enum class GibColor { Black, White }

private fun GibColor.flip() = when (this) {
    GibColor.Black -> GibColor.White
    GibColor.White -> GibColor.Black
}

/**
 * A structured representation of a GIB document containing the [header] properties
 * of the document together with the [game] tree.
 *
 * @param header A key-value map containing the properties of the game.
 * @param game An ordered list containing the game tree.
 */
data class Gib(val header: Map<String, String>, val game: List<GameProperty>) {
    private val gameInfo: Map<String, String> by lazy {
        header["GAMEINFOMAIN"]?.split(",").orEmpty().associate {
            val (name, value) = it.split(":")
            name to value
        }
    }

    /**
     * The specified handicap of the game. The returned value is always equal to or larger than 0.
     */
    val handicap by lazy { game.filterIsInstance<GameProperty.INI>().map { it.handicap }.singleOrNull() ?: 0 }

    /**
     * The specified komi of the game, or null if there was no komi specified in the header.
     * The value is always divided by 10 such that if the GIB document specifies a komi of 65,
     * the returned value will be 6.5.
     */
    val komi by lazy { header["GAMEGONGJE"]?.toIntOrNull()?.let { it / 10.0 } }

    /**
     * The place of where the game occurred or null if there was no game place specified in the header.
     */
    val gamePlace by lazy { header["GAMEPLACE"] }

    /**
     * The name of the black player, or null if there was no player name specified in the header.
     */
    val playerBlack by lazy { header["GAMEBLACKNAME"] }

    /**
     * The name of the white player, or null if there was no player name specified in the header.
     */
    val playerWhite by lazy { header["GAMEWHITENAME"] }

    private val gameScore by lazy { header["GAMEZIPSU"]?.toIntOrNull()?.let { it / 10.0 } ?: 0.0 }

    /**
     * The result of the game in form of [GameResult] or null if either the result couldn't be recognized
     * or if there was no game result specified in the header.
     */
    val gameResult by lazy {
        when (gameInfo["GRLT"]?.toIntOrNull()) {
            0 -> GameResult.Score(GibColor.Black, gameScore)
            1 -> GameResult.Score(GibColor.White, gameScore)
            3 -> GameResult.Resignation(GibColor.Black)
            4 -> GameResult.Resignation(GibColor.White)
            7 -> GameResult.Time(GibColor.Black)
            8 -> GameResult.Time(GibColor.White)
            else -> null
        }
    }

    val moves by lazy {
        val startingColor = if (handicap >= 2) GibColor.White else GibColor.Black

        fun colorFromMoveNumber(moveNumber: Int) = when (moveNumber % 2) {
            0 -> startingColor.flip()
            else -> startingColor
        }

        game.mapNotNull {
            when (it) {
                is GameProperty.INI -> null
                is GameProperty.SKI -> Move.Pass(colorFromMoveNumber(it.moveNumber), it.moveNumber)
                is GameProperty.STO -> Move.Point(it.color, it.moveNumber, it.x, it.y)
            }
        }
    }

    /**
     * The time settings of the game in form of [TimeSettings], or null if there was no time settings
     * specified in the header.
     */
    val timeSettings by lazy {
        gameInfo["GTIME"]?.let { string ->
            val parts = string.split("-")
            parts.takeIf { it.size == 3 }?.let { (timeLimit, seconds, stones) ->
                TimeSettings(timeLimit.toInt(), seconds.toInt(), stones.toInt())
            }
        }
    }

    /**
     * The date and time of the game, or null if there was no game date specified in the header.
     */
    val gameDate by lazy {
        header["GAMEDATE"]?.let { string ->
            string.split("-").takeIf { it.size == 6 }?.let { parts ->
                LocalDateTime.of(
                    parts[0].trim().toInt(),
                    parts[1].trim().toInt(),
                    parts[2].trim().toInt(),
                    parts[3].trim().toInt(),
                    parts[4].trim().toInt(),
                    parts[5].trim().toInt(),
                    0
                )
            }
        }
    }

    companion object
}

/**
 * Describes the time settings of the game.
 * @param timeLimit The time limit in seconds.
 * @param overtimeSeconds The number of seconds each overtime period have.
 * @param overtimePeriods The number of overtime periods.
 */
data class TimeSettings(val timeLimit: Int, val overtimeSeconds: Int, val overtimePeriods: Int)

/**
 * The base class for a game property of a GIB document.
 */
sealed class GameProperty {
    /**
     * Describes a move in the game tree. The coordinates are zero-based.
     */
    data class STO(val moveNumber: Int, val color: GibColor, val x: Int, val y: Int) : GameProperty()

    /**
     * Describes the handicap setting of the game.
     */
    data class INI(val handicap: Int) : GameProperty()

    /**
     * Describes a pass in the game tree.
     */
    data class SKI(val moveNumber: Int) : GameProperty()
}

/**
 * The base class for a move in a GIB document. A move always consist of a color and a move number.
 */
sealed class Move {
    abstract val color: GibColor
    abstract val moveNumber: Int

    /**
     * The player placed a stone on a specified point of the board.
     * The coordinates are zero-based.
     */
    data class Point(
        override val color: GibColor,
        override val moveNumber: Int,
        val x: Int,
        val y: Int
    ) : Move()

    /**
     * The player passed.
     */
    data class Pass(override val color: GibColor, override val moveNumber: Int) : Move()
}

/**
 * Describes the various types of game result a GIB document can specify. A game result always contain a winner
 * in form of a [GibColor].
 */
sealed class GameResult {
    abstract val winner: GibColor

    /**
     * The [winner] won by the given [score].
     * @param winner The winner of the game.
     * @param score The score the winner won by.
     */
    data class Score(override val winner: GibColor, val score: Double) : GameResult()

    /**
     * The [winner] won by resignation.
     * @param winner The winner of the game.
     */
    data class Resignation(override val winner: GibColor) : GameResult()

    /**
     * The [winner] won by time.
     * @param winner The winner of the game.
     */
    data class Time(override val winner: GibColor) : GameResult()
}
