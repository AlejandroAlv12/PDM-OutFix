package com.pdm0126.outfix.services

import com.pdm0126.outfix.database.tables.DailyOutfitGarments
import com.pdm0126.outfix.database.tables.DailyOutfits
import com.pdm0126.outfix.database.tables.Garments
import io.ktor.server.application.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.LocalDate

// Tarea recurrente que marca las prendas del outfit de hoy como "DIRTY"
// al finalizar el día. Usa coroutines estándar de Kotlin, sin librerías extra.
object LaundryScheduler {
    private val logger = LoggerFactory.getLogger("LaundryScheduler")

    fun start(application: Application) {
        // Lanza una coroutine que se ejecuta mientras el servidor esté vivo
        application.launch {
            while (isActive) {
                // Esperar hasta la medianoche (o cada 24 horas en la práctica)
                val now = java.time.LocalTime.now()
                val midnight = java.time.LocalTime.of(23, 59)
                val delayMs = if (now.isBefore(midnight)) {
                    java.time.Duration.between(now, midnight).toMillis()
                } else {
                    // Si ya pasó la hora, esperar hasta mañana
                    java.time.Duration.between(now, midnight).toMillis() + 24 * 60 * 60 * 1000
                }

                logger.info("Próxima ejecución de canasta de ropa sucia en ${delayMs / 1000 / 60} minutos")
                delay(delayMs)

                // Ejecutar la lógica de marcar prendas como sucias
                markTodayGarmentsDirty()
            }
        }
    }

    // Busca los outfits del día actual y marca sus prendas como DIRTY
    private fun markTodayGarmentsDirty() {
        try {
            val todayName = LocalDate.now().dayOfWeek.toOutfitDay()
            logger.info("Ejecutando canasta de ropa sucia para: $todayName")

            transaction {
                // Encontrar todas las prendas asignadas a outfits de hoy
                val garmentIds = (DailyOutfits innerJoin DailyOutfitGarments)
                    .select(DailyOutfitGarments.garmentId)
                    .where { DailyOutfits.dayOfWeek eq todayName }
                    .map { it[DailyOutfitGarments.garmentId] }

                if (garmentIds.isEmpty()) {
                    logger.info("No hay prendas asignadas para hoy.")
                    return@transaction
                }

                // Marcar como DIRTY solo las que estén AVAILABLE
                val updated = Garments.update({
                    (Garments.id inList garmentIds) and (Garments.status eq "AVAILABLE")
                }) {
                    it[status] = "DIRTY"
                }

                logger.info("$updated prendas movidas a canasta de ropa sucia.")
            }
        } catch (e: Exception) {
            logger.error("Error en canasta de ropa sucia: ${e.message}")
        }
    }

    // Convierte DayOfWeek de Java a nuestro formato de string
    private fun DayOfWeek.toOutfitDay(): String = when (this) {
        DayOfWeek.MONDAY -> "MONDAY"
        DayOfWeek.TUESDAY -> "TUESDAY"
        DayOfWeek.WEDNESDAY -> "WEDNESDAY"
        DayOfWeek.THURSDAY -> "THURSDAY"
        DayOfWeek.FRIDAY -> "FRIDAY"
        DayOfWeek.SATURDAY -> "SATURDAY"
        DayOfWeek.SUNDAY -> "SUNDAY"
    }
}
