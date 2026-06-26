package com.pdm0126.outfix.data.api

import com.pdm0126.outfix.data.api.dto.AuthResponse
import com.pdm0126.outfix.data.api.dto.LoginRequest
import com.pdm0126.outfix.data.api.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}
