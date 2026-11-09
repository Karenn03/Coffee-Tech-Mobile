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

data class SellerOrder(
    val id: String,
    val date: String,
    val customer: String,
    val products: List<String>,
    val totalUnits: Int,
    val deliveryType: String,
    val paymentMethod: String,
    val total: Int,
    val status: SellerOrderStatus
)
