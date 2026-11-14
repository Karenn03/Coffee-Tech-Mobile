package com.example.coffeetechmobile.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.navigation.RoleNavigator
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.Executor

/**
 * Pantalla de inicio de sesión
 *
 * Permite autenticarse con correo y contraseña o con huella dactilar. En el
 * segundo caso el correo identifica la cuenta y BiometricPrompt confirma la
 * identidad antes de restaurar la sesión
 */
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signInButton: MaterialButton
    private lateinit var biometricButton: MaterialButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupNavigation(view)
        setupPasswordToggle()
        setupListeners()
        observeAuthState()
    }

    private fun bindViews(view: View) {
        emailLayout = view.findViewById(R.id.email_input_layout)
        emailEditText = view.findViewById(R.id.email_edit_text)
        passwordLayout = view.findViewById(R.id.password_input_layout)
        passwordEditText = view.findViewById(R.id.password_edit_text)
        signInButton = view.findViewById(R.id.sign_in_button)
        biometricButton = view.findViewById(R.id.biometric_button)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<TextView>(R.id.sign_up_tab)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_sign_in_to_sign_up
                )
            }

        view.findViewById<TextView>(R.id.forgot_password)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_sign_in_to_forgot_password
                )
            }
    }

    private fun setupPasswordToggle() {
        passwordLayout.setupPasswordVisibilityToggle(passwordEditText)
    }

    private fun setupListeners() {
        signInButton.setOnClickListener {
            submitSignIn()
        }

        biometricButton.setOnClickListener {
            requestBiometricLogin()
        }

        // Los errores desaparecen en cuanto el usuario corrige el campo.
        emailEditText.doAfterTextChanged {
            emailLayout.clearError()
        }

        passwordEditText.doAfterTextChanged {
            passwordLayout.clearError()
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
                        "Bienvenido, ${state.user.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    RoleNavigator.navigateToRoleHome(
                        navController = findNavController(),
                        role = state.user.role
                    )
                }

                is AuthUiState.BiometricReady -> {
                    setLoading(false)
                    val email = state.email
                    // Evita repetir el prompt si la pantalla se recrea.
                    authViewModel.consumeState()
                    showBiometricPrompt(email)
                }

                // Estos estados pertenecen a otras pantallas de auth.
                is AuthUiState.RecoveryEmailVerified -> Unit
                AuthUiState.PasswordReset -> Unit
            }
        }
    }

    private fun submitSignIn() {
        emailLayout.clearError()
        passwordLayout.clearError()

        authViewModel.signIn(
            email = emailEditText.text?.toString().orEmpty(),
            password = passwordEditText.text?.toString().orEmpty()
        )
    }

    private fun requestBiometricLogin() {
        emailLayout.clearError()

        val email = emailEditText.text?.toString().orEmpty().trim()

        if (email.isEmpty()) {
            emailLayout.showError(
                "Ingresa tu correo para usar la huella."
            )
            return
        }

        // Primero se comprueba el hardware y luego la cuenta en SQLite.
        if (checkBiometricSupport()) {
            authViewModel.prepareBiometricLogin(email)
        }
    }

    // Verifica que el dispositivo tenga sensor y huellas configuradas
    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK

        return when (
            biometricManager.canAuthenticate(authenticators)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> true

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showMessage(
                    "Este dispositivo no tiene sensor biométrico."
                )
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showMessage(
                    "El sensor biométrico no está disponible."
                )
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showMessage(
                    "Primero configura una huella o reconocimiento facial en tu dispositivo."
                )
                false
            }

            else -> {
                showMessage(
                    "No se puede utilizar la autenticación biométrica."
                )
                false
            }
        }
    }

    /**
     * Abre el diálogo nativo de huella
     *
     * Solo se llama después de que el ViewModel confirmó que el
     * correo existe y tiene la huella habilitada
     */
    private fun showBiometricPrompt(email: String) {
        val executor: Executor =
            ContextCompat.getMainExecutor(requireContext())

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    // La huella fue validada por el sistema: se restaura la
                    // sesión de la cuenta asociada al correo ingresado
                    authViewModel.completeBiometricLogin(email)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showMessage("Huella o rostro no reconocido.")
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    if (
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        showMessage(errString.toString())
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Coffee Tech")
            .setSubtitle("Confirma tu identidad")
            .setDescription(
                "Utiliza tu huella o reconocimiento facial para continuar"
            )
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showError(state: AuthUiState.Error) {
        when (state.field) {
            AuthField.EMAIL -> emailLayout.showError(state.message)
            AuthField.PASSWORD -> passwordLayout.showError(state.message)
            else -> showMessage(state.message)
        }
    }

    private fun setLoading(loading: Boolean) {
        signInButton.isEnabled = !loading
        biometricButton.isEnabled = !loading
        signInButton.alpha = if (loading) 0.7f else 1f
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
