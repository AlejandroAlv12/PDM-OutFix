package com.pdm0126.outfix.data.repository

import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.api.dto.CreateGarmentRequest
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.local.GarmentEntity
import com.pdm0126.outfix.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class GarmentRepository(
    private val garmentDao: GarmentDao
) {
    val garmentsFlow: Flow<List<GarmentResponse>> = garmentDao.getAllGarmentsFlow().map { entities ->
        entities.map { it.toDto() }
    }

    val availableGarmentsFlow: Flow<List<GarmentResponse>> = garmentDao.getAllGarmentsFlow().map { entities ->
        entities.filter { it.status != "LENT" }.map { it.toDto() }
    }

    suspend fun refreshGarments() {
        try {
            val response = RetrofitClient.garmentApi.getGarments()
            if (response.success && response.data != null) {
                withContext(Dispatchers.IO) {
                    val pending = garmentDao.getPendingSyncGarments()
                    garmentDao.deleteAllGarments()
                    if (pending.isNotEmpty()) {
                        garmentDao.insertGarments(pending)
                    }
                    val entities = response.data!!.map { it.toEntity(isPendingSync = false) }
                    garmentDao.insertGarments(entities)
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun createGarment(request: CreateGarmentRequest): Result<GarmentResponse> {
        return try {
            val response = RetrofitClient.garmentApi.createGarment(request)
            if (response.success && response.data != null) {
                val garment = response.data
                withContext(Dispatchers.IO) {
                    garmentDao.insertGarment(garment.toEntity(isPendingSync = false))
                }
                Result.success(garment)
            } else {
                Result.failure(Exception(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            val localId = "local_${UUID.randomUUID()}"
            val mockGarment = GarmentResponse(
                id = localId,
                userId = "local_user",
                name = request.name,
                category = request.category,
                colorHex = request.colorHex,
                colorName = request.colorName ?: "Detectado",
                style = request.style,
                brand = request.brand ?: "Genérica",
                size = request.size ?: "M",
                status = request.status,
                imageUrl = request.imageUrl ?: "",
                notes = request.notes ?: "Guardado localmente",
                createdAt = "Recién",
                updatedAt = "Recién"
            )
            withContext(Dispatchers.IO) {
                garmentDao.insertGarment(mockGarment.toEntity(isPendingSync = true))
            }
            Result.success(mockGarment)
        }
    }

    suspend fun syncPendingGarments() {
        val pending = withContext(Dispatchers.IO) { garmentDao.getPendingSyncGarments() }
        for (entity in pending) {
            try {
                val request = CreateGarmentRequest(
                    name = entity.name,
                    category = entity.category,
                    colorHex = entity.colorHex,
                    colorName = entity.colorName,
                    style = entity.style,
                    brand = entity.brand,
                    size = entity.size,
                    status = entity.status,
                    imageUrl = entity.imageUrl,
                    notes = entity.notes
                )
                val response = RetrofitClient.garmentApi.createGarment(request)
                if (response.success && response.data != null) {
                    withContext(Dispatchers.IO) {
                        garmentDao.deleteGarmentById(entity.id)
                        garmentDao.insertGarment(response.data.toEntity(isPendingSync = false))
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateGarment(garment: GarmentResponse) {
        withContext(Dispatchers.IO) {
            garmentDao.insertGarment(garment.toEntity(isPendingSync = false))
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = com.pdm0126.outfix.data.api.dto.UpdateGarmentRequest(
                    name = garment.name,
                    category = garment.category,
                    colorHex = garment.colorHex,
                    colorName = garment.colorName,
                    style = garment.style,
                    brand = garment.brand,
                    size = garment.size,
                    status = garment.status,
                    imageUrl = garment.imageUrl,
                    notes = garment.notes
                )
                RetrofitClient.garmentApi.updateGarment(garment.id, request)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun deleteGarment(id: String) {
        withContext(Dispatchers.IO) {
            garmentDao.deleteGarmentById(id)
        }
        try {
            RetrofitClient.garmentApi.deleteGarment(id)
        } catch (e: Exception) {
        }
    }

    suspend fun sendToLaundry(garments: List<GarmentResponse>) {
        val updatedGarments = garments.map { it.copy(status = "IN_WASH") }
        
        withContext(Dispatchers.IO) {
            garmentDao.insertGarments(updatedGarments.map { it.toEntity(isPendingSync = false) })
        }
        
        for (garment in updatedGarments) {
            try {
                val request = com.pdm0126.outfix.data.api.dto.UpdateGarmentRequest(
                    name = garment.name,
                    category = garment.category,
                    colorHex = garment.colorHex,
                    colorName = garment.colorName,
                    style = garment.style,
                    brand = garment.brand,
                    size = garment.size,
                    status = garment.status,
                    imageUrl = garment.imageUrl,
                    notes = garment.notes
                )
                RetrofitClient.garmentApi.updateGarment(garment.id, request)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun markAsWashed(garmentId: String) {
        val entity = withContext(Dispatchers.IO) { garmentDao.getGarmentById(garmentId) } ?: return
        val updatedDto = entity.toDto().copy(status = "AVAILABLE")
        updateGarment(updatedDto)
    }
}
