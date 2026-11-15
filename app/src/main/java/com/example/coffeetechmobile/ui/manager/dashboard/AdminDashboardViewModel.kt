package com.example.coffeetechmobile.ui.manager.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.repository.OrderRepository
import kotlinx.coroutines.launch

// Órdenes reales usadas por el dashboard del administrador
class AdminDashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = OrderRepository(application)

    private val _orders =
        MutableLiveData<List<StoredOrder>>(emptyList())

    val orders: LiveData<List<StoredOrder>> = _orders

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _orders.value = try {
                repository.getOrders()
            } catch (exception: Exception) {
                emptyList()
            }
        }
    }
}
