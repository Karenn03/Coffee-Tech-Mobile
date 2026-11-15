package com.example.coffeetechmobile.data.model

/**
 * Producto guardado en el carrito de un comprador
 *
 * Se conservan los datos visibles del producto (nombre, descripción y precio)
 * para que el carrito no dependa de que el producto siga existiendo. La
 * combinación usuario + producto + variante es única en la base de datos
 */
data class StoredCartItem(
    val id: Long,
    val productId: Long,
    val variantKey: String,
    val name: String,
    val description: String,
    val unitPrice: Int,
    val imageResName: String,
    val imagePath: String,
    val quantity: Int
)
