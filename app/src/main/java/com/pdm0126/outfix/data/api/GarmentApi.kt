package com.pdm0126.outfix.data.api

import com.pdm0126.outfix.data.api.dto.ApiResponse
import com.pdm0126.outfix.data.api.dto.CreateGarmentRequest
import com.pdm0126.outfix.data.api.dto.UpdateGarmentRequest
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GarmentApi {
    
    @GET("garments")
    suspend fun getGarments(): ApiResponse<List<GarmentResponse>>

    @POST("garments")
    suspend fun createGarment(@Body request: CreateGarmentRequest): ApiResponse<GarmentResponse>

    @PUT("garments/{id}")
    suspend fun updateGarment(
        @Path("id") id: String,
        @Body request: UpdateGarmentRequest
    ): ApiResponse<GarmentResponse>

    @DELETE("garments/{id}")
    suspend fun deleteGarment(@Path("id") id: String): ApiResponse<Unit>
}
