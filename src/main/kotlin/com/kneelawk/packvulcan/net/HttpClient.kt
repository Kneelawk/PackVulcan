package com.kneelawk.packvulcan.net

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

val HTTP_CLIENT = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

fun shutdownHttpClient() {
    HTTP_CLIENT.close()
}
