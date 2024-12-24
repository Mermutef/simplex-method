package ru.yarsu.config

import org.http4k.config.Environment
import java.io.File

data class AppConfig(
    val webConfig: WebConfig,
) {
    companion object {
        private val appEnv =
            Environment.from(File("app.properties")) overrides
                Environment.JVM_PROPERTIES overrides
                Environment.ENV overrides
                WebConfig.defaultEnv

        fun readConfiguration(): AppConfig = AppConfig(WebConfig.makeWebConfig(appEnv))
    }
}
