package com.github.ekenstein.gibson

enum class GibColor { Black, White }

data class Gib(val header: Map<String, String>, val game: List<GameProperty>) {
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
