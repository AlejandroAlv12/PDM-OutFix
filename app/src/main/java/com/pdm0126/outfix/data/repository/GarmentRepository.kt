package com.pdm0126.outfix.data.repository

import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.api.dto.CreateGarmentRequest
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.local.GarmentEntity
import com.pdm0126.outfix.data.local.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class GarmentRepository(
    private val garmentDao: GarmentDao
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncNotification = MutableStateFlow<String?>(null)
    val syncNotification: StateFlow<String?> = _syncNotification.asStateFlow()

    private fun showSyncNotification(message: String) {
        repoScope.launch {
            _syncNotification.value = message
            delay(2500)
            _syncNotification.value = null
        }
    }

    val garmentsFlow: Flow<List<GarmentResponse>> = garmentDao.getAllGarmentsFlow().map { entities ->
        entities.map { it.toDto() }
    }

    val availableGarmentsFlow: Flow<List<GarmentResponse>> = garmentDao.getAllGarmentsFlow().map { entities ->
        entities.filter { it.status != "LENT" }.map { it.toDto() }
    }

    suspend fun refreshGarments() {
        repoScope.launch {
            try {
                // 1. Sincronizar prendas pendientes primero
                val pendingSynced = syncPendingGarments()

                // 2. Consultar prendas del servidor
                val response = RetrofitClient.garmentApi.getGarments()
                if (response.success && response.data != null) {
                    val remoteList = response.data ?: emptyList()
                    val remoteEntities = remoteList.map { it.toEntity(isPendingSync = false) }
                    val remoteIds = remoteEntities.map { it.id }.toSet()

                    var hasNewItems = false
                    withContext(Dispatchers.IO) {
                        val localEntities = garmentDao.getAllGarments()
                        val localSynced = localEntities.filter { !it.isPendingSync }
                        val localSyncedIds = localSynced.map { it.id }.toSet()

                        // Detectar si el servidor trajo prendas que no teníamos
                        hasNewItems = remoteEntities.any { it.id !in localSyncedIds }

                        // Eliminar únicamente las que fueron borradas en el servidor (que no sean pendientes)
                        val toDelete = localSynced.filter { it.id !in remoteIds }
                        for (del in toDelete) {
                            garmentDao.deleteGarmentById(del.id)
                        }

                        // Actualizar / insertar prendas del servidor
                        garmentDao.insertGarments(remoteEntities)
                    }

                    if (hasNewItems || pendingSynced > 0) {
                        showSyncNotification(
                            if (hasNewItems) "Prendas actualizadas desde la nube"
                            else "Prendas sincronizadas"
                        )
                    }
                }
            } catch (e: Exception) {
                // Servidor o base de datos offline: mantener datos locales intactos en silencio
            }
        }
    }

    suspend fun createGarment(request: CreateGarmentRequest): Result<GarmentResponse> {
        val localId = "local_${UUID.randomUUID()}"
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val localGarment = GarmentResponse(
            id = localId,
            userId = RetrofitClient.sessionManager?.fetchUserId() ?: "local_user",
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
            createdAt = now,
            updatedAt = now
        )

        // 1. Guardar de inmediato en local (Room)
        withContext(Dispatchers.IO) {
            garmentDao.insertGarment(localGarment.toEntity(isPendingSync = true))
        }

        // 2. Sincronizar en segundo plano si la base de datos remota está disponible
        repoScope.launch {
            try {
                val response = RetrofitClient.garmentApi.createGarment(request)
                if (response.success && response.data != null) {
                    val serverGarment = response.data
                    withContext(Dispatchers.IO) {
                        garmentDao.deleteGarmentById(localId)
                        garmentDao.insertGarment(serverGarment.toEntity(isPendingSync = false))
                    }
                    showSyncNotification("Prenda sincronizada con la nube")
                }
            } catch (e: Exception) {
                // Servidor offline: queda seguro en local con isPendingSync = true
            }
        }

        // 3. Retornar inmediatamente éxito con el ítem local para que el usuario no espere
        return Result.success(localGarment)
    }

    suspend fun syncPendingGarments(): Int {
        val pending = withContext(Dispatchers.IO) { garmentDao.getPendingSyncGarments() }
        if (pending.isEmpty()) return 0

        var syncedCount = 0
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
                    val serverGarment = response.data
                    withContext(Dispatchers.IO) {
                        garmentDao.deleteGarmentById(entity.id)
                        garmentDao.insertGarment(serverGarment.toEntity(isPendingSync = false))
                    }
                    syncedCount++
                }
            } catch (e: Exception) {
                // Si el servidor sigue caído, detener el bucle
                break
            }
        }
        return syncedCount
    }

    suspend fun updateGarment(garment: GarmentResponse) {
        withContext(Dispatchers.IO) {
            garmentDao.insertGarment(garment.toEntity(isPendingSync = false))
        }

        repoScope.launch {
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
        repoScope.launch {
            try {
                RetrofitClient.garmentApi.deleteGarment(id)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun sendToLaundry(garments: List<GarmentResponse>) {
        val updatedGarments = garments.map { it.copy(status = "IN_WASH") }
        
        withContext(Dispatchers.IO) {
            garmentDao.insertGarments(updatedGarments.map { it.toEntity(isPendingSync = false) })
        }
        
        repoScope.launch {
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
    }

    suspend fun markAsWashed(garmentId: String) {
        val entity = withContext(Dispatchers.IO) { garmentDao.getGarmentById(garmentId) } ?: return
        val updatedDto = entity.toDto().copy(status = "AVAILABLE")
        updateGarment(updatedDto)
    }

    suspend fun getAllGarmentsRaw(): List<GarmentEntity> {
        return withContext(Dispatchers.IO) {
            garmentDao.getAllGarments()
        }
    }
}
