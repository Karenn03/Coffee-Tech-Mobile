package com.example.coffeetechmobile.ui.buyer.catalog

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.Product

/**
 * Modelo de producto que usan las pantallas del comprador
 *
 * [productId] es el id real de SQLite y se usa para abrir el detalle.
 * [id] se conserva para la lógica del carrito y los catálogos estáticos
 */
data class MenuProduct(
    val id: Int,
    val productId: Long = 0L,
    val name: String,
    val description: String,
    val category: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val imagePath: String? = null,
    val isDrink: Boolean,
    val isIced: Boolean = false
) {

    companion object {

        // Convierte el producto de SQLite al modelo de la tienda
        fun fromProduct(
            context: Context,
            product: Product
        ): MenuProduct {
            val imageRes = if (product.imageResName.isNotBlank()) {
                context.resources.getIdentifier(
                    product.imageResName,
                    "drawable",
                    context.packageName
                ).takeIf { it != 0 } ?: R.drawable.ic_coffee
            } else {
                R.drawable.ic_coffee
            }

            val isDrink = product.category == "Bebidas calientes" ||
                product.category == "Bebidas frías"

            val isIced = product.category == "Bebidas frías"

            return MenuProduct(
                id = product.id.toInt(),
                productId = product.id,
                name = product.name,
                description = product.description,
                category = product.category,
                price = product.salePrice,
                imageRes = imageRes,
                imagePath = product.imagePath.ifBlank { null },
                isDrink = isDrink,
                isIced = isIced
            )
        }
    }
}
