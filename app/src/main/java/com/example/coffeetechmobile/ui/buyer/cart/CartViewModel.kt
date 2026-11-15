package com.example.coffeetechmobile.ui.buyer.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.CartPricing
import com.example.coffeetechmobile.data.model.StoredCartItem
import com.example.coffeetechmobile.data.repository.CartRepository
import com.example.coffeetechmobile.data.session.SessionManager
import kotlinx.coroutines.launch

/**
 * Carrito persistente del comprador
 *
 * Cada cambio se guarda en SQLite y luego se recarga el carrito para que la
 * pantalla del carrito refleje exactamente lo que hay en la base de datos
 */
class CartViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val cartRepository = CartRepository(application)
    private val sessionManager = SessionManager(application)
    private val appContext = getApplication<Application>()

    private val _items = MutableLiveData<List<CartItem>>(emptyList())

    val items: LiveData<List<CartItem>> = _items

    init {
        refresh()
    }

    // Recarga el carrito del usuario que tiene la sesión activa
    fun refresh() {
        val userId = sessionManager.current()?.id

        if (userId == null) {
            _items.value = emptyList()
            return
        }

        reload(userId)
    }

    fun addProduct(product: CartItem) {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            try {
                cartRepository.addItem(
                    userId = userId,
                    productId = product.productId,
                    variantKey = product.variantKey,
                    name = product.name,
                    description = product.description,
                    unitPrice = product.price,
                    imageResName = resolveImageResName(
                        product.imageRes
                    ),
                    imagePath = product.imagePath.orEmpty(),
                    quantity = product.quantity
                )

                reload(userId)
            } catch (exception: Exception) {
                // Si falla la escritura se conserva el carrito anterior
            }
        }
    }

    fun increaseQuantity(cartItemId: Long) {
        updateQuantity(cartItemId) { userId ->
            cartRepository.increaseQuantity(userId, cartItemId)
        }
    }

    fun decreaseQuantity(cartItemId: Long) {
        updateQuantity(cartItemId) { userId ->
            cartRepository.decreaseQuantity(userId, cartItemId)
        }
    }

    fun removeProduct(cartItemId: Long) {
        updateQuantity(cartItemId) { userId ->
            cartRepository.removeItem(userId, cartItemId)
        }
    }

    fun clearCart() {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            try {
                cartRepository.clear(userId)
            } finally {
                reload(userId)
            }
        }
    }

    fun getSubtotal(): Int {
        return _items.value.orEmpty().sumOf {
            it.price * it.quantity
        }
    }

    fun getTotal(): Int {
        val subtotal = getSubtotal()

        return if (_items.value.orEmpty().isEmpty()) {
            0
        } else {
            subtotal - DISCOUNT + SHIPPING_COST
        }
    }

    private fun updateQuantity(
        cartItemId: Long,
        operation: suspend (Long) -> Unit
    ) {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            try {
                operation(userId)
            } finally {
                reload(userId)
            }
        }
    }

    private fun reload(userId: Long) {
        viewModelScope.launch {
            val storedItems = try {
                cartRepository.getCart(userId)
            } catch (exception: Exception) {
                emptyList()
            }

            _items.value = storedItems.map { it.toCartItem() }
        }
    }

    // Resuelve el drawable guardado para restaurar la imagen del carrito
    private fun StoredCartItem.toCartItem(): CartItem {
        val drawableId = if (imageResName.isNotBlank()) {
            appContext.resources.getIdentifier(
                imageResName,
                "drawable",
                appContext.packageName
            )
        } else {
            0
        }

        return CartItem(
            id = id,
            productId = productId,
            variantKey = variantKey,
            name = name,
            description = description,
            price = unitPrice,
            imageRes = if (drawableId != 0) {
                drawableId
            } else {
                R.drawable.ic_coffee
            },
            imagePath = imagePath.ifBlank { null },
            quantity = quantity
        )
    }

    // Guarda el nombre del recurso para poder restaurarlo luego
    private fun resolveImageResName(imageRes: Int): String {
        return try {
            appContext.resources.getResourceEntryName(imageRes)
        } catch (exception: Exception) {
            ""
        }
    }

    companion object {
        const val SHIPPING_COST = CartPricing.SHIPPING_COST
        const val DISCOUNT = CartPricing.DISCOUNT
    }
}
