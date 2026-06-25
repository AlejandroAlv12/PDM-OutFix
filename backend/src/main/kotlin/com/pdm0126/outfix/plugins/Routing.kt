package com.pdm0126.outfix.plugins

import com.pdm0126.outfix.routes.authRoutes
import com.pdm0126.outfix.routes.garmentRoutes
import com.pdm0126.outfix.routes.plannerRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

// Índice central de rutas.
// Cada compañero trabaja en su archivo de rutas (AuthRoutes.kt, etc.)
// sin tocar este archivo. Solo se agrega la llamada aquí una vez.
fun Application.configureRouting() {
    routing {
        // Rutas públicas (no necesitan JWT)
        authRoutes()

        // Rutas protegidas (necesitan token JWT en el header Authorization)
        authenticate("auth-jwt") {
            garmentRoutes()
            plannerRoutes()
        }
    }
}
