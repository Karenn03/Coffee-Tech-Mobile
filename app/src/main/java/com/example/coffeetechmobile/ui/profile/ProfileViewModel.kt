package com.example.coffeetechmobile.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.repository.UserRepository
import com.example.coffeetechmobile.data.repository.UserResult
import com.example.coffeetechmobile.data.session.SessionManager
import kotlinx.coroutines.launch

// Estado de la edición del perfil
sealed interface ProfileUpdateState {

    data object Idle : ProfileUpdateState

    data object Saving : ProfileUpdateState

    data object Success : ProfileUpdateState

    data class Error(val message: String) : ProfileUpdateState
}

/**
 * Perfil del usuario con sesión activa, leído y guardado en SQLite.
 */
class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val sessionManager = SessionManager(application)

    private val _profile = MutableLiveData<UserProfile?>()

    val profile: LiveData<UserProfile?> = _profile

    private val _updateState = MutableLiveData<ProfileUpdateState>(
        ProfileUpdateState.Idle
    )

    val updateState: LiveData<ProfileUpdateState> = _updateState

    fun loadProfile() {
        val userId = sessionManager.current()?.id ?: return

        viewModelScope.launch {
            val user = try {
                userRepository.getUser(userId)
            } catch (exception: Exception) {
                null
            }

            _profile.value = user?.toUserProfile()
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String
    ) {
        val userId = sessionManager.current()?.id ?: return

        if (_updateState.value == ProfileUpdateState.Saving) {
            return
        }

        _updateState.value = ProfileUpdateState.Saving

        viewModelScope.launch {
            when (
                val result = userRepository.updateProfile(
                    userId = userId,
                    fullName = name,
                    email = email,
                    phone = phone
                )
            ) {
                is UserResult.Success -> {
                    sessionManager.save(result.user)

                    _profile.value = result.user.toUserProfile()
                    _updateState.value = ProfileUpdateState.Success
                }

                is UserResult.Error -> {
                    _updateState.value = ProfileUpdateState.Error(
                        result.message
                    )
                }
            }
        }
    }

    fun consumeUpdateState() {
        if (_updateState.value != ProfileUpdateState.Saving) {
            _updateState.value = ProfileUpdateState.Idle
        }
    }

    private fun User.toUserProfile(): UserProfile {
        return UserProfile(
            name = fullName,
            email = email,
            phone = phone,
            role = role,
            document = document,
            location = location
        )
    }
}
