package com.example.coffeetechmobile.ui.manager.reports

enum class SaleStatus {
    COMPLETED,
    PENDING
}

enum class SaleFilter {
    ALL,
    COMPLETED,
    PENDING
}

data class SalesReport(
    val id: String,
    val date: String,
    val customer: String,
    val products: String,
    val paymentMethod: String,
    val amount: Int,
    val status: SaleStatus
)
