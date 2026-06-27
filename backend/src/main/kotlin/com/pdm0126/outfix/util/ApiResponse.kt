package com.pdm0126.outfix.util

import kotlinx.serialization.Serializable

// Wrapper estandarizado para TODAS las respuestas de la API.
// El frontend (Ktor Client) decodifica siempre esta misma estructura.
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> ok(data: T, message: String = "Operación exitosa") =
            ApiResponse(success = true, message = message, data = data)

        fun error(message: String) =
            ApiResponse(success = false, message = message, data = null as String?)
    }
}
