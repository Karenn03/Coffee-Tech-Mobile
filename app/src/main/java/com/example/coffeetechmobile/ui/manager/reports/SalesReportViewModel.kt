package com.example.coffeetechmobile.ui.manager.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SalesReportViewModel : ViewModel() {

    private val sales = listOf(
        SalesReport(
            id = "CT-001",
            date = "08/11/2026 · 10:32",
            customer = "Cliente de prueba 01",
            products = "2 productos",
            paymentMethod = "PSE",
            amount = 22000,
            status = SaleStatus.COMPLETED
        ),
        SalesReport(
            id = "CT-002",
            date = "08/11/2026 · 09:45",
            customer = "Cliente de prueba 02",
            products = "3 productos",
            paymentMethod = "Nequi",
            amount = 31500,
            status = SaleStatus.COMPLETED
        ),
        SalesReport(
            id = "CT-003",
            date = "07/11/2026 · 16:20",
            customer = "Cliente de prueba 03",
            products = "1 producto",
            paymentMethod = "Contra entrega",
            amount = 11500,
            status = SaleStatus.PENDING
        ),
        SalesReport(
            id = "CT-004",
            date = "07/11/2026 · 14:15",
            customer = "Cliente de prueba 04",
            products = "4 productos",
            paymentMethod = "Tarjeta",
            amount = 46000,
            status = SaleStatus.COMPLETED
        ),
        SalesReport(
            id = "CT-005",
            date = "06/11/2026 · 11:05",
            customer = "Cliente de prueba 05",
            products = "2 productos",
            paymentMethod = "PSE",
            amount = 18000,
            status = SaleStatus.PENDING
        ),
        SalesReport(
            id = "CT-006",
            date = "06/11/2026 · 08:40",
            customer = "Cliente de prueba 06",
            products = "2 productos",
            paymentMethod = "Daviplata",
            amount = 24500,
            status = SaleStatus.COMPLETED
        )
    )

    private val _selectedFilter =
        MutableLiveData(SaleFilter.ALL)

    val selectedFilter: LiveData<SaleFilter> = _selectedFilter

    private val _visibleSales =
        MutableLiveData(sales)

    val visibleSales: LiveData<List<SalesReport>> = _visibleSales

    fun setFilter(filter: SaleFilter) {
        _selectedFilter.value = filter

        _visibleSales.value = when (filter) {
            SaleFilter.ALL -> sales

            SaleFilter.COMPLETED ->
                sales.filter { it.status == SaleStatus.COMPLETED }

            SaleFilter.PENDING ->
                sales.filter { it.status == SaleStatus.PENDING }
        }
    }

    fun getTotalAmount(): Int {
        // Solo las ventas completadas representan ingresos.
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
}
