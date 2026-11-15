
package com.example.coffeetechmobile.ui.seller.orders

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.coffeetechmobile.R

enum class SellerOrderStatus {
    RECEIVED,
    PREPARING,
    READY,
    DELIVERED
}

enum class SellerOrderFilter {
    ALL,
    ACTIVE,
    DELIVERED
}

data class SellerOrderProduct(
    val name: String,
    val quantity: Int,
    @DrawableRes val imageRes: Int,
    val imagePath: String? = null
)

data class SellerOrder(
    val id: String,
    val orderId: Long,
    val date: String,
    val customer: String,
    val products: List<SellerOrderProduct>,
    val totalUnits: Int,
    val deliveryType: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val total: Int,
    val status: SellerOrderStatus,
    val customerNote: String? = null
)

// Totales que se muestran en las tarjetas superiores del historial
data class SellerOrderCounts(
    val active: Int,
    val delivered: Int,
    val received: Int,
    val preparing: Int,
    val ready: Int
)

// Siguiente estado del flujo de preparación
fun SellerOrderStatus.next(): SellerOrderStatus? {
    return when (this) {
        SellerOrderStatus.RECEIVED -> SellerOrderStatus.PREPARING
        SellerOrderStatus.PREPARING -> SellerOrderStatus.READY
        SellerOrderStatus.READY -> SellerOrderStatus.DELIVERED
        SellerOrderStatus.DELIVERED -> null
    }
}

// Nombre visible del estado para el diálogo de confirmación
@get:StringRes
val SellerOrderStatus.labelRes: Int
    get() = when (this) {
        SellerOrderStatus.RECEIVED ->
            R.string.seller_orders_status_received

        SellerOrderStatus.PREPARING ->
            R.string.seller_orders_status_preparing

        SellerOrderStatus.READY ->
            R.string.seller_orders_status_ready

        SellerOrderStatus.DELIVERED ->
            R.string.seller_orders_status_delivered
    }
