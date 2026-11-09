
package com.example.coffeetechmobile.ui.seller.orders

enum class SellerOrderStatus {
    RECEIVED,
    PREPARING,
    READY,
    DELIVERED
}

enum class SellerOrderFilter {
    ALL,
    ACTIVE,
    DELIVERED
}

data class SellerOrderProduct(
    val productId: Int,
    val quantity: Int
)

data class SellerOrder(
    val id: String,
    val date: String,
    val customer: String,
    val products: List<SellerOrderProduct>,
    val totalUnits: Int,
    val deliveryType: String,
    val paymentMethod: String,
    val total: Int,
    val status: SellerOrderStatus,
    val customerNote: String? = null
)
