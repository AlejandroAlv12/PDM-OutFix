package com.pdm0126.outfix.routes

import com.pdm0126.outfix.models.AssignGarmentSlotRequest
import com.pdm0126.outfix.models.CreateDailyOutfitRequest
import com.pdm0126.outfix.models.CreateWeeklyPlanRequest
import com.pdm0126.outfix.services.PlannerService
import com.pdm0126.outfix.util.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.plannerRoutes() {
    route("/planner") {

        // --- Planes Semanales ---

        // POST /planner/weeks — Crear plan semanal
        post("/weeks") {
            val userId = call.userId()
            val request = call.receive<CreateWeeklyPlanRequest>()
            val plan = PlannerService.createWeeklyPlan(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.ok(plan, "Plan semanal creado"))
        }

        // GET /planner/weeks — Listar planes del usuario
        get("/weeks") {
            val userId = call.userId()
            val plans = PlannerService.findAllWeeklyPlans(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.ok(plans, "Planes obtenidos"))
        }

        // GET /planner/weeks/{id} — Ver plan con outfits y prendas
        get("/weeks/{id}") {
            val userId = call.userId()
            val planId = UUID.fromString(call.parameters["id"])
            val plan = PlannerService.findOneWeeklyPlan(userId, planId)

            if (plan != null) {
                call.respond(HttpStatusCode.OK, ApiResponse.ok(plan))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Plan no encontrado"))
            }
        }

        // --- Outfits Diarios ---

        // POST /planner/outfits — Crear outfit para un día
        post("/outfits") {
            val request = call.receive<CreateDailyOutfitRequest>()
            val outfit = PlannerService.createDailyOutfit(request)
            call.respond(HttpStatusCode.Created, ApiResponse.ok(outfit, "Outfit creado"))
        }

        // DELETE /planner/outfits/{id} — Eliminar outfit
        delete("/outfits/{id}") {
            val outfitId = UUID.fromString(call.parameters["id"])
            val deleted = PlannerService.deleteDailyOutfit(outfitId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.ok("Outfit eliminado"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Outfit no encontrado"))
            }
        }

        // --- Asignación de Prendas ---

        // POST /planner/outfits/{id}/garments — Asignar prenda a slot
        post("/outfits/{id}/garments") {
            val outfitId = UUID.fromString(call.parameters["id"])
            val request = call.receive<AssignGarmentSlotRequest>()
            val assignment = PlannerService.assignGarmentToSlot(outfitId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.ok(assignment, "Prenda asignada"))
        }

        // DELETE /planner/outfit-garments/{id} — Quitar prenda de slot
        delete("/outfit-garments/{id}") {
            val assignmentId = UUID.fromString(call.parameters["id"])
            val deleted = PlannerService.removeGarmentFromSlot(assignmentId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.ok("Prenda desasignada"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error("Asignación no encontrada"))
            }
        }
    }
}
