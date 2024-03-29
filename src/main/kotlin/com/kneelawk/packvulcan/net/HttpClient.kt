package com.kneelawk.packvulcan.net

import com.kneelawk.packvulcan.GlobalConstants
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val HTTP_CLIENT = HttpClient(CIO) {
    install(UserAgent) {
        agent = GlobalConstants.REST_USER_AGENT
    }
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
