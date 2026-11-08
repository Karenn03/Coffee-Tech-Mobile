package com.example.coffeetechmobile.ui.buyer.cart

data class CartItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val imageRes: Int,
    val quantity: Int = 1
)
