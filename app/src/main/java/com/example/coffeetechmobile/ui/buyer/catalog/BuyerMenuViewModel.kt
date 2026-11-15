package com.example.coffeetechmobile.ui.buyer.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.data.repository.ProductRepository
import kotlinx.coroutines.launch

// Carga el menú que el administrador y el vendedor mantienen en SQLite
class BuyerMenuViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    // Recarga el menú cada vez que el comprador vuelve a la pantalla
    fun loadProducts() {
        viewModelScope.launch {
            try {
                _products.value = repository.getAllProducts()
            } catch (exception: Exception) {
                _products.value = emptyList()
            }
        }
    }
}
