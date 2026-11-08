package com.example.coffeetechmobile.ui.buyer.catalog

import com.example.coffeetechmobile.R

object MenuCatalog {

    val products = listOf(
        MenuProduct(
            id = R.id.product_iced_caramel,
            name = "Iced Caramel Latte",
            description = "Café espresso mezclado con leche fría y un toque de caramelo, servido sobre hielo. Dulce, cremoso y refrescante.",
            category = "Bebidas frías",
            price = 7000,
            imageRes = R.drawable.img_iced_caramel_latte,
            isDrink = true,
            isIced = true
        ),
        MenuProduct(
            id = R.id.product_frappe_oreo,
            name = "Frappé Oreo",
            description = "Una deliciosa combinación de espresso, chocolate caliente y leche vaporizada, coronada con crema batida. Perfecta para los amantes del chocolate y el café.",
            category = "Bebidas frías",
            price = 8000,
            imageRes = R.drawable.img_frappe_oreo,
            isDrink = true,
            isIced = true
        ),
        MenuProduct(
            id = R.id.product_muffin_red_velvet,
            name = "Muffin de Red Velvet",
            description = "Muffin suave y esponjoso, con el clásico color rojo y un toque de sabor a chocolate, coronado con un glaseado de queso crema.",
            category = "Panadería y pastelería",
            price = 5000,
            imageRes = R.drawable.img_muffin_red_velvet,
            isDrink = false
        ),
        MenuProduct(
            id = R.id.product_iced_macchiato,
            name = "Iced Macchiato",
            description = "Espresso manchado con leche fría y servido con hielo. Una opción ligera y refrescante para los amantes del café.",
            category = "Bebidas frías",
            price = 6500,
            imageRes = R.drawable.img_iced_macchiato,
            isDrink = true,
            isIced = true
        )
    )

    fun findById(id: Int): MenuProduct? {
        return products.find { it.id == id }
    }
}
