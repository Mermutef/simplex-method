import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val http4kVersion: String by project
val http4kConnectVersion: String by project
val junitVersion: String by project
val kotlinVersion: String by project
val ktlintVersion: String by project
val kotestVersion: String by project
val h2dbVersion: String by project
val flywayVersion: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version ("+")
    id("com.gradleup.shadow") version ("+")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.konform:konform-jvm:+")
    implementation("dev.forkhandles:result4k:+")
    implementation("org.http4k:http4k-client-okhttp:+")
    implementation("org.http4k:http4k-cloudnative:+")
    implementation("org.http4k:http4k-core:+")
    implementation("org.http4k:http4k-format-jackson:+")
    implementation("org.http4k:http4k-multipart:+")
    implementation("org.http4k:http4k-server-netty:+")
    implementation("org.http4k:http4k-template-pebble:+")
    implementation("org.http4k:http4k-cloudnative:+")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:+")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:+")
    testImplementation("org.http4k:http4k-testing-approval:+")
    testImplementation("org.http4k:http4k-testing-hamkrest:+")
    testImplementation("org.http4k:http4k-testing-kotest:+")
    testImplementation("org.junit.jupiter:junit-jupiter-api:+")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:+")
    testImplementation("io.kotest:kotest-runner-junit5:+")
    testImplementation("io.kotest:kotest-assertions-core:+")
}

dependencyLocking {
    lockAllConfigurations()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "ru.yarsu.MainKt"
}

ktlint {
    version.set(ktlintVersion)
    filter {
        exclude("**/generated/**")
    }
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all")
            jvmTarget = JvmTarget.JVM_21
        }
    }

    withType<JavaExec> {
        standardInput = System.`in`
    }

    java {
        sourceCompatibility = VERSION_21
        targetCompatibility = VERSION_21
    }

    test {
        useJUnitPlatform()
    }
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "Main.kt"
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            archiveBaseName = "simplex-server"
        }
        configurations = listOf(project.configurations["compileClasspath"])
    }
}
