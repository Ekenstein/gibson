package com.github.ekenstein.gibson

import java.time.OffsetDateTime
import java.time.ZoneOffset

enum class GibColor { Black, White }

data class Gib(val header: Map<String, String>, val game: List<GameProperty>) {
    private val gameInfo: Map<String, String> by lazy {
        header["GAMEINFOMAIN"]?.split(",").orEmpty().associate {
            val (name, value) = it.split(":")
            name to value
        }
    }

    val handicap by lazy { game.filterIsInstance<GameProperty.INI>().map { it.handicap }.singleOrNull() ?: 0 }

    val komi by lazy { header["GAMEGONGJE"]?.toIntOrNull()?.let { it / 10.0 } }

    val gamePlace by lazy { header["GAMEPLACE"] }

    val playerBlack by lazy { header["GAMEBLACKNAME"] }

    val playerWhite by lazy { header["GAMEWHITENAME"] }

    private val gameScore by lazy { header["GAMEZIPSU"]?.toIntOrNull()?.let { it / 10.0 } ?: 0.0 }

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

    private fun GibColor.flip() = when (this) {
        GibColor.Black -> GibColor.White
        GibColor.White -> GibColor.Black
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

    val gameTime by lazy {
        gameInfo["GTIME"]?.let { string ->
            val parts = string.split("-")
            parts.takeIf { it.size == 3 }?.let { (timeLimit, seconds, stones) ->
                GameTime(timeLimit.toInt(), seconds.toInt(), stones.toInt())
            }
        }
    }

    val gameDate by lazy {
        header["GAMEDATE"]?.let { string ->
            string.split("-").takeIf { it.size == 6 }?.let { parts ->
                OffsetDateTime.of(
                    parts[0].trim().toInt(),
                    parts[1].trim().toInt(),
                    parts[2].trim().toInt(),
                    parts[3].trim().toInt(),
                    parts[4].trim().toInt(),
                    parts[5].trim().toInt(),
                    0,
                    ZoneOffset.UTC
                )
            }
        }
    }

    companion object
}

data class GameTime(val timeLimit: Int, val overtimeSeconds: Int, val overtimeStones: Int)

sealed class GameProperty {
    data class STO(val moveNumber: Int, val color: GibColor, val x: Int, val y: Int) : GameProperty()
    data class INI(val handicap: Int) : GameProperty()
    data class SKI(val moveNumber: Int) : GameProperty()
}

sealed class Move {
    abstract val color: GibColor
    abstract val moveNumber: Int

    data class Point(
        override val color: GibColor,
        override val moveNumber: Int,
        val x: Int,
        val y: Int
    ) : Move()

    data class Pass(override val color: GibColor, override val moveNumber: Int) : Move()
}

sealed class GameResult {
    abstract val winner: GibColor
    data class Score(override val winner: GibColor, val score: Double) : GameResult()
    data class Resignation(override val winner: GibColor) : GameResult()
    data class Time(override val winner: GibColor) : GameResult()
}
