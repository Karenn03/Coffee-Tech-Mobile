package com.example.coffeetechmobile.data.model

/**
 * Dirección de entrega guardada por el comprador
 *
 * Se conserva el punto exacto del mapa para que la orden pueda asociarse
 * a la ubicación aunque la dirección cambie de texto
 */
data class DeliveryAddress(
    val id: Long,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
