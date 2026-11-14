package com.example.coffeetechmobile.ui.auth

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.example.coffeetechmobile.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Muestra u oculta el mensaje de error de un campo Material
fun TextInputLayout.showError(message: String?) {
    error = message
    isErrorEnabled = !message.isNullOrBlank()
}

// Limpia el error de un campo
fun TextInputLayout.clearError() {
    showError(null)
}

/**
 * Configura el icono del ojo para alternar la visibilidad de una contraseña.
 *
 * Se extrajo de las pantallas para reutilizarlo en login, registro y
 * restablecimiento sin duplicar la lógica.
 */
fun TextInputLayout.setupPasswordVisibilityToggle(
    editText: TextInputEditText
) {
    var passwordVisible = false

    setEndIconOnClickListener {
        passwordVisible = !passwordVisible

        editText.transformationMethod = if (passwordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }

        setEndIconDrawable(
            if (passwordVisible) {
                R.drawable.ic_eye_off
            } else {
                R.drawable.ic_eye
            }
        )

        endIconContentDescription = context.getString(
            if (passwordVisible) {
                R.string.hide_password
            } else {
                R.string.show_password
            }
        )

        editText.setSelection(editText.text?.length ?: 0)
    }
}
