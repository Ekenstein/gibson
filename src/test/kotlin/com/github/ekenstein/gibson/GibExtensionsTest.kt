package com.github.ekenstein.gibson

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GibExtensionsTest {
    @Test
    fun `passes has the correct color assigned`() {
        assertAll(
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.SKI(1)))
                val expected = listOf(Move.Pass(GibColor.Black, 1))
                val actual = gib.getMoves()
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.INI(2), GameProperty.SKI(1)))
                val expected = listOf(Move.Pass(GibColor.White, 1))
                val actual = gib.getMoves()
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.SKI(1), GameProperty.SKI(2)))
                val expected = listOf(Move.Pass(GibColor.Black, 1), Move.Pass(GibColor.White, 2))
                val actual = gib.getMoves()
                assertEquals(expected, actual)
            }
        )
    }

    @Test
    fun `GAMEWHITENAME is mapped to white player name`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEWHITENAME" to "Test"), emptyList())
                val actual = gib.getPlayerWhite()
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getPlayerWhite()
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEBLACKNAME is mapped to black player name`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEBLACKNAME" to "Test"), emptyList())
                val actual = gib.getPlayerBlack()
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getPlayerBlack()
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEPLACE is mapped to game place`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEPLACE" to "Test"), emptyList())
                val actual = gib.getGamePlace()
                assertEquals("Test", actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getGamePlace()
                assertNull(actual)
            }
        )
    }

    @Test
    fun `GAMEGONGJE is mapped to komi`() {
        assertAll(
            {
                val gib = Gib(mapOf("GAMEGONGJE" to "65"), emptyList())
                val actual = gib.getKomi()
                assertEquals(6.5, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getKomi()
                assertNull(actual)
            }
        )
    }

    @Test
    fun `INI contains handicap`() {
        assertAll(
            {
                val gib = Gib(emptyMap(), listOf(GameProperty.INI(8)))
                val actual = gib.getHandicap()
                assertEquals(8, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getHandicap()
                assertEquals(0, actual)
            }
        )
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
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
                val actual = gib.getGameResult()
                val expected = GameResult.Time(GibColor.White)
                assertEquals(expected, actual)
            },
            {
                val gib = Gib(emptyMap(), emptyList())
                val actual = gib.getGameResult()
                assertNull(actual)
            }
        )
    }
}
