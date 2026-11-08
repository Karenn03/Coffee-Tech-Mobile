package com.example.coffeetechmobile.ui.manager.users

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.util.Patterns
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R

class CreateUserFragment : Fragment(R.layout.fragment_create_user) {

    private var selectedRole: String? = null
    private var userToEdit: UserItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar el usuario enviado desde el listado
        userToEdit = arguments?.getSerializable("user_to_edit") as? UserItem

        // Si existe un usuario, cargar sus datos
        userToEdit?.let { user ->
            loadUserData(view, user)
        }

        // Configuración de roles
        bindRoleOption(
            view,
            R.id.role_barista,
            R.drawable.ic_coffee,
            R.string.role_barista,
            R.string.role_barista_description
        )

        bindRoleOption(
            view,
            R.id.role_baker,
            R.drawable.ic_croissant,
            R.string.role_baker,
            R.string.role_baker_description
        )

        bindRoleOption(
            view,
            R.id.role_cashier,
            R.drawable.ic_hand_coins,
            R.string.role_cashier,
            R.string.role_cashier_description
        )

        bindRoleOption(
            view,
            R.id.role_manager,
            R.drawable.ic_user_star,
            R.string.role_manager,
            R.string.role_manager_description
        )

        bindRoleOption(
            view,
            R.id.role_super_admin,
            R.drawable.ic_user_key,
            R.string.role_super_admin,
            R.string.role_super_admin_description
        )

        // Selección de un único rol
        val roleIds = listOf(
            R.id.role_barista,
            R.id.role_baker,
            R.id.role_cashier,
            R.id.role_manager,
            R.id.role_super_admin
        )

        roleIds.forEach { roleId ->
            view.findViewById<View>(roleId).setOnClickListener {
                selectRole(view, roleId)
            }
        }

        view.findViewById<View>(R.id.back_to_users_button)
            .setOnClickListener {
                findNavController().navigateUp()
            }

        view.findViewById<View>(R.id.cancel_registration_button)
            .setOnClickListener {
                findNavController().navigateUp()
            }

        view.findViewById<View>(R.id.create_and_activate_button)
            .setOnClickListener {

                val document = view.findViewById<EditText>(
                    R.id.document_number_input
                ).text.toString().trim()

                val name = view.findViewById<EditText>(
                    R.id.full_name_input
                ).text.toString().trim()

                val email = view.findViewById<EditText>(
                    R.id.corporate_email_input
                ).text.toString().trim()

                val phone = view.findViewById<EditText>(
                    R.id.corporate_phone_input
                ).text.toString().trim()

                val branch = view.findViewById<Spinner>(
                    R.id.branch_spinner
                )

                val contract = view.findViewById<Spinner>(
                    R.id.contract_type_spinner
                )

                when {
                    document.isEmpty() ->
                        Toast.makeText(requireContext(), "Ingresa el número de documento", Toast.LENGTH_SHORT).show()

                    name.isEmpty() ->
                        Toast.makeText(requireContext(), "Ingresa los nombres completos", Toast.LENGTH_SHORT).show()

                    email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                        Toast.makeText(requireContext(), "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()

                    phone.isEmpty() ->
                        Toast.makeText(requireContext(), "Ingresa el celular corporativo", Toast.LENGTH_SHORT).show()

                    selectedRole == null ->
                        Toast.makeText(requireContext(), "Selecciona un rol", Toast.LENGTH_SHORT).show()

                    branch.selectedItemPosition == 0 ->
                        Toast.makeText(requireContext(), "Selecciona una sede", Toast.LENGTH_SHORT).show()

                    contract.selectedItemPosition == 0 ->
                        Toast.makeText(requireContext(), "Selecciona un tipo de contrato", Toast.LENGTH_SHORT).show()

                    else -> {
                        val documentType = view.findViewById<Spinner>(
                            R.id.document_type_spinner
                        ).selectedItem.toString()

                        val updatedUser = userToEdit?.copy(
                            name = name,
                            initials = name.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .map { it.first().uppercaseChar() }
                                .joinToString(""),

                            role = selectedRole!!,
                            document = "$documentType $document",
                            documentType = documentType,
                            documentNumber = document,
                            email = email,
                            phone = phone,
                            branch = branch.selectedItem.toString(),
                            contractType = contract.selectedItem.toString(),
                            primaryLabel = "Sede:",
                            primaryValue = branch.selectedItem.toString()
                        )

                        if (updatedUser != null) {
                            // Enviar el usuario actualizado al listado
                            findNavController()
                                .previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("updated_user", updatedUser)

                            Toast.makeText(
                                requireContext(),
                                "Usuario actualizado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().navigateUp()

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "¡Formulario validado correctamente!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

    }

    private fun bindRoleOption(
        root: View,
        roleId: Int,
        iconRes: Int,
        titleRes: Int,
        descriptionRes: Int
    ) {
        val roleView = root.findViewById<View>(roleId)

        roleView.findViewById<ImageView>(R.id.role_icon)
            .setImageResource(iconRes)

        roleView.findViewById<TextView>(R.id.role_title)
            .setText(titleRes)

        roleView.findViewById<TextView>(R.id.role_description)
            .setText(descriptionRes)
    }

    private fun selectRole(root: View, selectedId: Int) {

        val roleIds = listOf(
            R.id.role_barista,
            R.id.role_baker,
            R.id.role_cashier,
            R.id.role_manager,
            R.id.role_super_admin
        )

        roleIds.forEach { roleId ->

            val roleView = root.findViewById<View>(roleId)

            if (roleId == selectedId) {
                roleView.setBackgroundResource(
                    R.drawable.bg_role_option_selected
                )

                selectedRole = roleView
                    .findViewById<TextView>(R.id.role_title)
                    .text.toString()

            } else {
                roleView.setBackgroundResource(
                    R.drawable.bg_role_option
                )
            }
        }
    }

    private fun loadUserData(root: View, user: UserItem) {

        // Cambiar título y subtítulo
        root.findViewById<TextView>(R.id.form_title)
            .setText(R.string.edit_user_title)

        root.findViewById<TextView>(R.id.form_subtitle)
            .setText(R.string.edit_user_subtitle)

        // Cargar documento
        root.findViewById<EditText>(R.id.document_number_input)
            .setText(
                user.documentNumber.ifBlank {
                    user.document.removePrefix("${user.documentType} ").trim()
                }
            )

        // Cargar nombre, correo y teléfono
        root.findViewById<EditText>(R.id.full_name_input)
            .setText(user.name)

        root.findViewById<EditText>(R.id.corporate_email_input)
            .setText(user.email)

        root.findViewById<EditText>(R.id.corporate_phone_input)
            .setText(user.phone)

        // Seleccionar tipo de documento
        selectSpinnerItem(
            root.findViewById(R.id.document_type_spinner),
            user.documentType
        )

        // Seleccionar sede
        val branch = user.branch.ifBlank {
            if (user.role != "Cliente" && user.role != "Super Admin") {
                user.primaryValue
            } else {
                ""
            }
        }

        selectSpinnerItem(
            root.findViewById(R.id.branch_spinner),
            branch
        )

        // Seleccionar tipo de contrato
        selectSpinnerItem(
            root.findViewById(R.id.contract_type_spinner),
            user.contractType
        )

        // Seleccionar rol
        val roleId = when (user.role) {
            getString(R.string.role_barista) -> R.id.role_barista
            getString(R.string.role_baker) -> R.id.role_baker
            getString(R.string.role_cashier) -> R.id.role_cashier
            getString(R.string.role_manager) -> R.id.role_manager
            getString(R.string.role_super_admin) -> R.id.role_super_admin
            else -> null
        }

        roleId?.let {
            selectRole(root, it)
        }

        // Cambiar texto del botón principal
        root.findViewById<TextView>(R.id.form_action_text)
            .setText(R.string.save_user_changes)
    }

    private fun selectSpinnerItem(
        spinner: Spinner,
        value: String
    ) {
        if (value.isBlank()) return

        val adapter = spinner.adapter ?: return

        for (position in 0 until adapter.count) {
            if (adapter.getItem(position).toString() == value) {
                spinner.setSelection(position)
                return
            }
        }
    }

}
