import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "de.rubixdev"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // Kord
    implementation("dev.kord:kord-core:0.7.0-RC2")
    implementation("dev.kord.x:emoji:0.5.0-SNAPSHOT")

    // Logging
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.14.1"))
    implementation("org.apache.logging.log4j", "log4j-core")
    implementation("org.apache.logging.log4j", "log4j-api")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl")

    // KotlinX
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
}

application {
    mainClassName = "MainKt"
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "MainKt"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}