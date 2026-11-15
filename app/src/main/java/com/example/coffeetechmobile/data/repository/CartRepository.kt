package com.example.coffeetechmobile.data.repository

import android.content.Context
import com.example.coffeetechmobile.data.local.CartDao
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.model.StoredCartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Carrito persistente del comprador sobre SQLite
 *
 * Todos los métodos reciben el id del usuario para que cada cuenta tenga
 * su propio carrito y para evitar operar sobre filas de otra persona
 */
class CartRepository(context: Context) {

    private val cartDao = CartDao(
        CoffeeTechDatabase.getInstance(context)
    )

    suspend fun getCart(userId: Long): List<StoredCartItem> =
        withContext(Dispatchers.IO) {
            cartDao.findAll(userId)
        }

    suspend fun addItem(
        userId: Long,
        productId: Long,
        variantKey: String,
        name: String,
        description: String,
        unitPrice: Int,
        imageResName: String,
        imagePath: String,
        quantity: Int
    ): Long = withContext(Dispatchers.IO) {
        cartDao.addOrIncrement(
            userId = userId,
            productId = productId,
            variantKey = variantKey,
            name = name,
            description = description,
            unitPrice = unitPrice,
            imageResName = imageResName,
            imagePath = imagePath,
            quantity = quantity
        )
    }

    suspend fun increaseQuantity(userId: Long, cartItemId: Long) =
        withContext(Dispatchers.IO) {
            cartDao.increaseQuantity(userId, cartItemId)
        }

    suspend fun decreaseQuantity(userId: Long, cartItemId: Long) =
        withContext(Dispatchers.IO) {
            cartDao.decreaseQuantity(userId, cartItemId)
        }

    suspend fun removeItem(userId: Long, cartItemId: Long) =
        withContext(Dispatchers.IO) {
            cartDao.delete(userId, cartItemId)
        }

    suspend fun clear(userId: Long) = withContext(Dispatchers.IO) {
        cartDao.clear(userId)
    }
}
