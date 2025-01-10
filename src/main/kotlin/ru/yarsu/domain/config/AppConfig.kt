package ru.yarsu.domain.config

import org.http4k.config.Environment
import java.io.File
import java.io.FileNotFoundException

data class AppConfig(
    val webConfig: WebConfig,
) {
    companion object {
        private val appEnv =
            try {
                Environment.from(File("app.properties")) overrides
                    Environment.JVM_PROPERTIES overrides
                    Environment.ENV overrides
                    WebConfig.defaultEnv
            } catch (_: FileNotFoundException) {
                Environment.JVM_PROPERTIES overrides
                    Environment.ENV overrides
                    WebConfig.defaultEnv
            }

        fun readConfiguration(): AppConfig = AppConfig(WebConfig.makeWebConfig(appEnv))
    }
}
