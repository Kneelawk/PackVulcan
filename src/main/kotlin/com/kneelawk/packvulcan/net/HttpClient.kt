package com.kneelawk.packvulcan.net

import com.kneelawk.packvulcan.GlobalConstants
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val HTTP_CLIENT = HttpClient(CIO) {
    install(UserAgent) {
        agent = GlobalConstants.REST_USER_AGENT
    }
    installJson()
}

fun HttpClientConfig<CIOEngineConfig>.installJson() {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = false
            useArrayPolymorphism = false

            // TODO: disable this periodically to check for new api keys
            ignoreUnknownKeys = true
        })
    }
}

fun shutdownHttpClient() {
    HTTP_CLIENT.close()
}
