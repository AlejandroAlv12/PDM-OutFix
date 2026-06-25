package com.pdm0126.outfix.database

import com.pdm0126.outfix.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val dataSource = hikari()
        Database.connect(dataSource)

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

    // Pool de conexiones con HikariCP
    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/outfix_db"
            driverClassName = "org.postgresql.Driver"
            username = "outfix_user"
            password = "outfix_pass_2026"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.validate()
        return HikariDataSource(config)
    }
}
