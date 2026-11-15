package com.example.coffeetechmobile.ui.buyer.address

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.location.DeliveryLocation
import com.example.coffeetechmobile.data.location.DeliveryLocationProvider
import com.example.coffeetechmobile.data.model.DeliveryAddress
import com.example.coffeetechmobile.data.repository.AddressRepository
import com.example.coffeetechmobile.data.repository.UserRepository
import com.example.coffeetechmobile.data.session.SessionManager
import kotlinx.coroutines.launch

// Estados de la libreta de direcciones
sealed interface AddressBookState {

    data object Idle : AddressBookState

    data object Locating : AddressBookState

    data class Candidate(
        val location: DeliveryLocation
    ) : AddressBookState

    data object Saved : AddressBookState

    data object Deleted : AddressBookState

    data class Error(
        @StringRes val messageRes: Int
    ) : AddressBookState
}

/**
 * Libreta de direcciones del comprador.
 *
 * Lista las direcciones guardadas, permite seleccionar una, eliminarla
 * y agregar nuevas usando la ubicación actual o el selector del mapa
 */
class AddressBookViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AddressRepository(application)
    private val userRepository = UserRepository(application)
    private val locationProvider = DeliveryLocationProvider(application)
    private val sessionManager = SessionManager(application)

    private val _addresses =
        MutableLiveData<List<DeliveryAddress>>(emptyList())

    val addresses: LiveData<List<DeliveryAddress>> = _addresses

    private val _selectedAddressId = MutableLiveData<Long?>()

    val selectedAddressId: LiveData<Long?> = _selectedAddressId

    // Dirección que se muestra en el home y en la pasarela
    private val _displayAddress = MutableLiveData<String?>()

    val displayAddress: LiveData<String?> = _displayAddress

    // Nombre de la dirección activa (Casa, Trabajo, etc.)
    private val _displayAddressLabel = MutableLiveData<String?>()

    val displayAddressLabel: LiveData<String?> = _displayAddressLabel

    private val _state = MutableLiveData<AddressBookState>(
        AddressBookState.Idle
    )

    val state: LiveData<AddressBookState> = _state

    // Carga la libreta del usuario con sesión activa
    fun loadAddresses() {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            val loaded = try {
                repository.getAddresses(userId)
            } catch (exception: Exception) {
                emptyList()
            }

            _addresses.value = loaded

            val selectedId = try {
                repository.getSelectedAddressId(userId)
            } catch (exception: Exception) {
                null
            }

            _selectedAddressId.value = selectedId

            val selectedAddress = loaded.firstOrNull {
                it.id == selectedId
            }?.address

            _displayAddressLabel.value = loaded.firstOrNull {
                it.id == selectedId
            }?.label

            _displayAddress.value = selectedAddress ?: try {
                userRepository
                    .getUser(userId)
                    ?.location
                    ?.takeIf { it.isNotBlank() }
            } catch (exception: Exception) {
                null
            }
        }
    }

    // Marca una dirección guardada como la dirección de entrega activa
    fun selectAddress(addressId: Long) {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            val selected = try {
                repository.selectAddress(userId, addressId)
            } catch (exception: Exception) {
                false
            }

            _state.value = if (selected) {
                _selectedAddressId.value = addressId

                val selectedAddress = _addresses.value
                    ?.firstOrNull { it.id == addressId }

                _displayAddress.value = selectedAddress?.address
                _displayAddressLabel.value = selectedAddress?.label

                AddressBookState.Saved
            } else {
                AddressBookState.Error(
                    R.string.address_select_error
                )
            }
        }
    }

    // Guarda una dirección nueva y la deja seleccionada
    fun saveAddress(
        label: String,
        location: DeliveryLocation
    ) {
        val userId = sessionManager.current()?.id ?: return
        val cleanLabel = label.trim().ifBlank {
            getApplication<Application>().getString(
                R.string.address_default_label
            )
        }

        viewModelScope.launch {
            val saved = try {
                repository.saveAddress(
                    userId = userId,
                    label = cleanLabel,
                    address = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } catch (exception: Exception) {
                null
            }

            if (saved == null) {
                _state.value = AddressBookState.Error(
                    R.string.address_save_error
                )
                return@launch
            }

            _selectedAddressId.value = saved.id
            _displayAddress.value = saved.address
            _displayAddressLabel.value = saved.label

            _addresses.value = try {
                repository.getAddresses(userId)
            } catch (exception: Exception) {
                _addresses.value.orEmpty()
            }

            _state.value = AddressBookState.Saved
        }
    }

    fun deleteAddress(addressId: Long) {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            val deleted = try {
                repository.deleteAddress(userId, addressId)
            } catch (exception: Exception) {
                false
            }

            if (!deleted) {
                _state.value = AddressBookState.Error(
                    R.string.address_delete_error
                )
                return@launch
            }

            _state.value = AddressBookState.Deleted

            loadAddresses()
        }
    }

    // Captura la ubicación actual para ofrecerla como dirección nueva
    fun captureCurrentLocation() {
        if (_state.value == AddressBookState.Locating) {
            return
        }

        if (sessionManager.current()?.id == null) {
            _state.value = AddressBookState.Error(
                R.string.address_save_error
            )
            return
        }

        _state.value = AddressBookState.Locating

        viewModelScope.launch {
            val location = locationProvider.findCurrentLocation()

            _state.value = if (location != null) {
                AddressBookState.Candidate(location)
            } else {
                AddressBookState.Error(
                    R.string.delivery_location_unavailable
                )
            }
        }
    }

    fun consumeState() {
        if (_state.value != AddressBookState.Locating) {
            _state.value = AddressBookState.Idle
        }
    }
}
