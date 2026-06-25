package com.pdm0126.outfix.services

import com.pdm0126.outfix.database.tables.Garments
import com.pdm0126.outfix.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

object GarmentService {

    // Crear prenda nueva
    fun create(userId: UUID, request: CreateGarmentRequest): GarmentResponse {
        val now = Instant.now()
        val garmentId = transaction {
            Garments.insert {
                it[Garments.userId] = userId
                it[name] = request.name
                it[category] = request.category
                it[colorHex] = request.colorHex
                it[colorName] = request.colorName
                it[style] = request.style
                it[brand] = request.brand
                it[size] = request.size
                it[status] = request.status
                it[imageUrl] = request.imageUrl
                it[notes] = request.notes
                it[createdAt] = now
                it[updatedAt] = now
            } get Garments.id
        }

        return GarmentResponse(
            id = garmentId.toString(),
            userId = userId.toString(),
            name = request.name,
            category = request.category,
            colorHex = request.colorHex,
            colorName = request.colorName,
            style = request.style,
            brand = request.brand,
            size = request.size,
            status = request.status,
            imageUrl = request.imageUrl,
            notes = request.notes,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
    }

    // Listar todas las prendas de un usuario
    fun findAll(userId: UUID): List<GarmentResponse> = transaction {
        Garments.selectAll()
            .where { Garments.userId eq userId }
            .orderBy(Garments.createdAt, SortOrder.DESC)
            .map { it.toGarmentResponse() }
    }

    // Buscar prenda por ID (verificando que sea del usuario)
    fun findOne(userId: UUID, garmentId: UUID): GarmentResponse? = transaction {
        Garments.selectAll()
            .where { (Garments.id eq garmentId) and (Garments.userId eq userId) }
            .singleOrNull()
            ?.toGarmentResponse()
    }

    // Actualizar prenda
    fun update(userId: UUID, garmentId: UUID, request: UpdateGarmentRequest): Boolean {
        val updated = transaction {
            Garments.update({ (Garments.id eq garmentId) and (Garments.userId eq userId) }) {
                request.name?.let { value -> it[name] = value }
                request.category?.let { value -> it[category] = value }
                request.colorHex?.let { value -> it[colorHex] = value }
                request.colorName?.let { value -> it[colorName] = value }
                request.style?.let { value -> it[style] = value }
                request.brand?.let { value -> it[brand] = value }
                request.size?.let { value -> it[size] = value }
                request.status?.let { value -> it[status] = value }
                request.imageUrl?.let { value -> it[imageUrl] = value }
                request.notes?.let { value -> it[notes] = value }
                it[updatedAt] = Instant.now()
            }
        }
        return updated > 0
    }

    // Eliminar prenda
    fun delete(userId: UUID, garmentId: UUID): Boolean {
        val deleted = transaction {
            Garments.deleteWhere { (Garments.id eq garmentId) and (Garments.userId eq userId) }
        }
        return deleted > 0
    }

    // Extensión para convertir un ResultRow a GarmentResponse
    private fun ResultRow.toGarmentResponse() = GarmentResponse(
        id = this[Garments.id].toString(),
        userId = this[Garments.userId].toString(),
        name = this[Garments.name],
        category = this[Garments.category],
        colorHex = this[Garments.colorHex],
        colorName = this[Garments.colorName],
        style = this[Garments.style],
        brand = this[Garments.brand],
        size = this[Garments.size],
        status = this[Garments.status],
        imageUrl = this[Garments.imageUrl],
        notes = this[Garments.notes],
        createdAt = this[Garments.createdAt].toString(),
        updatedAt = this[Garments.updatedAt].toString()
    )
}
