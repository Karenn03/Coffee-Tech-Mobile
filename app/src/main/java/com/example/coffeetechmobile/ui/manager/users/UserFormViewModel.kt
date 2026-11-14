package com.example.coffeetechmobile.ui.manager.users

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.repository.UserInput
import com.example.coffeetechmobile.data.repository.UserRepository
import com.example.coffeetechmobile.data.repository.UserResult
import kotlinx.coroutines.launch

// Estados del formulario de creación y edición de usuarios
sealed interface UserFormState {

    data object Idle : UserFormState

    data object Loading : UserFormState

    data class Success(
        val user: User,
        val temporaryPassword: String? = null
    ) : UserFormState

    data class Error(val message: String) : UserFormState
}

// Ejecuta el alta y la edición de usuarios contra SQLite
class UserFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _state = MutableLiveData<UserFormState>(UserFormState.Idle)
    val state: LiveData<UserFormState> = _state

    fun createUser(input: UserInput) {
        save {
            repository.createUser(input)
        }
    }

    fun updateUser(userId: Long, input: UserInput) {
        save {
            repository.updateUser(userId, input)
        }
    }

    // Limpia el resultado para no repetir el diálogo al rotar la pantalla
    fun consumeState() {
        if (_state.value != UserFormState.Loading) {
            _state.value = UserFormState.Idle
        }
    }

    private fun save(action: suspend () -> UserResult) {
        viewModelScope.launch {
            _state.value = UserFormState.Loading

            try {
                when (val result = action()) {
                    is UserResult.Success -> {
                        _state.value = UserFormState.Success(
                            user = result.user,
                            temporaryPassword = result.temporaryPassword
                        )
                    }

                    is UserResult.Error -> {
                        _state.value = UserFormState.Error(result.message)
                    }
                }
            } catch (exception: Exception) {
                _state.value = UserFormState.Error(
                    "No se pudo guardar el usuario. Intenta de nuevo."
                )
            }
        }
    }
}
