package com.pdm0126.outfix.routes

import com.pdm0126.outfix.models.CreateGarmentRequest
import com.pdm0126.outfix.models.UpdateGarmentRequest
import com.pdm0126.outfix.services.GarmentService
import com.pdm0126.outfix.util.ApiResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun RoutingCall.userId(): UUID {
    val principal = principal<JWTPrincipal>()
    if (principal == null) {
        // Dummy UUID para pruebas sin autenticación
        return UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}

fun Route.garmentRoutes() {
    route("/garments") {

        // POST /garments — Crear prenda
        post {
            val userId = call.userId()
            val request = call.receive<CreateGarmentRequest>()
            val garment = GarmentService.create(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.ok(garment, "Prenda creada"))
        }

        // GET /garments — Listar prendas del usuario
        get {
            val userId = call.userId()
            val garments = GarmentService.findAll(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.ok(garments, "Prendas obtenidas"))
        }

        // GET /garments/{id} — Detalle de una prenda
        get("/{id}") {
            val userId = call.userId()
            val garmentId = UUID.fromString(call.parameters["id"])
            val garment = GarmentService.findOne(userId, garmentId)

            if (garment != null) {
                call.respond(HttpStatusCode.OK, ApiResponse.ok(garment))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Prenda no encontrada"))
            }
        }

        // PUT /garments/{id} — Actualizar prenda
        put("/{id}") {
            val userId = call.userId()
            val garmentId = UUID.fromString(call.parameters["id"])
            val request = call.receive<UpdateGarmentRequest>()
            val updated = GarmentService.update(userId, garmentId, request)

            if (updated) {
                val garment = GarmentService.findOne(userId, garmentId)
                call.respond(HttpStatusCode.OK, ApiResponse.ok(garment, "Prenda actualizada"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Prenda no encontrada"))
            }
        }

        // DELETE /garments/{id} — Eliminar prenda
        delete("/{id}") {
            val userId = call.userId()
            val garmentId = UUID.fromString(call.parameters["id"])
            val deleted = GarmentService.delete(userId, garmentId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.ok("Prenda eliminada"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Prenda no encontrada"))
            }
        }
    }
}
