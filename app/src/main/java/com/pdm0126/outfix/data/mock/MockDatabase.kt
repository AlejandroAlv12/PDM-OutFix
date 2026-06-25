package com.pdm0126.outfix.data.mock

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import java.util.UUID

object MockDatabase {

    private val dummyUserId = UUID.randomUUID().toString()

    val prendas: List<GarmentResponse> = listOf(
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Camiseta Básica Blanca",
            category = "Camiseta",
            colorHex = "#FFFFFF",
            colorName = "Blanco",
            style = "casual",
            brand = "Zara",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500&q=80",
            notes = "Ideal para verano",
            createdAt = "2023-10-01T12:00:00Z",
            updatedAt = "2023-10-01T12:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Jeans Clásicos",
            category = "Jeans",
            colorHex = "#283593",
            colorName = "Azul Oscuro",
            style = "casual",
            brand = "Levi's",
            size = "32",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=500&q=80",
            notes = "Un poco desgastados",
            createdAt = "2023-10-05T10:30:00Z",
            updatedAt = "2023-10-05T10:30:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Chaqueta de Cuero",
            category = "Chaqueta",
            colorHex = "#000000",
            colorName = "Negro",
            style = "formal/casual",
            brand = "AllSaints",
            size = "L",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&q=80",
            notes = "Para salir de noche",
            createdAt = "2023-11-20T18:15:00Z",
            updatedAt = "2023-11-20T18:15:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Zapatillas Deportivas",
            category = "Calzado",
            colorHex = "#E0E0E0",
            colorName = "Gris/Blanco",
            style = "deportiva",
            brand = "Nike",
            size = "42",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&q=80",
            notes = "Muy cómodas para correr",
            createdAt = "2024-01-10T09:00:00Z",
            updatedAt = "2024-01-10T09:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Camisa de Cuadros",
            category = "Camisa",
            colorHex = "#B71C1C",
            colorName = "Rojo/Negro",
            style = "casual",
            brand = "Vans",
            size = "M",
            status = "IN_LAUNDRY",
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e32f6b717?w=500&q=80",
            notes = "Franela cálida",
            createdAt = "2023-12-05T14:20:00Z",
            updatedAt = "2024-02-15T11:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Pantalón de Vestir",
            category = "Pantalón",
            colorHex = "#424242",
            colorName = "Gris oscuro",
            style = "formal",
            brand = "Massimo Dutti",
            size = "34",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1594938298598-70f70f9076d3?w=500&q=80",
            notes = "Para eventos formales",
            createdAt = "2024-03-01T08:45:00Z",
            updatedAt = "2024-03-01T08:45:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Suéter de Lana",
            category = "Suéter",
            colorHex = "#FFECB3",
            colorName = "Beige",
            style = "invierno",
            brand = "H&M",
            size = "L",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=500&q=80",
            notes = "Pica un poco si no usas camiseta debajo",
            createdAt = "2023-09-15T16:30:00Z",
            updatedAt = "2023-09-15T16:30:00Z"
        )
    )

    // Simulador de retraso de red
    suspend fun getGarments(): List<GarmentResponse> {
        kotlinx.coroutines.delay(800) // 800ms de "carga"
        return prendas
    }
}
