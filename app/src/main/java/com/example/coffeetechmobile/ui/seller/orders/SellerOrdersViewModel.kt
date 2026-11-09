package com.example.coffeetechmobile.ui.seller.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coffeetechmobile.R

class SellerOrdersViewModel : ViewModel() {

    private val allOrders = listOf(
        SellerOrder(
            id = "CT-105",
            date = "08/11/2026 · 11:35",
            customer = "Cliente de prueba 01",
            products = listOf(
                SellerOrderProduct(
                    R.id.product_iced_caramel, 1
                ),
                SellerOrderProduct(
                    R.id.product_muffin_red_velvet, 2
                )
            ),
            totalUnits = 3,
            deliveryType = "Para llevar",
            paymentMethod = "PSE",
            total = 17000,
            status = SellerOrderStatus.PREPARING,
            customerNote = "Con poco hielo y los muffins tibios, por favor."
        ),
        SellerOrder(
            id = "CT-104",
            date = "08/11/2026 · 11:18",
            customer = "Cliente de prueba 02",
            products = listOf(
                SellerOrderProduct(
                    R.id.product_frappe_oreo, 2
                )
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
                SellerOrderProduct(
                    R.id.product_iced_macchiato, 1
                ),
                SellerOrderProduct(
                    R.id.product_muffin_red_velvet, 1
                )
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
                SellerOrderProduct(
                    R.id.product_iced_caramel, 1
                )
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
                SellerOrderProduct(
                    R.id.product_frappe_oreo, 1
                ),
                SellerOrderProduct(
                    R.id.product_muffin_red_velvet, 1
                )
            ),
            totalUnits = 2,
            deliveryType = "Domicilio",
            paymentMethod = "Daviplata",
            total = 13000,
            status = SellerOrderStatus.PREPARING,
            customerNote = "Entregar las bebidas bien frías."
        ),
        SellerOrder(
            id = "CT-100",
            date = "08/11/2026 · 09:42",
            customer = "Cliente de prueba 06",
            products = listOf(
                SellerOrderProduct(
                    R.id.product_iced_macchiato, 2
                )
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
        get() = countByStatus(SellerOrderStatus.DELIVERED)

    fun countByStatus(status: SellerOrderStatus): Int {
        return allOrders.count {
            it.status == status
        }
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
