import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "de.rubixdev"
version = "1.0"

repositories {
    mavenCentral()
    //maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // Kord
    implementation("dev.kord:kord-core:0.7.3")

    // Logging
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.14.1"))
    implementation("org.apache.logging.log4j", "log4j-core")
    implementation("org.apache.logging.log4j", "log4j-api")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl")

    // KotlinX
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}

application {
    mainClassName = "de.rubixdev.mastermind.MainKt"
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Main-Class" to "de.rubixdev.mastermind.MainKt",
            "Multi-Release" to "true"
        ))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}