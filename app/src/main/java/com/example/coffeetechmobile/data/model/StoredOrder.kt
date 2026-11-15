package com.example.coffeetechmobile.data.model

/**
 * Pedido tal como queda guardado en SQLite
 *
 * Los datos del cliente y de cada producto se guardan como una copia
 * (snapshot) para que el historial no cambie si luego se edita o elimina
 * el producto o la cuenta del comprador
 */
data class StoredOrder(
    val id: Long,
    val orderNumber: String,
    val userId: Long?,
    val customerName: String,
    val deliveryAddress: String,
    val deliveryLatitude: Double?,
    val deliveryLongitude: Double?,
    val deliveryType: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val confirmationCode: String,
    val subtotal: Int,
    val discount: Int,
    val shipping: Int,
    val total: Int,
    val status: String,
    val customerNote: String,
    val createdAt: Long,
    val items: List<StoredOrderItem>
)

data class StoredOrderItem(
    val productId: Long?,
    val name: String,
    val description: String,
    val unitPrice: Int,
    val quantity: Int,
    val imageResName: String,
    val imagePath: String
)
