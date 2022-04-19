# gibson
A simple, easy-to-use parser for GIB in Kotlin.

GIB is the file format the Tygem Go Server produces instead of a plain SGF.

### Usage
### 1. Add a dependency on gibson
For `build.gradle.kts`:
````kotlin
repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.Ekenstein:gibson:0.1.2")
}
````
### 2. Obtain a GIB file from the Tygem Go Server
For examples, see the test [resources](https://github.com/Ekenstein/gibson/tree/main/src/test/resources/games)
### 3. Write some code
```kotlin
import com.github.ekenstein.gibson.Gib
import com.github.ekenstein.gibson.Move
import com.github.ekenstein.gibson.parser.from
import java.nio.file.Path

fun main() {
    val file = Path.of("some_messy_name.gib..gib")

    // parse the file
    val gib = Gib.from(file)

    // get some information about the file such as the game place
    val place = gib.gamePlace

    // ... or komi
    val komi = gib.komi

    // ... or the handicap used for the game
    val handicap = gib.handicap

    // ... or the name of the players
    val playerBlack = gib.playerBlack
    val playerWhite = gib.playerWhite

    // ... or the game result.
    val gameResult = gib.gameResult

    // ... or maybe traverse the game tree
    gib.moves.map {
        when (it) {
            is Move.Pass -> { /* handle a pass */ }
            is Move.Point -> { /* handle a stone */}
        }
    }
}
```