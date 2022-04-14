package com.github.ekenstein.gibson

enum class GibColor { Black, White }

data class Gib(val header: Map<String, String>, val game: List<GameProperty>) {
    private val gameInfo by lazy {
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

    companion object
}

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
