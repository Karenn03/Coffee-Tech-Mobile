package com.example.coffeetechmobile.ui.buyer.catalog

import androidx.annotation.DrawableRes

data class MenuProduct(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val isDrink: Boolean,
    val isIced: Boolean = false
)
