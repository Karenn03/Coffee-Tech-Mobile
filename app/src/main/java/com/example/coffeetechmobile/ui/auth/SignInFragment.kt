package com.example.coffeetechmobile.ui.auth

import androidx.fragment.app.Fragment
import com.example.coffeetechmobile.R
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ir a registro
        view.findViewById<TextView>(R.id.sign_up_tab)
            .setOnClickListener {

                findNavController().navigate(
                    R.id.action_sign_in_to_sign_up
                )
            }

        // Ir a recuperación de contraseña
        view.findViewById<TextView>(R.id.forgot_password)
            .setOnClickListener {

                findNavController().navigate(
                    R.id.action_sign_in_to_forgot_password
                )
            }

        // Funcionalidad del icono para mostrar/ocultar la contraseña
        val passwordLayout = view.findViewById<TextInputLayout>(
            R.id.password_input_layout
        )

        val passwordEditText = view.findViewById<TextInputEditText>(
            R.id.password_edit_text
        )

        var passwordVisible = false

        passwordLayout.setEndIconOnClickListener {

            passwordVisible = !passwordVisible

            passwordEditText.transformationMethod =
                if (passwordVisible) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }

            passwordLayout.setEndIconDrawable(
                if (passwordVisible) {
                    R.drawable.ic_eye_off
                } else {
                    R.drawable.ic_eye
                }
            )

            passwordLayout.endIconContentDescription = getString(
                if (passwordVisible) {
                    R.string.hide_password
                } else {
                    R.string.show_password
                }
            )

            passwordEditText.setSelection(
                passwordEditText.text?.length ?: 0
            )
        }

        // Autenticación biométrica
        view.findViewById<View>(R.id.biometric_button)
            .setOnClickListener {
                authenticateWithBiometrics()
            }

    }

    private fun authenticateWithBiometrics() {

        val biometricManager = BiometricManager.from(requireContext())

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK

        when (biometricManager.canAuthenticate(authenticators)) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showBiometricMessage(
                    "Este dispositivo no tiene sensor biométrico"
                )
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showBiometricMessage(
                    "El sensor biométrico no está disponible"
                )
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showBiometricMessage(
                    "Primero configura una huella o reconocimiento facial en tu dispositivo"
                )
            }

            else -> {
                showBiometricMessage(
                    "No se puede utilizar la autenticación biométrica"
                )
            }
        }
    }

    private fun showBiometricPrompt() {

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

                    showBiometricMessage(
                        "Autenticación biométrica exitosa"
                    )

                    // Flujo de sesión
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    showBiometricMessage(
                        "Huella o rostro no reconocido"
                    )
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
                        showBiometricMessage(errString.toString())
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

    private fun showBiometricMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
