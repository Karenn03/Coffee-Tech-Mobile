package com.example.coffeetechmobile.ui.buyer.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.repository.CheckoutResult
import com.example.coffeetechmobile.data.repository.OrderRepository
import kotlinx.coroutines.launch

// Estados del proceso de confirmación de una compra
sealed interface CheckoutState {

    data object Idle : CheckoutState

    data object Processing : CheckoutState

    data class Success(
        val orderNumber: String,
        val confirmationCode: String,
        val paymentStatus: String,
        val paymentMethod: String,
        val total: Int
    ) : CheckoutState

    data class Error(val message: String) : CheckoutState
}

/**
 * Crea la orden a partir del carrito persistente
 *
 * La orden y el vaciado del carrito ocurren en una sola transacción de SQLite
 */
class CheckoutViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = OrderRepository(application)

    private val _state = MutableLiveData<CheckoutState>(
        CheckoutState.Idle
    )

    val state: LiveData<CheckoutState> = _state

    fun confirmOrder(paymentMethod: String) {
        if (_state.value == CheckoutState.Processing) {
            return
        }

        _state.value = CheckoutState.Processing

        viewModelScope.launch {
            _state.value = when (
                val result = repository.checkout(paymentMethod)
            ) {
                is CheckoutResult.Success ->
                    CheckoutState.Success(
                        orderNumber = result.orderNumber,
                        confirmationCode = result.confirmationCode,
                        paymentStatus = result.paymentStatus,
                        paymentMethod = result.paymentMethod,
                        total = result.total
                    )

                is CheckoutResult.Error ->
                    CheckoutState.Error(result.message)
            }
        }
    }

    fun consumeResult() {
        _state.value = CheckoutState.Idle
    }
}
