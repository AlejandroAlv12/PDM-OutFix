package com.pdm0126.outfix.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.api.dto.CreateLentItemRequest
import com.pdm0126.outfix.data.local.LentItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class LentRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lent_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _lentItemsFlow = MutableStateFlow<List<LentItem>>(emptyList())
    val lentItemsFlow: StateFlow<List<LentItem>> = _lentItemsFlow.asStateFlow()

    init {
        _lentItemsFlow.value = loadLocal()
    }

    // ---- Local persistence ----

    private fun loadLocal(): List<LentItem> {
        val raw = prefs.getString(KEY_LENT_ITEMS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<LentItem>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocal(items: List<LentItem>) {
        prefs.edit().putString(KEY_LENT_ITEMS, json.encodeToString(items)).apply()
        _lentItemsFlow.value = items
    }

    // ---- Public API ----

    suspend fun refresh() {
        try {
            val response = RetrofitClient.lentApi.getLentItems()
            if (response.success && response.data != null) {
                val items = response.data!!.map { dto ->
                    LentItem(
                        id = dto.id,
                        garmentId = dto.garmentId,
                        garmentImageUrl = dto.garmentImageUrl,
                        garmentName = dto.garmentName,
                        borrowerName = dto.borrowerName,
                        lentDate = dto.lentDate,
                        reclaimDate = dto.reclaimDate,
                        reclaimDateMillis = dto.reclaimDateMillis,
                        isReturned = dto.isReturned
                    )
                }
                saveLocal(items)
            }
        } catch (e: Exception) {
            // Network error — rely on local cache
        }
    }

    suspend fun createLentItem(
        garmentId: String,
        garmentImageUrl: String?,
        garmentName: String,
        borrowerName: String,
        lentDate: String,
        reclaimDate: String,
        reclaimDateMillis: Long
    ): LentItem {
        val localId = "local_${UUID.randomUUID()}"
        val localItem = LentItem(
            id = localId,
            garmentId = garmentId,
            garmentImageUrl = garmentImageUrl,
            garmentName = garmentName,
            borrowerName = borrowerName,
            lentDate = lentDate,
            reclaimDate = reclaimDate,
            reclaimDateMillis = reclaimDateMillis,
            isReturned = false
        )

        // Optimistic local save
        val updated = loadLocal() + localItem
        saveLocal(updated)

        // Try server asynchronously so UI doesn't block
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CreateLentItemRequest(
                    garmentId = garmentId,
                    garmentImageUrl = garmentImageUrl,
                    garmentName = garmentName,
                    borrowerName = borrowerName,
                    lentDate = lentDate,
                    reclaimDate = reclaimDate,
                    reclaimDateMillis = reclaimDateMillis
                )
                val response = RetrofitClient.lentApi.createLentItem(request)
                if (response.success && response.data != null) {
                    // Replace local item with server item
                    val serverItem = response.data!!.let { dto ->
                        LentItem(
                            id = dto.id,
                            garmentId = dto.garmentId,
                            garmentImageUrl = dto.garmentImageUrl,
                            garmentName = dto.garmentName,
                            borrowerName = dto.borrowerName,
                            lentDate = dto.lentDate,
                            reclaimDate = dto.reclaimDate,
                            reclaimDateMillis = dto.reclaimDateMillis,
                            isReturned = dto.isReturned
                        )
                    }
                    val list = loadLocal().filter { it.id != localId } + serverItem
                    saveLocal(list)
                }
            } catch (e: Exception) {
                // Keep local version
            }
        }

        return localItem
    }

    suspend fun markReturned(id: String) {
        // Optimistic local update
        val list = loadLocal().map { if (it.id == id) it.copy(isReturned = true) else it }
        saveLocal(list)

        withContext(Dispatchers.IO) {
            try {
                RetrofitClient.lentApi.markReturned(id)
            } catch (e: Exception) {
                // Keep local change
            }
        }
    }

    suspend fun deleteLentItem(id: String) {
        val list = loadLocal().filter { it.id != id }
        saveLocal(list)

        withContext(Dispatchers.IO) {
            try {
                RetrofitClient.lentApi.deleteLentItem(id)
            } catch (e: Exception) {}
        }
    }

    companion object {
        private const val KEY_LENT_ITEMS = "lent_items_json"
    }
}
