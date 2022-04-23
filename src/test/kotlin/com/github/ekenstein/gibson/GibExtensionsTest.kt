package com.github.ekenstein.gibson

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GibExtensionsTest {
    @Test
    fun `passes has the correct color assigned`() {
        assertAll(
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.SKI(1)))
                val expected = listOf(Move.Pass(GibColor.Black, 1))
                val actual = gib.moves
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.INI(2), GameProperty.SKI(1)))
                val expected = listOf(Move.Pass(GibColor.White, 1))
                val actual = gib.moves
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.SKI(1), GameProperty.SKI(2)))
                val expected = listOf(Move.Pass(GibColor.Black, 1), Move.Pass(GibColor.White, 2))
                val actual = gib.moves
                assertEquals(expected, actual)
            }
        )
    }

    @Test
    fun `GAMEWHITENAME is mapped to white player name`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEWHITENAME" to "Test"), emptyList())
                val actual = gib.playerWhite
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.playerWhite
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEBLACKNAME is mapped to black player name`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEBLACKNAME" to "Test"), emptyList())
                val actual = gib.playerBlack
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.playerBlack
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEPLACE is mapped to game place`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEPLACE" to "Test"), emptyList())
                val actual = gib.gamePlace
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.gamePlace
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEGONGJE is mapped to komi`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEGONGJE" to "65"), emptyList())
                val actual = gib.komi
                assertEquals(6.5, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.komi
                assertNull(actual)
            }
        )
    }

    @Test
    fun `INI contains handicap`() {
        assertAll(
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.INI(8)))
                val actual = gib.handicap
                assertEquals(8, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.handicap
                assertEquals(0, actual)
            }
        )
    }

    @Test
    fun `GAMEDATE contains the date of the game`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEDATE" to "2020- 8- 4-11- 7-47"), emptyList())
                val actual = gib.gameDate
                val expected = LocalDateTime.of(
                    LocalDate.of(2020, 8, 4),
                    LocalTime.of(11, 7, 47)
                )
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(mapOf("GAMEDATE" to "2020- 8-21-22-55- 3"), emptyList())
                val expected = LocalDateTime.of(
                    LocalDate.of(2020, 8, 21),
                    LocalTime.of(22, 55, 3)
                )
                assertEquals(expected, gib.gameDate)
            }
        )
    }

    @Test
    fun `GTIME contains the time limit of the game`() {
        val gib = Gib(mapOf("GAMEINFOMAIN" to "GTIME:600-20-3"), emptyList())
        val actual = gib.timeSettings
        val expected = TimeSettings(600, 20, 3)
        assertEquals(expected, actual)
    }

    @Test
    fun `GRLT in GAMEINFOMAIN contains information about game result`() {
        assertAll(
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:0",
                        "GAMEZIPSU" to "245"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Score(GibColor.Black, 24.5)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:0"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Score(GibColor.Black, 0.0)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:1",
                        "GAMEZIPSU" to "245"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Score(GibColor.White, 24.5)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:1"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Score(GibColor.White, 0.0)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:3"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Resignation(GibColor.Black)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:4"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Resignation(GibColor.White)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:7"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Time(GibColor.Black)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(
                    mapOf(
                        "GAMEINFOMAIN" to "GRLT:8"
                    ),
                    emptyList()
                )
                val actual = gib.gameResult
                val expected = GameResult.Time(GibColor.White)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.gameResult
                assertNull(actual)
            }
        )
    }
}
