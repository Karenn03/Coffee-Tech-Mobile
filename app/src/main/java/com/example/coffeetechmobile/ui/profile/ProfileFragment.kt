package com.example.coffeetechmobile.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.session.SessionManager

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    companion object {
        const val ARG_ROLE = "profile_role"

        fun createArguments(role: UserRole): Bundle {
            return bundleOf(ARG_ROLE to role.name)
        }
    }

    private val viewModel: ProfileViewModel by viewModels()

    // Controla si el perfil está en modo edición
    private var isEditing = false

    private val userRole: UserRole
        get() {
            val value = arguments?.getString(ARG_ROLE)

            return UserRole.entries.firstOrNull {
                it.name == value
            } ?: UserRole.BUYER
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(
            R.id.profile_back_button
        ).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(
            R.id.profile_edit_button
        ).setOnClickListener {
            if (isEditing) {
                cancelEditing(view)
            } else {
                startEditing(view)
            }
        }

        view.findViewById<View>(
            R.id.profile_save_button
        ).setOnClickListener {
            saveProfileChanges(view)
        }

        view.findViewById<View>(
            R.id.profile_logout_button
        ).setOnClickListener {
            showLogoutConfirmation()
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            bindProfile(view, profile)
        }

        viewModel.loadProfile(userRole)
    }


    private fun startEditing(root: View) {
        val profile = viewModel.profile.value ?: return

        root.findViewById<EditText>(
            R.id.profile_name_input
        ).setText(profile.name)

        root.findViewById<EditText>(
            R.id.profile_email_input
        ).setText(profile.email)

        root.findViewById<EditText>(
            R.id.profile_phone_input
        ).setText(profile.phone)

        setEditingMode(root, true)
    }

    private fun cancelEditing(root: View) {
        setEditingMode(root, false)
        hideKeyboard(root)

        viewModel.profile.value?.let { profile ->
            bindProfile(root, profile)
        }
    }

    private fun setEditingMode(
        root: View,
        editing: Boolean
    ) {
        isEditing = editing

        val fields = listOf(
            R.id.profile_full_name to R.id.profile_name_input,
            R.id.profile_email to R.id.profile_email_input,
            R.id.profile_phone to R.id.profile_phone_input
        )

        fields.forEach { (textId, inputId) ->
            root.findViewById<TextView>(textId).visibility =
                if (editing) View.GONE else View.VISIBLE

            root.findViewById<EditText>(inputId).visibility =
                if (editing) View.VISIBLE else View.GONE
        }

        root.findViewById<View>(
            R.id.profile_save_button
        ).visibility = if (editing) View.VISIBLE else View.GONE

        root.findViewById<TextView>(
            R.id.profile_edit_button
        ).text = if (editing) "Cancelar" else "Editar"
    }

    private fun saveProfileChanges(root: View) {
        val nameInput = root.findViewById<EditText>(
            R.id.profile_name_input
        )

        val emailInput = root.findViewById<EditText>(
            R.id.profile_email_input
        )

        val phoneInput = root.findViewById<EditText>(
            R.id.profile_phone_input
        )

        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        when {
            name.isBlank() -> {
                nameInput.error = "Ingresa tu nombre"
                nameInput.requestFocus()
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Ingresa un correo válido"
                emailInput.requestFocus()
                return
            }

            phone.isBlank() -> {
                phoneInput.error = "Ingresa tu teléfono"
                phoneInput.requestFocus()
                return
            }
        }

        val updated = viewModel.updateProfile(
            name = name,
            email = email,
            phone = phone
        )

        if (updated) {
            setEditingMode(root, false)
            hideKeyboard(root)

            Toast.makeText(
                requireContext(),
                "Perfil actualizado correctamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideKeyboard(root: View) {
        val inputManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager

        inputManager.hideSoftInputFromWindow(
            root.windowToken,
            0
        )

        root.findFocus()?.clearFocus()
    }

    private fun bindProfile(
        root: View,
        profile: UserProfile
    ) {
        root.findViewById<TextView>(
            R.id.profile_header_role
        ).text = profile.roleLabel

        root.findViewById<TextView>(
            R.id.profile_avatar
        ).text = profile.initials

        root.findViewById<TextView>(
            R.id.profile_name
        ).text = profile.name

        root.findViewById<TextView>(
            R.id.profile_role
        ).text = profile.roleLabel

        root.findViewById<TextView>(
            R.id.profile_full_name
        ).text = profile.name

        root.findViewById<TextView>(
            R.id.profile_email
        ).text = profile.email

        root.findViewById<TextView>(
            R.id.profile_phone
        ).text = profile.phone

        root.findViewById<TextView>(
            R.id.profile_document
        ).text = profile.document

        root.findViewById<TextView>(
            R.id.profile_location
        ).text = profile.location

        root.findViewById<TextView>(
            R.id.profile_hero_email
        ).text = profile.email

        val sectionTitle = root.findViewById<TextView>(
            R.id.profile_role_section_title
        )

        val locationLabel = root.findViewById<TextView>(
            R.id.profile_location_label
        )

        when (profile.role) {
            UserRole.BUYER -> {
                sectionTitle.text = "Información de entrega"
                locationLabel.text = "Ubicación de entrega"
            }

            UserRole.SELLER -> {
                sectionTitle.text = "Información laboral"
                locationLabel.text = "Sede asignada"
            }

            UserRole.ADMIN -> {
                sectionTitle.text = "Información administrativa"
                locationLabel.text = "Sede asignada"
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage(
                "¿Quieres regresar al inicio de sesión?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ ->

                // Cierra la sesión persistida antes de volver al login
                SessionManager(requireContext()).clear()

                val navController = findNavController()

                val options = NavOptions.Builder()
                    .setPopUpTo(
                        navController.graph.id,
                        true
                    )
                    .build()

                navController.navigate(
                    R.id.sign_in_fragment,
                    null,
                    options
                )
            }
            .show()
    }
}
