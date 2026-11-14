package com.example.coffeetechmobile.ui.manager.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.data.repository.ProductRepository
import kotlinx.coroutines.launch

/**
 * Mantiene el listado de productos del panel del administrador.
 *
 * La lista se recarga desde SQLite después de crear, editar o eliminar, para
 * que el RecyclerView y las tarjetas de categorías queden sincronizados.
 */
class ProductManagementViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    // Recarga el menú desde SQLite
    fun loadProducts() {
        viewModelScope.launch {
            try {
                _products.value = repository.getAllProducts()
            } catch (exception: Exception) {
                _message.value = "No se pudieron cargar los productos."
            }
        }
    }

    // Elimina un producto y actualiza la pantalla
    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            try {
                if (repository.deleteProduct(productId)) {
                    _message.value = "Producto eliminado correctamente."
                    loadProducts()
                } else {
                    _message.value = "No se pudo eliminar el producto."
                }
            } catch (exception: Exception) {
                _message.value = "No se pudo eliminar el producto."
            }
        }
    }

    // Limpia el mensaje para que no se repita al rotar la pantalla
    fun consumeMessage() {
        _message.value = null
    }
}
