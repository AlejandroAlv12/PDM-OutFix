package com.pdm0126.outfix

import com.pdm0126.outfix.database.DatabaseFactory
import com.pdm0126.outfix.plugins.configureRouting
import com.pdm0126.outfix.plugins.configureSecurity
import com.pdm0126.outfix.plugins.configureSerialization
import com.pdm0126.outfix.services.LaundryScheduler
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    // Plugins de Ktor
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(
                    "success" to false,
                    "message" to (cause.message ?: "Error interno del servidor"),
                    "data" to null
                )
            )
        }
    }

    configureSerialization()
    configureSecurity()
    configureRouting()

    // Iniciar la tarea recurrente de canasta de ropa sucia
    LaundryScheduler.start(this)
}
