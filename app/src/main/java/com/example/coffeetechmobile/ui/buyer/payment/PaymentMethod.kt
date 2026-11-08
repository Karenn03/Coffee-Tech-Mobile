package com.example.coffeetechmobile.ui.buyer.payment

import androidx.annotation.DrawableRes

enum class PaymentMethodType {
    PSE,
    DIGITAL_WALLET,
    CARD,
    CASH_ON_DELIVERY
}

data class PaymentMethod(
    val type: PaymentMethodType,
    val name: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val badge: String? = null,
    val details: String? = null
)
