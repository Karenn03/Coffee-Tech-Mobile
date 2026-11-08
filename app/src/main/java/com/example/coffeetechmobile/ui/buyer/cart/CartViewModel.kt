package com.example.coffeetechmobile.ui.buyer.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {

    private val _items = MutableLiveData<List<CartItem>>(emptyList())

    val items: LiveData<List<CartItem>> = _items

    fun addProduct(product: CartItem) {
        val currentItems = _items.value.orEmpty()
        val existingProduct = currentItems.find {
            it.id == product.id
        }

        val updatedItems = if (existingProduct != null) {
            currentItems.map { item ->
                if (item.id == product.id) {
                    item.copy(
                        quantity = item.quantity + product.quantity
                    )
                } else {
                    item
                }
            }
        } else {
            currentItems + product
        }

        _items.value = updatedItems
    }

    fun increaseQuantity(productId: Int) {
        _items.value = _items.value.orEmpty().map { item ->
            if (item.id == productId) {
                item.copy(quantity = item.quantity + 1)
            } else {
                item
            }
        }
    }

    fun decreaseQuantity(productId: Int) {
        _items.value = _items.value.orEmpty().mapNotNull { item ->
            when {
                item.id != productId -> item
                item.quantity > 1 -> item.copy(
                    quantity = item.quantity - 1
                )
                else -> null
            }
        }
    }

    fun removeProduct(productId: Int) {
        _items.value = _items.value.orEmpty().filter {
            it.id != productId
        }
    }

    fun getSubtotal(): Int {
        return _items.value.orEmpty().sumOf {
            it.price * it.quantity
        }
    }

    companion object {
        // Valor temporal para la demostración del frontend
        const val SHIPPING_COST = 4500
        const val DISCOUNT = 0
    }

    fun getTotal(): Int {
        val subtotal = getSubtotal()

        return if (_items.value.orEmpty().isEmpty()) {
            0
        } else {
            subtotal - DISCOUNT + SHIPPING_COST
        }
    }
}
