package com.pdm0126.outfix.routes

import com.pdm0126.outfix.models.LoginRequest
import com.pdm0126.outfix.models.RegisterRequest
import com.pdm0126.outfix.services.AuthService
import com.pdm0126.outfix.util.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {

        // POST /auth/register
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                if (request.email.isBlank() || request.password.length < 8) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error("Email requerido y contraseña mínimo 8 caracteres")
                    )
                    return@post
                }

                val result = AuthService.register(request)
                call.respond(HttpStatusCode.Created, ApiResponse.ok(result, "Usuario registrado"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, ApiResponse.error(e.message ?: "Error al registrar"))
            }
        }

        // POST /auth/login
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = AuthService.login(request)
                call.respond(HttpStatusCode.OK, ApiResponse.ok(result, "Login exitoso"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse.error(e.message ?: "Credenciales incorrectas"))
            }
        }
    }
}
