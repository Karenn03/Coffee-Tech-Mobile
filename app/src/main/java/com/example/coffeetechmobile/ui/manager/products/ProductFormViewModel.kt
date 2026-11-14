package com.example.coffeetechmobile.ui.manager.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.data.repository.ProductInput
import com.example.coffeetechmobile.data.repository.ProductRepository
import com.example.coffeetechmobile.data.repository.ProductResult
import kotlinx.coroutines.launch

// Estados del formulario de productos
sealed interface ProductFormState {

    data object Idle : ProductFormState

    data object Loading : ProductFormState

    data class Success(val product: Product) : ProductFormState

    data class Error(val message: String) : ProductFormState
}

// Ejecuta el alta y la edición de productos contra SQLite
class ProductFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _state = MutableLiveData<ProductFormState>(ProductFormState.Idle)
    val state: LiveData<ProductFormState> = _state

    fun createProduct(input: ProductInput) {
        save {
            repository.createProduct(input)
        }
    }

    fun updateProduct(productId: Long, input: ProductInput) {
        save {
            repository.updateProduct(productId, input)
        }
    }

    // Limpia el resultado para no repetir el Toast al rotar la pantalla
    fun consumeState() {
        if (_state.value != ProductFormState.Loading) {
            _state.value = ProductFormState.Idle
        }
    }

    private fun save(action: suspend () -> ProductResult) {
        viewModelScope.launch {
            _state.value = ProductFormState.Loading

            try {
                when (val result = action()) {
                    is ProductResult.Success -> {
                        _state.value = ProductFormState.Success(result.product)
                    }

                    is ProductResult.Error -> {
                        _state.value = ProductFormState.Error(result.message)
                    }
                }
            } catch (exception: Exception) {
                _state.value = ProductFormState.Error(
                    "No se pudo guardar el producto. Intenta de nuevo."
                )
            }
        }
    }
}
