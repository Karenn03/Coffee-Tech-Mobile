package com.example.coffeetechmobile.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CartPricingTest {

    private fun item(
        price: Int,
        quantity: Int,
        id: Long = 1L
    ): StoredCartItem {
        return StoredCartItem(
            id = id,
            productId = id,
            variantKey = "",
            name = "Producto",
            description = "",
            unitPrice = price,
            imageResName = "", 
            imagePath = "",
            quantity = quantity
        )
    }

    @Test
    fun subtotalMultipliesPriceByQuantity() {
        val items = listOf(
            item(price = 5000, quantity = 2, id = 1L),
            item(price = 7000, quantity = 1, id = 2L)
        )

        assertEquals(17000, CartPricing.subtotal(items))
    }

    @Test
    fun totalAddsShippingToSubtotal() {
        val items = listOf(
            item(price = 5000, quantity = 1, id = 1L)
        )

        assertEquals(
            9500,
            CartPricing.total(items)
        )
    }

    @Test
    fun emptyCartTotalIsZero() {
        assertEquals(0, CartPricing.total(emptyList()))
    }
}
