package com.example.coffeetechmobile.ui.seller.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.model.StoredOrderItem
import com.example.coffeetechmobile.data.repository.OrderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Historial de pedidos del vendedor, leído desde SQLite
 *
 * Los filtros y los contadores se calculan sobre las órdenes guardadas,
 * de modo que un pedido confirmado por el comprador aparece aquí
 * automáticamente al volver a la pantalla
 */
class SellerOrdersViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = OrderRepository(application)
    private val appContext = getApplication<Application>()

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy · HH:mm",
        Locale("es", "CO")
    )

    private var allOrders: List<SellerOrder> = emptyList()

    private val _selectedFilter =
        MutableLiveData(SellerOrderFilter.ALL)

    val selectedFilter: LiveData<SellerOrderFilter> =
        _selectedFilter

    private val _visibleOrders =
        MutableLiveData<List<SellerOrder>>(emptyList())

    val visibleOrders: LiveData<List<SellerOrder>> =
        _visibleOrders

    private val _counts = MutableLiveData(
        SellerOrderCounts(0, 0, 0, 0, 0)
    )

    val counts: LiveData<SellerOrderCounts> = _counts

    init {
        refresh()
    }

    // Vuelve a leer las órdenes guardadas en SQLite
    fun refresh() {
        viewModelScope.launch {
            allOrders = try {
                repository.getOrders().map { it.toSellerOrder() }
            } catch (exception: Exception) {
                emptyList()
            }

            applyFilter(_selectedFilter.value ?: SellerOrderFilter.ALL)

            _counts.value = SellerOrderCounts(
                active = allOrders.count {
                    it.status != SellerOrderStatus.DELIVERED
                },
                delivered = countByStatus(
                    SellerOrderStatus.DELIVERED
                ),
                received = countByStatus(
                    SellerOrderStatus.RECEIVED
                ),
                preparing = countByStatus(
                    SellerOrderStatus.PREPARING
                ),
                ready = countByStatus(SellerOrderStatus.READY)
            )
        }
    }

    fun setFilter(filter: SellerOrderFilter) {
        _selectedFilter.value = filter
        applyFilter(filter)
    }

    /**
     * Avanza el pedido al siguiente estado:
     * Recibido → Preparando → Listo → Entregado.
     */
    fun advanceStatus(orderId: Long) {
        val order = allOrders.find { it.orderId == orderId } ?: return
        val nextStatus = order.status.next() ?: return

        viewModelScope.launch {
            try {
                repository.updateStatus(orderId, nextStatus.name)
            } finally {
                refresh()
            }
        }
    }

    private fun applyFilter(filter: SellerOrderFilter) {
        _visibleOrders.value = when (filter) {
            SellerOrderFilter.ALL -> allOrders

            SellerOrderFilter.ACTIVE ->
                allOrders.filter {
                    it.status != SellerOrderStatus.DELIVERED
                }

            SellerOrderFilter.DELIVERED ->
                allOrders.filter {
                    it.status == SellerOrderStatus.DELIVERED
                }
        }
    }

    private fun countByStatus(status: SellerOrderStatus): Int {
        return allOrders.count { it.status == status }
    }

    private fun StoredOrder.toSellerOrder(): SellerOrder {
        return SellerOrder(
            id = orderNumber,
            orderId = id,
            date = dateFormat.format(Date(createdAt)),
            customer = customerName,
            products = items.map { it.toSellerOrderProduct() },
            totalUnits = items.sumOf { it.quantity },
            deliveryType = deliveryType,
            paymentMethod = paymentMethod,
            total = total,
            status = toSellerStatus(),
            customerNote = customerNote.ifBlank { null }
        )
    }

    private fun StoredOrder.toSellerStatus(): SellerOrderStatus {
        return runCatching {
            SellerOrderStatus.valueOf(status)
        }.getOrDefault(SellerOrderStatus.RECEIVED)
    }

    private fun StoredOrderItem.toSellerOrderProduct(): SellerOrderProduct {
        val drawableId = if (imageResName.isNotBlank()) {
            appContext.resources.getIdentifier(
                imageResName,
                "drawable",
                appContext.packageName
            )
        } else {
            0
        }

        return SellerOrderProduct(
            name = name,
            quantity = quantity,
            imageRes = if (drawableId != 0) {
                drawableId
            } else {
                R.drawable.ic_coffee
            },
            imagePath = imagePath.ifBlank { null }
        )
    }
}
