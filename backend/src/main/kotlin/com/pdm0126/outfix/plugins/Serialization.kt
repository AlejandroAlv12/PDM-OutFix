package com.pdm0126.outfix.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true        // JSON legible en desarrollo
            isLenient = false
            ignoreUnknownKeys = true  // No truena si el cliente manda campos extra
        })
    }
}
