import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  kotlin("jvm") version "1.3.50"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
  id("org.jlleitschuh.gradle.ktlint") version "7.1.0"
  id("com.github.johnrengelman.shadow") version "4.0.4"
}
repositories {
  jcenter()
  mavenCentral()
  maven("https://dl.bintray.com/hexlabsio/kloudformation")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.13.0")
  implementation(group = "com.amazonaws", name = "aws-java-sdk-lambda", version = "1.11.677")
  implementation(group = "io.symphonia", name = "lambda-logging", version = "1.0.0")
  http4k("core")
  http4k("client-okhttp")
  http4k("serverless-lambda")
  http4k("server-ktorcio")
  http4k("format-jackson")
  testing()
  kloudformation()

}

fun DependencyHandlerScope.testing() {
  testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.21")
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "1.3.21")
  testRuntime(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
}

fun DependencyHandlerScope.logging() {
}

fun DependencyHandlerScope.http4k(artifact: String) {
  implementation(group = "org.http4k", name = "http4k-$artifact", version = "3.189.0")
}

fun DependencyHandlerScope.kloudformation() {
  testImplementation("io.kloudformation:kloudformation:1.1.76")
  testImplementation("io.hexlabs:kloudformation-serverless-module:1.1.2")
}

val shadowJar by tasks.getting(ShadowJar::class) {
  archiveClassifier.set("uber")
  manifest {
    attributes(mapOf("Main-Class" to "io.hexlabs.vehicle.inventory.RootHandlerKt"))
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

artifacts {
  add("archives", shadowJar)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

configure<KtlintExtension> {
  outputToConsole.set(true)
  coloredOutput.set(true)
  reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

ktlint {
  verbose.set(true)
  outputToConsole.set(true)
  coloredOutput.set(true)
  reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

sourceSets {
  main {
    java {
      srcDirs("src/main/kotlin")
    }
  }
  test {
    java {
      srcDirs("src/test/kotlin", "stack")
    }
  }
}
