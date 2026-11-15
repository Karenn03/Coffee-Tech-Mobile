package com.example.coffeetechmobile.ui.buyer.cart

import androidx.annotation.DrawableRes

/**
 * Producto que se muestra en el carrito del comprador
 *
 * [id] es el id de la fila en SQLite y se usa para aumentar, disminuir
 * o eliminar. [variantKey] distingue las personalizaciones del mismo
 * producto (tamaño, leche, dulzor, etc.)
 */
data class CartItem(
    val id: Long = 0L,
    val productId: Long = 0L,
    val variantKey: String = "",
    val name: String,
    val description: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val imagePath: String? = null,
    val quantity: Int = 1
)
