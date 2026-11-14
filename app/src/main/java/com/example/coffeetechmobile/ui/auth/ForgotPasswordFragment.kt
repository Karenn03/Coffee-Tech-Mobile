package com.example.coffeetechmobile.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Primer paso de la recuperación de contraseña
 *
 * Valida que el correo exista en SQLite y luego navega al
 * formulario donde se define la nueva contraseña
 */
class ForgotPasswordFragment :
    Fragment(R.layout.fragment_forgot_password) {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var resetButton: MaterialButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        emailLayout = view.findViewById(
            R.id.recovery_email_input_layout
        )
        emailEditText = view.findViewById(
            R.id.recovery_email_edit_text
        )
        resetButton = view.findViewById(R.id.reset_password_button)

        view.findViewById<View>(R.id.back_to_sign_in_container)
            .setOnClickListener {
                findNavController().popBackStack(
                    R.id.sign_in_fragment,
                    false
                )
            }

        resetButton.setOnClickListener {
            submitEmail()
        }

        emailEditText.doAfterTextChanged {
            emailLayout.clearError()
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
                    emailLayout.showError(state.message)
                }

                is AuthUiState.RecoveryEmailVerified -> {
                    setLoading(false)
                    val email = state.email
                    // Consume el evento antes de navegar para no repetirlo.
                    authViewModel.consumeState()

                    findNavController().navigate(
                        R.id.action_forgot_password_to_reset_password,
                        bundleOf(
                            ResetPasswordFragment.ARG_EMAIL to email
                        )
                    )
                }

                // Estados que pertenecen al login o al registro.
                is AuthUiState.SignedIn -> Unit
                is AuthUiState.BiometricReady -> Unit
                AuthUiState.PasswordReset -> Unit
            }
        }
    }

    private fun submitEmail() {
        emailLayout.clearError()

        authViewModel.checkRecoveryEmail(
            email = emailEditText.text?.toString().orEmpty()
        )
    }

    private fun setLoading(loading: Boolean) {
        resetButton.isEnabled = !loading
        resetButton.alpha = if (loading) 0.7f else 1f
    }
}
