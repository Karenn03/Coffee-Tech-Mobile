package com.example.coffeetechmobile.ui.manager.users

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Mantiene la lista de usuarios del panel del administrador.
 *
 * La lista vive en LiveData para que el RecyclerView y las tarjetas de
 * estadísticas se actualicen solas después de crear, editar o eliminar.
 */
class UserManagementViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    // Recarga la lista desde SQLite
    fun loadUsers() {
        viewModelScope.launch {
            try {
                _users.value = repository.getAllUsers()
            } catch (exception: Exception) {
                _message.value = "No se pudieron cargar los usuarios."
            }
        }
    }
    // Elimina una cuenta y refresca el listado
    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                if (repository.deleteUser(userId)) {
                    _message.value = "Usuario eliminado correctamente."
                    loadUsers()
                } else {
                    _message.value = "No se pudo eliminar el usuario."
                }
            } catch (exception: Exception) {
                _message.value = "No se pudo eliminar el usuario."
            }
        }
    }

    // Limpia el mensaje para que no se repita al rotar la pantalla
    fun consumeMessage() {
        _message.value = null
    }
}
