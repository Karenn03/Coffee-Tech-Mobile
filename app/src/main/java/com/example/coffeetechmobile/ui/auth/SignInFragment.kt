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

    }
}
