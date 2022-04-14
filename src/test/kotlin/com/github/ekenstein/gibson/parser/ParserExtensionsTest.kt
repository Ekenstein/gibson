package com.github.ekenstein.gibson.parser

import com.github.ekenstein.gibson.Gib
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class ParserExtensionsTest {
    @ParameterizedTest
    @MethodSource("listResources")
    fun `parse real gib games from string`(resource: Path) {
        resource.toFile().inputStream().use {
            val string = String(it.readAllBytes())
            val x = assertDoesNotThrow { Gib.from(string) }
            println(x)
        }
    }

    @ParameterizedTest
    @MethodSource("listResources")
    fun `parse real gib games from path`(resource: Path) {
        assertDoesNotThrow { Gib.from(resource) }
    }

    @ParameterizedTest
    @MethodSource("listResources")
    fun `parse real gib games from input stream`(resource: Path) {
        resource.toFile().inputStream().use {
            assertDoesNotThrow { Gib.from(it) }
        }
    }

    companion object {
        @JvmStatic
        fun listResources(): List<Path> {
            val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
            val resourcesPath = Paths.get(projectDirAbsolutePath, "/src/test/resources/games")
            return Files.walk(resourcesPath)
                .filter { item -> Files.isRegularFile(item) }
                .filter { item -> item.toString().endsWith(".gib") }
                .toList()
        }
    }
}
