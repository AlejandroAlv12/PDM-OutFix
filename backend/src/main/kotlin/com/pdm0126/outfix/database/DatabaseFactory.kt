package com.pdm0126.outfix.database

import com.pdm0126.outfix.database.tables.*

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        // Conexión directa a SQLite en archivo local
        Database.connect(
            url = "jdbc:sqlite:outfix.db",
            driver = "org.sqlite.JDBC"
        )

        // Crear las tablas automáticamente si no existen
        transaction {
            SchemaUtils.create(
                Users,
                Garments,
                WeeklyPlans,
                DailyOutfits,
                DailyOutfitGarments
            )
        }
    }
}
