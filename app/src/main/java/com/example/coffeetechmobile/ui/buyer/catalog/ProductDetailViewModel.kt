package com.example.coffeetechmobile.ui.buyer.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.data.repository.ProductRepository
import kotlinx.coroutines.launch

// Estados de carga del detalle de producto
sealed interface ProductDetailState {

    data object Loading : ProductDetailState

    data class Loaded(val product: Product) : ProductDetailState

    data object NotFound : ProductDetailState
}

// Carga un producto puntual desde SQLite para la pantalla de detalle
class ProductDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _state = MutableLiveData<ProductDetailState>(
        ProductDetailState.Loading
    )
    val state: LiveData<ProductDetailState> = _state

    fun load(productId: Long) {
        viewModelScope.launch {
            val product = try {
                repository.getProduct(productId)
            } catch (exception: Exception) {
                null
            }

            _state.value = if (product != null) {
                ProductDetailState.Loaded(product)
            } else {
                ProductDetailState.NotFound
            }
        }
    }
}
