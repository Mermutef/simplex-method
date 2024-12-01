plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "ru.yarsu"
version = "1.0-SNAPSHOT"

application {
    mainClass = "$group.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}