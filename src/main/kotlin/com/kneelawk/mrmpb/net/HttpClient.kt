package com.kneelawk.mrmpb.net

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

val HTTP_CLIENT = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}

fun shutdownHttpClient() {
    HTTP_CLIENT.close()
}
