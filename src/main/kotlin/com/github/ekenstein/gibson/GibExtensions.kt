package com.github.ekenstein.gibson

private val Gib.gameInfo
    get() = header["GAMEINFOMAIN"]?.split(",").orEmpty().associate {
        val (name, value) = it.split(":")
        name to value
    }

fun Gib.getHandicap() = game.filterIsInstance<GameProperty.INI>().map { it.handicap }.singleOrNull() ?: 0

fun Gib.getKomi() = header["GAMEGONGJE"]?.toIntOrNull()?.let { it / 10.0 }

fun Gib.getGamePlace() = header["GAMEPLACE"]

fun Gib.getPlayerBlack() = header["GAMEBLACKNAME"]

fun Gib.getPlayerWhite() = header["GAMEWHITENAME"]

private val Gib.gameScore
    get() = header["GAMEZIPSU"]?.toIntOrNull()?.let { it / 10.0 } ?: 0.0

fun Gib.getGameResult() = when (gameInfo["GRLT"]?.toIntOrNull()) {
    0 -> GameResult.Score(GibColor.Black, gameScore)
    1 -> GameResult.Score(GibColor.White, gameScore)
    3 -> GameResult.Resignation(GibColor.Black)
    4 -> GameResult.Resignation(GibColor.White)
    7 -> GameResult.Time(GibColor.Black)
    8 -> GameResult.Time(GibColor.White)
    else -> null
}

private fun GibColor.flip() = when (this) {
    GibColor.Black -> GibColor.White
    GibColor.White -> GibColor.Black
}

fun Gib.getMoves(): List<Move> {
    val startingColor = if (handicap >= 2) GibColor.White else GibColor.Black
    fun colorFromMoveNumber(moveNumber: Int) = when (moveNumber % 2) {
        0 -> startingColor.flip()
        else -> startingColor
    }

    return game.mapNotNull {
        when (it) {
            is GameProperty.INI -> null
            is GameProperty.SKI -> Move.Pass(colorFromMoveNumber(it.moveNumber), it.moveNumber)
            is GameProperty.STO -> Move.Point(it.color, it.moveNumber, it.x, it.y)
        }
    }
}
