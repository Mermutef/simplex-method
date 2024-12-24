package ru.yarsu.config

import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int

data class WebConfig(
    val webPort: Int,
    val hotReload: Boolean,
) {
    companion object {
        val portLens = EnvironmentKey.int().required("web.port")
        val hotReloadLens = EnvironmentKey.boolean().required("web.hotReloadTemplates")
        val defaultEnv = Environment.defaults(portLens of 9000, hotReloadLens of false)

        fun makeWebConfig(env: Environment): WebConfig = WebConfig(portLens(env), hotReloadLens(env))
    }
}
