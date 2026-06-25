package com.pdm0126.outfix.models

import kotlinx.serialization.Serializable

// --- Requests (lo que el frontend envía) ---

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// --- Responses (lo que el backend retorna) ---

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String
)

@Serializable
data class AuthResponse(
    val user: UserResponse,
    val token: String
)
