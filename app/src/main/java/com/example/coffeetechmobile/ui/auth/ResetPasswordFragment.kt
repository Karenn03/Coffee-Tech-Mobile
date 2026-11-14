package com.example.coffeetechmobile.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Segundo paso de la recuperación de contraseña
// Recibe el correo ya validado y guarda un nuevo hash PBKDF2 en SQLite
class ResetPasswordFragment :
    Fragment(R.layout.fragment_reset_password) {

    companion object {
        const val ARG_EMAIL = "recovery_email"
    }

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var email: String
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        email = requireArguments().getString(ARG_EMAIL).orEmpty()

        view.findViewById<TextView>(R.id.reset_password_email).text = email

        passwordLayout = view.findViewById(
            R.id.new_password_input_layout
        )
        passwordEditText = view.findViewById(
            R.id.new_password_edit_text
        )
        confirmPasswordLayout = view.findViewById(
            R.id.confirm_new_password_input_layout
        )
        confirmPasswordEditText = view.findViewById(
            R.id.confirm_new_password_edit_text
        )
        saveButton = view.findViewById(R.id.save_password_button)

        passwordLayout.setupPasswordVisibilityToggle(passwordEditText)
        confirmPasswordLayout.setupPasswordVisibilityToggle(
            confirmPasswordEditText
        )

        view.findViewById<View>(R.id.back_to_sign_in_container)
            .setOnClickListener {
                findNavController().popBackStack(
                    R.id.sign_in_fragment,
                    false
                )
            }

        saveButton.setOnClickListener {
            submitNewPassword()
        }

        passwordEditText.doAfterTextChanged {
            passwordLayout.clearError()
        }

        confirmPasswordEditText.doAfterTextChanged {
            confirmPasswordLayout.clearError()
        }

        observeAuthState()
    }

    private fun observeAuthState() {
        authViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthUiState.Idle -> Unit
                AuthUiState.Loading -> setLoading(true)

                is AuthUiState.Error -> {
                    setLoading(false)
                    showError(state)
                }

                AuthUiState.PasswordReset -> {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Contraseña actualizada. Ya puedes iniciar sesión.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Regresa al login limpiando el flujo de recuperación.
                    val options = NavOptions.Builder()
                        .setPopUpTo(R.id.auth_nav_graph, true)
                        .build()

                    findNavController().navigate(
                        R.id.sign_in_fragment,
                        null,
                        options
                    )
                }

                // Estados de otras pantallas de auth.
                is AuthUiState.SignedIn -> Unit
                is AuthUiState.BiometricReady -> Unit
                is AuthUiState.RecoveryEmailVerified -> Unit
            }
        }
    }

    private fun submitNewPassword() {
        passwordLayout.clearError()
        confirmPasswordLayout.clearError()

        authViewModel.resetPassword(
            email = email,
            newPassword = passwordEditText.text?.toString().orEmpty(),
            confirmPassword =
                confirmPasswordEditText.text?.toString().orEmpty()
        )
    }

    private fun showError(state: AuthUiState.Error) {
        when (state.field) {
            AuthField.PASSWORD -> passwordLayout.showError(state.message)
            AuthField.CONFIRM_PASSWORD ->
                confirmPasswordLayout.showError(state.message)

            else -> Toast.makeText(
                requireContext(),
                state.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        saveButton.isEnabled = !loading
        saveButton.alpha = if (loading) 0.7f else 1f
    }
}
