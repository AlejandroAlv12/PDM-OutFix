package com.pdm0126.outfix.data.api

import com.pdm0126.outfix.data.api.dto.ApiResponse
import com.pdm0126.outfix.data.api.dto.LentItemDto
import com.pdm0126.outfix.data.api.dto.CreateLentItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LentApi {
    @GET("lent")
    suspend fun getLentItems(): ApiResponse<List<LentItemDto>>

    @POST("lent")
    suspend fun createLentItem(@Body request: CreateLentItemRequest): ApiResponse<LentItemDto>

    @PATCH("lent/{id}/return")
    suspend fun markReturned(@Path("id") id: String): ApiResponse<LentItemDto>

    @DELETE("lent/{id}")
    suspend fun deleteLentItem(@Path("id") id: String): ApiResponse<Unit>
}
