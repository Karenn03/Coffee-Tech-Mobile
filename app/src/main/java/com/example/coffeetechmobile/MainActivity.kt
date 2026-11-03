package com.example.coffeetechmobile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.sign_up_scroll)
        ) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Funcionalidad del icono para mostrar/ocultar la contraseña
        val passwordLayout = findViewById<TextInputLayout>(
            R.id.password_input_layout
        )

        val passwordEditText = findViewById<TextInputEditText>(
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

        val confirmPasswordLayout = findViewById<TextInputLayout>(
            R.id.confirm_password_input_layout
        )

        val confirmPasswordEditText = findViewById<TextInputEditText>(
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
