package com.example.coffeetechmobile.ui.manager.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.repository.OrderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Reporte de ventas del administrador, calculado con las órdenes de SQLite
class SalesReportViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = OrderRepository(application)

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy · HH:mm",
        Locale("es", "CO")
    )

    private var allSales: List<SalesReport> = emptyList()

    private val _selectedFilter =
        MutableLiveData(SaleFilter.ALL)

    val selectedFilter: LiveData<SaleFilter> = _selectedFilter

    private val _visibleSales =
        MutableLiveData<List<SalesReport>>(emptyList())

    val visibleSales: LiveData<List<SalesReport>> = _visibleSales

    init {
        refresh()
    }

    // Vuelve a leer las órdenes del mes actual desde SQLite
    fun refresh() {
        viewModelScope.launch {
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            allSales = try {
                repository.getOrders()
                    .filter { it.createdAt >= monthStart }
                    .map { it.toSalesReport() }
            } catch (exception: Exception) {
                emptyList()
            }

            applyFilter(_selectedFilter.value ?: SaleFilter.ALL)
        }
    }

    fun setFilter(filter: SaleFilter) {
        _selectedFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: SaleFilter) {
        _visibleSales.value = when (filter) {
            SaleFilter.ALL -> allSales

            SaleFilter.COMPLETED ->
                allSales.filter { it.status == SaleStatus.COMPLETED }

            SaleFilter.PENDING ->
                allSales.filter { it.status == SaleStatus.PENDING }
        }
    }

    fun getTotalAmount(): Int {
        // Solo las ventas completadas representan ingresos confirmados.
        return _visibleSales.value.orEmpty()
            .filter { it.status == SaleStatus.COMPLETED }
            .sumOf { it.amount }
    }

    fun getCompletedCount(): Int {
        return _visibleSales.value.orEmpty().count {
            it.status == SaleStatus.COMPLETED
        }
    }

    fun getPendingCount(): Int {
        return _visibleSales.value.orEmpty().count {
            it.status == SaleStatus.PENDING
        }
    }

    private fun StoredOrder.toSalesReport(): SalesReport {
        val units = items.sumOf { it.quantity }

        return SalesReport(
            id = orderNumber,
            date = dateFormat.format(Date(createdAt)),
            customer = customerName,
            products = if (units == 1) {
                "1 producto"
            } else {
                "$units productos"
            },
            paymentMethod = paymentMethod,
            amount = total,
            status = if (status == "DELIVERED") {
                SaleStatus.COMPLETED
            } else {
                SaleStatus.PENDING
            }
        )
    }
}
