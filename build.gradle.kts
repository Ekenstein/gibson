import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream

group = "com.github.ekenstein"
version = "0.1.4"
val kotlinJvmTarget = "1.8"

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("org.jetbrains.dokka") version "1.6.20"
    antlr
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    antlr("org.antlr", "antlr4", "4.10.1")

    testImplementation(kotlin("test"))

    val junitVersion = "5.8.2"
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications {
        create<MavenPublication>("gibson") {
            groupId = project.group.toString()
            artifactId = "gibson"
            version = project.version.toString()
            from(components["kotlin"])
            artifact(javadocJar)
            artifact(tasks.kotlinSourcesJar)

            pom {
                name.set("gibson")
                description.set("Simple GIB parser")
                url.set("https://github.com/Ekenstein/gibson")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/Ekenstein/gibson/blob/main/LICENSE")
                    }
                }
            }
        }
    }
}

tasks {
    dependencyUpdates {
        rejectVersionIf(UpgradeToUnstableFilter())
    }

    val dependencyUpdateSentinel = register<DependencyUpdateSentinel>("dependencyUpdateSentinel", buildDir)
    dependencyUpdateSentinel.configure {
        dependsOn(dependencyUpdates)
    }

    withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }

    generateGrammarSource {
        outputDirectory = Paths
            .get("build", "generated-src", "antlr", "main", "com", "github", "ekenstein", "gibson", "parser")
            .toFile()
        mustRunAfter("runKtlintCheckOverMainSourceSet")
        mustRunAfter("dokkaHtml")
    }

    generateTestGrammarSource {
        mustRunAfter("runKtlintCheckOverTestSourceSet")
    }

    compileKotlin {
        dependsOn(generateGrammarSource)
        kotlinOptions {
            jvmTarget = kotlinJvmTarget
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }

    compileTestKotlin {
        dependsOn(generateTestGrammarSource)
        kotlinOptions {
            jvmTarget = kotlinJvmTarget
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }

    compileJava {
        sourceCompatibility = kotlinJvmTarget
        targetCompatibility = kotlinJvmTarget
    }

    compileTestJava {
        sourceCompatibility = kotlinJvmTarget
        targetCompatibility = kotlinJvmTarget
    }

    check {
        dependsOn(test)
        dependsOn(ktlintCheck)
        dependsOn(dependencyUpdateSentinel)
    }

    kotlinSourcesJar {
        dependsOn("generateGrammarSource")
    }

    build {
        dependsOn("javadocJar")
    }

    test {
        useJUnitPlatform()
    }
}

ktlint {
    version.set("0.45.2")
}

class UpgradeToUnstableFilter : com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentFilter {
    override fun reject(cs: ComponentSelectionWithCurrent) = reject(cs.currentVersion, cs.candidate.version)

    private fun reject(old: String, new: String): Boolean {
        return !isStable(new) && isStable(old) // no unstable proposals for stable dependencies
    }

    private fun isStable(version: String): Boolean {
        val stableKeyword = setOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val stablePattern = version.matches(Regex("""^[0-9,.v-]+(-r)?$"""))
        return stableKeyword || stablePattern
    }
}

abstract class DependencyUpdateSentinel @Inject constructor(private val buildDir: File) : DefaultTask() {
    @ExperimentalPathApi
    @org.gradle.api.tasks.TaskAction
    fun check() {
        val updateIndicator = "The following dependencies have later milestone versions:"
        val report = Paths.get(buildDir.toString(), "dependencyUpdates", "report.txt")

        report.inputStream().bufferedReader().use { reader ->
            if (reader.lines().anyMatch { it == updateIndicator }) {
                throw GradleException("Dependency updates are available.")
            }
        }
    }
}
