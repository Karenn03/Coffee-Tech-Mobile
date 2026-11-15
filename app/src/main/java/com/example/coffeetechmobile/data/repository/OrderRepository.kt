package com.example.coffeetechmobile.data.repository

import android.content.Context
import com.example.coffeetechmobile.data.local.CartDao
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.local.OrderDao
import com.example.coffeetechmobile.data.local.UserDao
import com.example.coffeetechmobile.data.model.CartPricing
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Resultado de confirmar una compra
sealed interface CheckoutResult {

    data class Success(val orderNumber: String) : CheckoutResult

    data class Error(val message: String) : CheckoutResult
}

/**
 * Órdenes del comprador
 *
 * Al confirmar el pago se guarda la orden y sus productos en una sola
 * transacción y se vacía el carrito. Así nunca queda una orden creada
 * con el carrito todavía lleno
 */
class OrderRepository(context: Context) {

    private val database = CoffeeTechDatabase.getInstance(context)
    private val cartDao = CartDao(database)
    private val orderDao = OrderDao(database)
    private val userDao = UserDao(database)
    private val sessionManager = SessionManager(context)

    suspend fun checkout(paymentMethod: String): CheckoutResult =
        withContext(Dispatchers.IO) {
            try {
                val session = sessionManager.current()
                    ?: return@withContext CheckoutResult.Error(
                        "Debes iniciar sesión para confirmar el pedido."
                    )

                val items = cartDao.findAll(session.id)

                if (items.isEmpty()) {
                    return@withContext CheckoutResult.Error(
                        "El carrito está vacío."
                    )
                }

                val user = userDao.findById(session.id)?.user

                val deliveryAddress = user
                    ?.location
                    ?.takeIf { it.isNotBlank() }
                    ?: DEFAULT_DELIVERY_ADDRESS

                val orderNumber = orderDao.createOrder(
                    userId = session.id,
                    customerName = user?.fullName ?: session.fullName,
                    deliveryAddress = deliveryAddress,
                    deliveryLatitude = user?.latitude,
                    deliveryLongitude = user?.longitude,
                    deliveryType = DELIVERY_TYPE_HOME,
                    paymentMethod = paymentMethod,
                    subtotal = CartPricing.subtotal(items),
                    discount = CartPricing.DISCOUNT,
                    shipping = CartPricing.SHIPPING_COST,
                    total = CartPricing.total(items),
                    customerNote = "",
                    items = items,
                    createdAt = System.currentTimeMillis()
                )

                CheckoutResult.Success(orderNumber)
            } catch (exception: Exception) {
                CheckoutResult.Error(
                    "No se pudo crear el pedido. Intenta de nuevo."
                )
            }
        }

    // Historial de pedidos que ve el vendedor
    suspend fun getOrders(): List<StoredOrder> =
        withContext(Dispatchers.IO) {
            orderDao.findAll()
        }

    // Cambia el estado de un pedido y devuelve si se actualizó
    suspend fun updateStatus(
        orderId: Long,
        status: String
    ): Boolean = withContext(Dispatchers.IO) {
        orderDao.updateStatus(orderId, status) > 0
    }

    private companion object {
        const val DEFAULT_DELIVERY_ADDRESS = "Bogotá, Colombia"
        const val DELIVERY_TYPE_HOME = "Domicilio"
    }
}
