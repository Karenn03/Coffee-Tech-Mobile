package com.example.coffeetechmobile.ui.seller.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SellerOrdersViewModel : ViewModel() {

    private val allOrders = listOf(
        SellerOrder(
            id = "CT-105",
            date = "08/11/2026 · 11:35",
            customer = "Cliente de prueba 01",
            products = listOf(
                "1 × Iced Caramel Latte",
                "2 × Muffin Red Velvet"
            ),
            totalUnits = 3,
            deliveryType = "Para llevar",
            paymentMethod = "PSE",
            total = 17000,
            status = SellerOrderStatus.PREPARING
        ),
        SellerOrder(
            id = "CT-104",
            date = "08/11/2026 · 11:18",
            customer = "Cliente de prueba 02",
            products = listOf(
                "2 × Frappé Oreo"
            ),
            totalUnits = 2,
            deliveryType = "Domicilio",
            paymentMethod = "Nequi",
            total = 16000,
            status = SellerOrderStatus.RECEIVED
        ),
        SellerOrder(
            id = "CT-103",
            date = "08/11/2026 · 10:52",
            customer = "Cliente de prueba 03",
            products = listOf(
                "1 × Iced Macchiato",
                "1 × Muffin Red Velvet"
            ),
            totalUnits = 2,
            deliveryType = "Recoger en tienda",
            paymentMethod = "Tarjeta",
            total = 11500,
            status = SellerOrderStatus.READY
        ),
        SellerOrder(
            id = "CT-102",
            date = "08/11/2026 · 10:40",
            customer = "Cliente de prueba 04",
            products = listOf(
                "1 × Iced Caramel Latte"
            ),
            totalUnits = 1,
            deliveryType = "Para llevar",
            paymentMethod = "Efectivo",
            total = 7000,
            status = SellerOrderStatus.DELIVERED
        ),
        SellerOrder(
            id = "CT-101",
            date = "08/11/2026 · 10:15",
            customer = "Cliente de prueba 05",
            products = listOf(
                "1 × Frappé Oreo",
                "1 × Muffin Red Velvet"
            ),
            totalUnits = 2,
            deliveryType = "Domicilio",
            paymentMethod = "Daviplata",
            total = 13000,
            status = SellerOrderStatus.PREPARING
        ),
        SellerOrder(
            id = "CT-100",
            date = "08/11/2026 · 09:42",
            customer = "Cliente de prueba 06",
            products = listOf(
                "2 × Iced Macchiato"
            ),
            totalUnits = 2,
            deliveryType = "Para llevar",
            paymentMethod = "PSE",
            total = 13000,
            status = SellerOrderStatus.DELIVERED
        )
    )

    private val _selectedFilter =
        MutableLiveData(SellerOrderFilter.ALL)

    val selectedFilter: LiveData<SellerOrderFilter> =
        _selectedFilter

    private val _visibleOrders =
        MutableLiveData(allOrders)

    val visibleOrders: LiveData<List<SellerOrder>> =
        _visibleOrders

    val activeCount: Int
        get() = allOrders.count {
            it.status != SellerOrderStatus.DELIVERED
        }

    val deliveredCount: Int
        get() = allOrders.count {
            it.status == SellerOrderStatus.DELIVERED
        }

    fun setFilter(filter: SellerOrderFilter) {
        _selectedFilter.value = filter

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
}
