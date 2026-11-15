package com.example.coffeetechmobile.data.model

/**
 * Cálculos compartidos entre el carrito, el pago y las órdenes
 *
 * Se centralizan aquí para que el total mostrado en pantalla y el
 * total guardado en SQLite siempre coincidan
 */
object CartPricing {

    // Valor temporal de envío usado por la demostración
    const val SHIPPING_COST = 4500

    const val DISCOUNT = 0

    fun subtotal(items: List<StoredCartItem>): Int {
        return items.sumOf { it.unitPrice * it.quantity }
    }

    fun total(items: List<StoredCartItem>): Int {
        return if (items.isEmpty()) {
            0
        } else {
            subtotal(items) - DISCOUNT + SHIPPING_COST
        }
    }
}
