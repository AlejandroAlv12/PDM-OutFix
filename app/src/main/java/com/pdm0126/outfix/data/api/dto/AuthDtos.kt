package com.pdm0126.outfix.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData? = null
)

@Serializable
data class AuthData(
    val token: String,
    val user: UserResponse
)
