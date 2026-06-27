package com.pdm0126.outfix.data.api

import com.pdm0126.outfix.data.api.dto.ApiResponse
import com.pdm0126.outfix.data.api.dto.CreateGarmentRequest
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GarmentApi {
    
    @POST("garments")
    suspend fun createGarment(@Body request: CreateGarmentRequest): ApiResponse<GarmentResponse>

    @retrofit2.http.GET("garments")
    suspend fun getGarments(): ApiResponse<List<GarmentResponse>>
}
