package com.example.coffeetechmobile.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.navigation.RoleNavigator
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Pantalla de registro
 *
 * Crea cuentas de comprador por defecto, valida los campos y deja
 * la sesión iniciada para entrar directamente al catálogo
 */
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var termsCheckBox: MaterialCheckBox
    private lateinit var signUpButton: MaterialButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupNavigation(view)
        setupPasswordToggles()
        setupListeners()
        observeAuthState()
    }

    private fun bindViews(view: View) {
        fullNameLayout = view.findViewById(R.id.full_name_input_layout)
        fullNameEditText = view.findViewById(R.id.full_name_edit_text)
        emailLayout = view.findViewById(R.id.email_input_layout)
        emailEditText = view.findViewById(R.id.email_edit_text)
        passwordLayout = view.findViewById(R.id.password_input_layout)
        passwordEditText = view.findViewById(R.id.password_edit_text)
        confirmPasswordLayout =
            view.findViewById(R.id.confirm_password_input_layout)
        confirmPasswordEditText =
            view.findViewById(R.id.confirm_password_edit_text)
        termsCheckBox = view.findViewById(R.id.terms_checkbox)
        signUpButton = view.findViewById(R.id.sign_up_button)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<TextView>(R.id.sign_in_tab)
            .setOnClickListener {
                findNavController().popBackStack(
                    R.id.sign_in_fragment,
                    false
                )
            }
    }

    private fun setupPasswordToggles() {
        passwordLayout.setupPasswordVisibilityToggle(passwordEditText)
        confirmPasswordLayout.setupPasswordVisibilityToggle(
            confirmPasswordEditText
        )
    }

    private fun setupListeners() {
        signUpButton.setOnClickListener {
            submitSignUp()
        }

        fullNameEditText.doAfterTextChanged {
            fullNameLayout.clearError()
        }

        emailEditText.doAfterTextChanged {
            emailLayout.clearError()
        }

        passwordEditText.doAfterTextChanged {
            passwordLayout.clearError()
        }

        confirmPasswordEditText.doAfterTextChanged {
            confirmPasswordLayout.clearError()
        }
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

                is AuthUiState.SignedIn -> {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Cuenta creada. Bienvenido, ${state.user.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    RoleNavigator.navigateToRoleHome(
                        navController = findNavController(),
                        role = state.user.role
                    )
                }

                // Estados de otras pantallas de auth
                is AuthUiState.BiometricReady -> Unit
                is AuthUiState.RecoveryEmailVerified -> Unit
                AuthUiState.PasswordReset -> Unit
            }
        }
    }

    private fun submitSignUp() {
        fullNameLayout.clearError()
        emailLayout.clearError()
        passwordLayout.clearError()
        confirmPasswordLayout.clearError()

        authViewModel.signUp(
            fullName = fullNameEditText.text?.toString().orEmpty(),
            email = emailEditText.text?.toString().orEmpty(),
            password = passwordEditText.text?.toString().orEmpty(),
            confirmPassword =
                confirmPasswordEditText.text?.toString().orEmpty(),
            acceptedTerms = termsCheckBox.isChecked
        )
    }

    private fun showError(state: AuthUiState.Error) {
        when (state.field) {
            AuthField.NAME -> fullNameLayout.showError(state.message)
            AuthField.EMAIL -> emailLayout.showError(state.message)
            AuthField.PASSWORD -> passwordLayout.showError(state.message)
            AuthField.CONFIRM_PASSWORD ->
                confirmPasswordLayout.showError(state.message)

            AuthField.TERMS, AuthField.NONE ->
                Toast.makeText(
                    requireContext(),
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        signUpButton.isEnabled = !loading
        signUpButton.alpha = if (loading) 0.7f else 1f
    }
}
