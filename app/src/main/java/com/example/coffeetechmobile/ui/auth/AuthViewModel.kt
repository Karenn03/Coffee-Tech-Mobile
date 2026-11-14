package com.example.coffeetechmobile.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.repository.AuthRepository
import com.example.coffeetechmobile.data.repository.AuthResult
import com.example.coffeetechmobile.data.session.SessionUser
import kotlinx.coroutines.launch

// Campos del formulario que pueden recibir un error de validación
enum class AuthField {
    NONE,
    NAME,
    EMAIL,
    PASSWORD,
    CONFIRM_PASSWORD,
    TERMS
}

// Estados que las pantallas de autenticación saben representar
sealed interface AuthUiState {

    // Sin operación en curso
    data object Idle : AuthUiState

    // Hay una consulta a SQLite o un hash en proceso
    data object Loading : AuthUiState

    // Error de validación o de negocio
    data class Error(
        val message: String,
        val field: AuthField = AuthField.NONE
    ) : AuthUiState

    // Login o registro correcto: la sesión ya quedó guardada
    data class SignedIn(val user: User) : AuthUiState

    // El correo existe y tiene huella habilitada
    data class BiometricReady(val email: String) : AuthUiState

    // El correo existe y puede pasar al formulario de nueva contraseña
    data class RecoveryEmailVerified(val email: String) : AuthUiState

    // La contraseña se actualizó correctamente
    data object PasswordReset : AuthUiState
}

/**
 * ViewModel compartido por las pantallas de login, registro y recuperación.
 *
 * Toda la lógica pesada (SQLite y PBKDF2) se ejecuta en Dispatchers.IO a través
 * del repositorio; la UI solo observa [state] y reacciona.
 */
class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    // Inicia sesión con correo y contraseña
    fun signIn(email: String, password: String) {
        val emailError = validateEmail(email)
        if (emailError != null) {
            _state.value = AuthUiState.Error(emailError, AuthField.EMAIL)
            return
        }

        if (password.isEmpty()) {
            _state.value = AuthUiState.Error(
                "Ingresa tu contraseña.",
                AuthField.PASSWORD
            )
            return
        }

        launch {
            when (val result = repository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.SignedIn(result.user)
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // Crea una cuenta de comprador y deja la sesión iniciada
    fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ) {
        validateName(fullName)?.let {
            _state.value = AuthUiState.Error(it, AuthField.NAME)
            return
        }

        validateEmail(email)?.let {
            _state.value = AuthUiState.Error(it, AuthField.EMAIL)
            return
        }

        validateNewPassword(password)?.let {
            _state.value = AuthUiState.Error(it, AuthField.PASSWORD)
            return
        }

        if (password != confirmPassword) {
            _state.value = AuthUiState.Error(
                "Las contraseñas no coinciden.",
                AuthField.CONFIRM_PASSWORD
            )
            return
        }

        if (!acceptedTerms) {
            _state.value = AuthUiState.Error(
                "Debes aceptar los términos y la política de datos.",
                AuthField.TERMS
            )
            return
        }

        launch {
            when (val result = repository.signUp(fullName, email, password)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.SignedIn(result.user)
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // Comprueba si el correo puede iniciar el proceso de recuperación
    fun checkRecoveryEmail(email: String) {
        validateEmail(email)?.let {
            _state.value = AuthUiState.Error(it, AuthField.EMAIL)
            return
        }

        launch {
            when (val result = repository.verifyRecoveryEmail(email)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.RecoveryEmailVerified(
                        result.user.email
                    )
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(
                        result.message,
                        AuthField.EMAIL
                    )
                }
            }
        }
    }

    // Guarda la nueva contraseña de la cuenta que viene del paso anterior
    fun resetPassword(
        email: String,
        newPassword: String,
        confirmPassword: String
    ) {
        validateNewPassword(newPassword)?.let {
            _state.value = AuthUiState.Error(it, AuthField.PASSWORD)
            return
        }

        if (newPassword != confirmPassword) {
            _state.value = AuthUiState.Error(
                "Las contraseñas no coinciden.",
                AuthField.CONFIRM_PASSWORD
            )
            return
        }

        launch {
            when (val result = repository.resetPassword(email, newPassword)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.PasswordReset
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // Verifica la cuenta antes de abrir el diálogo de huella
    fun prepareBiometricLogin(email: String) {
        validateEmail(email)?.let {
            _state.value = AuthUiState.Error(it, AuthField.EMAIL)
            return
        }

        launch {
            when (val result = repository.prepareBiometricSignIn(email)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.BiometricReady(
                        result.user.email
                    )
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(
                        result.message,
                        AuthField.EMAIL
                    )
                }
            }
        }
    }

    // Abre la sesión después de que BiometricPrompt confirmó la huella
    fun completeBiometricLogin(email: String) {
        launch {
            when (val result = repository.signInWithBiometrics(email)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.SignedIn(result.user)
                }

                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    // Devuelve la sesión persistida para el arranque de la aplicación
    fun currentSession(): SessionUser? = repository.currentSession()

    // Cierra la sesión activa.
    fun signOut() {
        repository.signOut()
    }

    /**
     * Limpia un estado de un solo uso (por ejemplo, BiometricReady)
     * para que no se repita al rotar la pantalla
     */
    fun consumeState() {
        if (_state.value != AuthUiState.Loading) {
            _state.value = AuthUiState.Idle
        }
    }

    // Ejecuta el bloque mostrando primero el estado de carga
    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            block()
        }
    }

    private fun validateName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Ingresa tu nombre completo."
            fullName.trim().length < 3 ->
                "El nombre debe tener al menos 3 caracteres."

            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        val value = email.trim()
        return when {
            value.isEmpty() -> "Ingresa tu correo electrónico."
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() ->
                "Ingresa un correo electrónico válido."

            else -> null
        }
    }

    private fun validateNewPassword(password: String): String? {
        return when {
            password.isEmpty() -> "Ingresa una contraseña."
            password.length < MIN_PASSWORD_LENGTH ->
                "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."

            else -> null
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
