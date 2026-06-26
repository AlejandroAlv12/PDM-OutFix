package com.pdm0126.outfix.data.mock

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import java.util.UUID

object MockDatabase {

    private val dummyUserId = UUID.randomUUID().toString()

    val prendas: MutableList<GarmentResponse> = mutableListOf(
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
            colorHex = "#42A5F5",
            colorName = "Azul",
            style = "casual",
            brand = "Levi's",
            size = "32",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1542272604-780c8d5215fa?w=500&q=80",
            notes = "Combina con todo",
            createdAt = "2023-10-05T09:30:00Z",
            updatedAt = "2023-10-05T09:30:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Chaqueta de Cuero",
            category = "Chaqueta",
            colorHex = "#212121",
            colorName = "Negro",
            style = "formal",
            brand = "Pull&Bear",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&q=80",
            notes = "Para salir de noche",
            createdAt = "2023-11-20T14:15:00Z",
            updatedAt = "2023-11-20T14:15:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Zapatillas Deportivas",
            category = "Calzado",
            colorHex = "#FAFAFA",
            colorName = "Blanco",
            style = "deportivo",
            brand = "Nike",
            size = "42",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&q=80",
            notes = "Correr por las mañanas",
            createdAt = "2024-01-10T07:00:00Z",
            updatedAt = "2024-01-10T07:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Camisa de Cuadros",
            category = "Camisa",
            colorHex = "#D32F2F",
            colorName = "Rojo",
            style = "casual",
            brand = "Springfield",
            size = "L",
            status = "IN_WASH",
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e32f85e98?w=500&q=80",
            notes = "Le falta un botón",
            createdAt = "2024-02-14T11:20:00Z",
            updatedAt = "2024-02-14T11:20:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Pantalón de Vestir",
            category = "Pantalón",
            colorHex = "#37474F",
            colorName = "Gris Oscuro",
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
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Gorra de Béisbol",
            category = "Gorra",
            colorHex = "#D32F2F",
            colorName = "Rojo",
            style = "deportivo",
            brand = "New Era",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=500&q=80",
            notes = "Para días soleados",
            createdAt = "2024-04-10T10:00:00Z",
            updatedAt = "2024-04-10T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Sombrero de Paja",
            category = "Sombrero",
            colorHex = "#FFF59D",
            colorName = "Amarillo Claro",
            style = "playa",
            brand = "Genérica",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1514327605112-b887c0e61c0a?w=500&q=80",
            notes = "Ideal para la playa",
            createdAt = "2024-05-01T14:20:00Z",
            updatedAt = "2024-05-01T14:20:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Bolso Tote de Lona",
            category = "Bolso",
            colorHex = "#8D6E63",
            colorName = "Marrón",
            style = "casual",
            brand = "Zara",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=500&q=80",
            notes = "Cabe de todo",
            createdAt = "2024-01-20T09:15:00Z",
            updatedAt = "2024-01-20T09:15:00Z"
        )
    )

    var updateTrigger = androidx.compose.runtime.mutableStateOf(0)

    // Simulador de retraso de red
    suspend fun getGarments(): List<GarmentResponse> {
        kotlinx.coroutines.delay(100) // 800ms de "carga"
        return prendas.toList()
    }

    fun addGarment(garment: GarmentResponse) {
        prendas.add(garment)
        updateTrigger.value++
    }
}
