package com.pdm0126.outfix.plugins

import com.pdm0126.outfix.routes.authRoutes
import com.pdm0126.outfix.routes.garmentRoutes
import com.pdm0126.outfix.routes.plannerRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

// Índice central de rutas.
fun Application.configureRouting() {
    routing {
        // Rutas públicas (no necesitan JWT)
        authRoutes()
        garmentRoutes()

        // Rutas protegidas (necesitan token JWT en el header Authorization)
        authenticate("auth-jwt") {
            plannerRoutes()
        }
    }
}
