package com.pdm0126.outfix.data.mock

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.UUID

data class DayInfo(
    val day: String,
    val calendarDay: Int,
    var topColor: Color,
    var bottomColor: Color,
    var shoesColor: Color,
    var hatColor: Color?,
    var topGarment: GarmentResponse? = null,
    var bottomGarment: GarmentResponse? = null,
    var shoesGarment: GarmentResponse? = null,
    var hatGarment: GarmentResponse? = null,
    var accessories: List<GarmentResponse> = emptyList()
)

object MockDatabase {

    val plannerDays = androidx.compose.runtime.mutableStateListOf(
        DayInfo("LUN", Calendar.MONDAY, Color.White, Color(0xFF90D5E1), Color(0xFF277636), Color(0xFF90D5E1)),
        DayInfo("MAR", Calendar.TUESDAY, Color(0xFF67B0E8), Color.Black, Color.Black, null),
        DayInfo("MIE", Calendar.WEDNESDAY, Color(0xFFFF85E2), Color.White, Color.Black, null),
        DayInfo("JUE", Calendar.THURSDAY, Color(0xFFFF1010), Color.White, Color.White, Color(0xFFFF1010)),
        DayInfo("VIE", Calendar.FRIDAY, Color(0xFFE4A9B0), Color(0xFF94C4E1), Color(0xFFE4A9B0), null),
        DayInfo("SAB", Calendar.SATURDAY, Color(0xFF4C8CA8), Color(0xFF033348), Color.Black, Color(0xFF4C8CA8)),
        DayInfo("DOM", Calendar.SUNDAY, Color(0xFF8F8F8F), Color.Black, Color.Black, Color.Black)
    )


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
            category = "Zapatillas",
            colorHex = "#D32F2F",
            colorName = "Rojo",
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
            name = "Reloj de Pulsera",
            category = "Reloj",
            colorHex = "#FFD700", // Gold
            colorName = "Dorado",
            style = "formal",
            brand = "Casio",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=500&q=80",
            notes = "Reloj elegante",
            createdAt = "2024-03-01T08:45:00Z",
            updatedAt = "2024-03-01T08:45:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Gafas de Sol",
            category = "Gafas",
            colorHex = "#000000",
            colorName = "Negro",
            style = "casual",
            brand = "Ray-Ban",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&q=80",
            notes = "Para días soleados",
            createdAt = "2024-03-01T08:45:00Z",
            updatedAt = "2024-03-01T08:45:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Collar de Plata",
            category = "Joyería",
            colorHex = "#C0C0C0",
            colorName = "Plata",
            style = "elegante",
            brand = "Pandora",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1599643477874-5c866f466cb1?w=500&q=80",
            notes = "Regalo especial",
            createdAt = "2024-03-01T08:45:00Z",
            updatedAt = "2024-03-01T08:45:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Botas de Cuero",
            category = "Botas",
            colorHex = "#5D4037",
            colorName = "Café",
            style = "casual",
            brand = "Timberland",
            size = "42",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=500&q=80",
            notes = "Botas altas para invierno",
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
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Gafas de Sol Clásicas",
            category = "Gafas",
            colorHex = "#000000",
            colorName = "Negro",
            style = "casual",
            brand = "Ray-Ban",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&q=80",
            notes = "Protección UV",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Reloj Elegante",
            category = "Reloj",
            colorHex = "#BDBDBD",
            colorName = "Plata",
            style = "formal",
            brand = "Casio",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=500&q=80",
            notes = "Resistente al agua",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Cinturón de Cuero",
            category = "Cinturón",
            colorHex = "#795548",
            colorName = "Marrón",
            style = "formal",
            brand = "Levi's",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&q=80",
            notes = "Cuero genuino",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Corbata de Seda",
            category = "Corbata",
            colorHex = "#1A237E",
            colorName = "Azul Marino",
            style = "formal",
            brand = "Hugo Boss",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1589756823695-278bc923f962?w=500&q=80",
            notes = "Para bodas",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Bufanda de Lana",
            category = "Bufanda",
            colorHex = "#F44336",
            colorName = "Rojo",
            style = "invierno",
            brand = "Zara",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1520903920243-00d872a2d1c9?w=500&q=80",
            notes = "Muy abrigadora",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Collar de Plata",
            category = "Joyería",
            colorHex = "#E0E0E0",
            colorName = "Plata",
            style = "formal",
            brand = "Pandora",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1599643478524-fb66f7ca0655?w=500&q=80",
            notes = "Regalo especial",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Mochila Urbana",
            category = "Mochila",
            colorHex = "#212121",
            colorName = "Negro",
            style = "casual",
            brand = "North Face",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&q=80",
            notes = "Para la laptop",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Botas de Invierno",
            category = "Bota",
            colorHex = "#5D4037",
            colorName = "Marrón Oscuro",
            style = "invierno",
            brand = "Timberland",
            size = "42",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=500&q=80",
            notes = "Resistentes al agua",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Falda de Verano",
            category = "Falda",
            colorHex = "#E91E63",
            colorName = "Rosa",
            style = "casual",
            brand = "Mango",
            size = "S",
            status = "AVAILABLE",
            imageUrl = "https://img.magnific.com/vector-premium/ilustracion-vectorial-dibujos-animados-falda-rosa-bonita-aislada-sobre-fondo-blanco_1332476-22294.jpg",
            notes = "Fresca",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Vestido Elegante",
            category = "Vestido",
            colorHex = "#000000",
            colorName = "Negro",
            style = "formal",
            brand = "Zara",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1566150905458-1bf1fc113f0d?w=500&q=80",
            notes = "De noche",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Short de Mezclilla",
            category = "Short",
            colorHex = "#42A5F5",
            colorName = "Celeste",
            style = "casual",
            brand = "Levi's",
            size = "32",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=500&q=80",
            notes = "Cómodo",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Abrigo Largo",
            category = "Abrigo",
            colorHex = "#795548",
            colorName = "Marrón",
            style = "invierno",
            brand = "Massimo Dutti",
            size = "L",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?w=500&q=80",
            notes = "Cálido",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Blusa de Seda",
            category = "Blusa",
            colorHex = "#FFFFFF",
            colorName = "Blanco",
            style = "formal",
            brand = "H&M",
            size = "M",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1551799517-eb8f03cb5e6a?w=500&q=80",
            notes = "Ligera",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        ),
        GarmentResponse(
            id = UUID.randomUUID().toString(),
            userId = dummyUserId,
            name = "Gorro de Lana",
            category = "Gorro",
            colorHex = "#9E9E9E",
            colorName = "Gris",
            style = "invierno",
            brand = "Vans",
            size = "Única",
            status = "AVAILABLE",
            imageUrl = "https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=500&q=80",
            notes = "Calentito",
            createdAt = "2024-06-01T10:00:00Z",
            updatedAt = "2024-06-01T10:00:00Z"
        )
    )

    var updateTrigger = androidx.compose.runtime.mutableStateOf(0)

    // Simulador de retraso de red
    suspend fun getGarments(): List<GarmentResponse> {
        kotlinx.coroutines.delay(0)
        return prendas.toList()
    }

    fun addGarment(garment: GarmentResponse) {
        prendas.add(garment)
        updateTrigger.value++
    }
    
    fun updateGarment(garment: GarmentResponse) {
        val index = prendas.indexOfFirst { it.id == garment.id }
        if (index != -1) {
            prendas[index] = garment
            
            plannerDays.forEach { day ->
                if (day.topGarment?.id == garment.id) day.topGarment = garment
                if (day.bottomGarment?.id == garment.id) day.bottomGarment = garment
                if (day.shoesGarment?.id == garment.id) day.shoesGarment = garment
                if (day.hatGarment?.id == garment.id) day.hatGarment = garment
                
                day.accessories = day.accessories.map { if (it.id == garment.id) garment else it }
            }
            
            updateTrigger.value++
        }
    }
    
    fun deleteGarment(id: String) {
        prendas.removeAll { it.id == id }
        updateTrigger.value++
    }
}
