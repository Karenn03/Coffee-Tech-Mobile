package com.example.coffeetechmobile.ui.auth

import androidx.fragment.app.Fragment
import com.example.coffeetechmobile.R
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sign_in_tab)
            .setOnClickListener {

                findNavController().popBackStack(
                    R.id.sign_in_fragment,
                    false
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

        // Funcionalidad para mostrar/ocultar la contraseña de confirmación
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(
            R.id.confirm_password_input_layout
        )

        val confirmPasswordEditText = view.findViewById<TextInputEditText>(
            R.id.confirm_password_edit_text
        )

        var confirmPasswordVisible = false

        confirmPasswordLayout.setEndIconOnClickListener {

            confirmPasswordVisible = !confirmPasswordVisible

            confirmPasswordEditText.transformationMethod =
                if (confirmPasswordVisible) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }

            confirmPasswordLayout.setEndIconDrawable(
                if (confirmPasswordVisible) {
                    R.drawable.ic_eye_off
                } else {
                    R.drawable.ic_eye
                }
            )

            confirmPasswordLayout.endIconContentDescription = getString(
                if (confirmPasswordVisible) {
                    R.string.hide_password
                } else {
                    R.string.show_password
                }
            )

            confirmPasswordEditText.setSelection(
                confirmPasswordEditText.text?.length ?: 0
            )
        }

    }
}
