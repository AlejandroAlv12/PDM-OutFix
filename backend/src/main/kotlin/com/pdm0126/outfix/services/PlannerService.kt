package com.pdm0126.outfix.services

import com.pdm0126.outfix.database.tables.DailyOutfitGarments
import com.pdm0126.outfix.database.tables.DailyOutfits
import com.pdm0126.outfix.database.tables.Garments
import com.pdm0126.outfix.database.tables.WeeklyPlans
import com.pdm0126.outfix.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object PlannerService {

    // --- Planes Semanales ---

    fun createWeeklyPlan(userId: UUID, request: CreateWeeklyPlanRequest): WeeklyPlanResponse {
        val planId = transaction {
            WeeklyPlans.insert {
                it[WeeklyPlans.userId] = userId
                it[WeeklyPlans.weekStartDate] = request.weekStartDate
                it[WeeklyPlans.label] = request.label
            } get WeeklyPlans.id
        }

        return WeeklyPlanResponse(
            id = planId.toString(),
            userId = userId.toString(),
            weekStartDate = request.weekStartDate,
            label = request.label
        )
    }

    fun findAllWeeklyPlans(userId: UUID): List<WeeklyPlanResponse> = transaction {
        WeeklyPlans.selectAll()
            .where { WeeklyPlans.userId eq userId }
            .orderBy(WeeklyPlans.createdAt, SortOrder.DESC)
            .map { row ->
                WeeklyPlanResponse(
                    id = row[WeeklyPlans.id].toString(),
                    userId = row[WeeklyPlans.userId].toString(),
                    weekStartDate = row[WeeklyPlans.weekStartDate],
                    label = row[WeeklyPlans.label]
                )
            }
    }

    // Obtener un plan con todos sus outfits y prendas asignadas
    fun findOneWeeklyPlan(userId: UUID, planId: UUID): WeeklyPlanResponse? = transaction {
        val planRow = WeeklyPlans.selectAll()
            .where { (WeeklyPlans.id eq planId) and (WeeklyPlans.userId eq userId) }
            .singleOrNull() ?: return@transaction null

        // Buscar los outfits de este plan
        val outfitRows = DailyOutfits.selectAll()
            .where { DailyOutfits.weeklyPlanId eq planId }
            .toList()

        val outfits = outfitRows.map { outfitRow ->
            val outfitUUID = outfitRow[DailyOutfits.id].value

            // Buscar las asignaciones de prendas de este outfit
            val assignmentRows = DailyOutfitGarments.selectAll()
                .where { DailyOutfitGarments.dailyOutfitId eq outfitUUID }
                .toList()

            // Para cada asignación, buscar la prenda correspondiente
            val garmentAssignments = assignmentRows.map { assignRow ->
                val gId = assignRow[DailyOutfitGarments.garmentId].value
                val garmentRow = Garments.selectAll()
                    .where { Garments.id eq gId }
                    .singleOrNull()

                OutfitGarmentResponse(
                    id = assignRow[DailyOutfitGarments.id].toString(),
                    garmentId = assignRow[DailyOutfitGarments.garmentId].toString(),
                    slotType = assignRow[DailyOutfitGarments.slotType],
                    garment = garmentRow?.let { gr ->
                        GarmentResponse(
                            id = gr[Garments.id].toString(),
                            userId = gr[Garments.userId].toString(),
                            name = gr[Garments.name],
                            category = gr[Garments.category],
                            colorHex = gr[Garments.colorHex],
                            colorName = gr[Garments.colorName],
                            status = gr[Garments.status],
                            imageUrl = gr[Garments.imageUrl],
                            notes = gr[Garments.notes],
                            createdAt = gr[Garments.createdAt].toString(),
                            updatedAt = gr[Garments.updatedAt].toString()
                        )
                    }
                )
            }

            DailyOutfitResponse(
                id = outfitRow[DailyOutfits.id].toString(),
                weeklyPlanId = outfitRow[DailyOutfits.weeklyPlanId].toString(),
                dayOfWeek = outfitRow[DailyOutfits.dayOfWeek],
                occasion = outfitRow[DailyOutfits.occasion],
                garments = garmentAssignments
            )
        }

        WeeklyPlanResponse(
            id = planRow[WeeklyPlans.id].toString(),
            userId = planRow[WeeklyPlans.userId].toString(),
            weekStartDate = planRow[WeeklyPlans.weekStartDate],
            label = planRow[WeeklyPlans.label],
            dailyOutfits = outfits
        )
    }

    // --- Outfits Diarios ---

    fun createDailyOutfit(request: CreateDailyOutfitRequest): DailyOutfitResponse {
        val outfitId = transaction {
            DailyOutfits.insert {
                it[DailyOutfits.weeklyPlanId] = UUID.fromString(request.weeklyPlanId)
                it[DailyOutfits.dayOfWeek] = request.dayOfWeek
                it[DailyOutfits.occasion] = request.occasion
            } get DailyOutfits.id
        }

        return DailyOutfitResponse(
            id = outfitId.toString(),
            weeklyPlanId = request.weeklyPlanId,
            dayOfWeek = request.dayOfWeek,
            occasion = request.occasion
        )
    }

    fun deleteDailyOutfit(outfitId: UUID): Boolean {
        val deleted = transaction {
            DailyOutfits.deleteWhere { DailyOutfits.id eq outfitId }
        }
        return deleted > 0
    }

    // --- Asignación de Prendas a Slots ---

    fun assignGarmentToSlot(outfitId: UUID, request: AssignGarmentSlotRequest): OutfitGarmentResponse {
        val assignmentId = transaction {
            DailyOutfitGarments.insert {
                it[DailyOutfitGarments.dailyOutfitId] = outfitId
                it[DailyOutfitGarments.garmentId] = UUID.fromString(request.garmentId)
                it[DailyOutfitGarments.slotType] = request.slotType
            } get DailyOutfitGarments.id
        }

        return OutfitGarmentResponse(
            id = assignmentId.toString(),
            garmentId = request.garmentId,
            slotType = request.slotType
        )
    }

    fun removeGarmentFromSlot(assignmentId: UUID): Boolean {
        val deleted = transaction {
            DailyOutfitGarments.deleteWhere { DailyOutfitGarments.id eq assignmentId }
        }
        return deleted > 0
    }
}
