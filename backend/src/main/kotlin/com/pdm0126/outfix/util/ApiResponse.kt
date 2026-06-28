package com.pdm0126.outfix.util

import kotlinx.serialization.Serializable

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
