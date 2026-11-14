package com.example.coffeetechmobile.ui.manager.users

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.repository.UserInput
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation

/**
 * Formulario de creación y edición de usuarios del administrador
 *
 * El formulario existente está orientado a colaboradores (Barista, Panadero,
 * Cajero, Gerente y Super administrador). Los compradores se registran desde
 * el login; cuando el administrador edita uno, se ocultan los campos laborales
 * y solo se actualizan sus datos de contacto
 */
class CreateUserFragment : Fragment(R.layout.fragment_create_user) {

    private val formViewModel: UserFormViewModel by viewModels()

    private var selectedRole: String? = null
    private var userToEdit: UserItem? = null
    private var isBuyerEdit = false
    private var isSaving = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.manager_bottom_navigation_container
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.USERS
        )

        // Recuperar el usuario enviado desde el listado
        userToEdit = arguments
            ?.getSerializable("user_to_edit") as? UserItem

        setupRoleOptions(view)

        userToEdit?.let { user ->
            loadUserData(view, user)
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
                saveUser(view)
            }

        observeFormState()
    }

    private fun setupRoleOptions(root: View) {
        bindRoleOption(
            root,
            R.id.role_barista,
            R.drawable.ic_coffee,
            R.string.role_barista,
            R.string.role_barista_description
        )

        bindRoleOption(
            root,
            R.id.role_baker,
            R.drawable.ic_croissant,
            R.string.role_baker,
            R.string.role_baker_description
        )

        bindRoleOption(
            root,
            R.id.role_cashier,
            R.drawable.ic_hand_coins,
            R.string.role_cashier,
            R.string.role_cashier_description
        )

        bindRoleOption(
            root,
            R.id.role_manager,
            R.drawable.ic_user_star,
            R.string.role_manager,
            R.string.role_manager_description
        )

        bindRoleOption(
            root,
            R.id.role_super_admin,
            R.drawable.ic_user_key,
            R.string.role_super_admin,
            R.string.role_super_admin_description
        )

        roleIds().forEach { roleId ->
            root.findViewById<View>(roleId).setOnClickListener {
                selectRole(root, roleId)
            }
        }
    }

    private fun roleIds(): List<Int> {
        return listOf(
            R.id.role_barista,
            R.id.role_baker,
            R.id.role_cashier,
            R.id.role_manager,
            R.id.role_super_admin
        )
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

    // Marca visualmente un único rol y guarda su título
    private fun selectRole(root: View, selectedId: Int) {
        roleIds().forEach { roleId ->
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

    // Valida, arma el modelo de datos y delega el guardado al ViewModel
    private fun saveUser(root: View) {
        if (isSaving) return

        val document = root.findViewById<EditText>(
            R.id.document_number_input
        ).text.toString().trim()

        val name = root.findViewById<EditText>(
            R.id.full_name_input
        ).text.toString().trim()

        val email = root.findViewById<EditText>(
            R.id.corporate_email_input
        ).text.toString().trim()

        val phone = root.findViewById<EditText>(
            R.id.corporate_phone_input
        ).text.toString().trim()

        val documentType = root.findViewById<Spinner>(
            R.id.document_type_spinner
        ).selectedItem.toString()

        val branch = root.findViewById<Spinner>(
            R.id.branch_spinner
        )

        val contract = root.findViewById<Spinner>(
            R.id.contract_type_spinner
        )

        when {
            document.isEmpty() -> showMessage(
                "Ingresa el número de documento"
            )

            name.isEmpty() -> showMessage(
                "Ingresa los nombres completos"
            )

            email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                showMessage("Ingresa un correo electrónico válido")

            phone.isEmpty() -> showMessage(
                "Ingresa el celular corporativo"
            )

            !isBuyerEdit && selectedRole == null -> showMessage(
                "Selecciona un rol"
            )

            !isBuyerEdit && branch.selectedItemPosition == 0 ->
                showMessage("Selecciona una sede")

            !isBuyerEdit && contract.selectedItemPosition == 0 ->
                showMessage("Selecciona un tipo de contrato")

            else -> {
                val role = if (isBuyerEdit) {
                    UserRole.BUYER
                } else {
                    roleFor(selectedRole!!)
                }

                val jobTitle = if (isBuyerEdit) {
                    "Cliente"
                } else {
                    selectedRole!!
                }

                val branchValue = if (isBuyerEdit) {
                    userToEdit?.branch.orEmpty()
                } else {
                    branch.selectedItem.toString()
                }

                val contractValue = if (isBuyerEdit) {
                    userToEdit?.contractType.orEmpty()
                } else {
                    contract.selectedItem.toString()
                }

                val input = UserInput(
                    fullName = name,
                    documentType = documentType,
                    documentNumber = document,
                    email = email,
                    phone = phone,
                    role = role,
                    jobTitle = jobTitle,
                    branch = branchValue,
                    contractType = contractValue
                )

                isSaving = true

                if (userToEdit == null) {
                    formViewModel.createUser(input)
                } else {
                    formViewModel.updateUser(
                        userToEdit!!.userId,
                        input
                    )
                }
            }
        }
    }

    // Traduce el cargo visible del formulario al rol técnico de autenticación
    private fun roleFor(jobTitle: String): UserRole {
        return when (jobTitle) {
            getString(R.string.role_manager),
            getString(R.string.role_super_admin) -> UserRole.ADMIN

            else -> UserRole.SELLER
        }
    }

    private fun observeFormState() {
        formViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                UserFormState.Idle -> Unit

                UserFormState.Loading -> setLoading(true)

                is UserFormState.Success -> {
                    setLoading(false)
                    isSaving = false

                    val temporaryPassword = state.temporaryPassword
                    formViewModel.consumeState()

                    if (temporaryPassword != null) {
                        showCreatedUserDialog(
                            user = state.user,
                            temporaryPassword = temporaryPassword
                        )
                    } else {
                        showMessage("Usuario actualizado correctamente")
                        findNavController().navigateUp()
                    }
                }

                is UserFormState.Error -> {
                    setLoading(false)
                    isSaving = false

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()

                    // Evita repetir el Toast si la pantalla se recrea.
                    formViewModel.consumeState()
                }
            }
        }
    }

    // Carga los datos del usuario que se va a editar
    private fun loadUserData(root: View, user: UserItem) {
        isBuyerEdit = user.authRole == UserRole.BUYER.name

        val emailLabel = root.findViewById<TextView>(
            R.id.email_field_label
        )

        val phoneLabel = root.findViewById<TextView>(
            R.id.phone_field_label
        )

        val emailInput = root.findViewById<EditText>(
            R.id.corporate_email_input
        )

        val phoneInput = root.findViewById<EditText>(
            R.id.corporate_phone_input
        )

        // Un comprador no tiene correo ni celular corporativo.
        if (isBuyerEdit) {
            emailLabel.setText(R.string.email_label)
            phoneLabel.setText(R.string.buyer_phone_label)
            emailInput.setHint(R.string.buyer_email_hint)
            phoneInput.setHint(R.string.buyer_phone_hint)
        } else {
            emailLabel.setText(R.string.corporate_email_label)
            phoneLabel.setText(R.string.corporate_phone_label)
            emailInput.setHint(R.string.corporate_email_hint)
            phoneInput.setHint(R.string.corporate_phone_hint)
        }

        root.findViewById<TextView>(R.id.cancel_registration_text)
            .setText(R.string.cancel)

        val employeeCard = root.findViewById<View>(
            R.id.coffee_tech_data_card
        )

        if (isBuyerEdit) {
            // Un comprador no tiene sede ni contrato laboral.
            employeeCard.visibility = View.GONE

            root.findViewById<TextView>(R.id.form_title)
                .setText(R.string.edit_client_title)

            root.findViewById<TextView>(R.id.form_subtitle)
                .setText(R.string.edit_client_subtitle)
        } else {
            employeeCard.visibility = View.VISIBLE

            root.findViewById<TextView>(R.id.form_title)
                .setText(R.string.edit_user_title)

            root.findViewById<TextView>(R.id.form_subtitle)
                .setText(R.string.edit_user_subtitle)

            val branch = user.branch.ifBlank {
                user.primaryValue
            }

            selectSpinnerItem(
                root.findViewById(R.id.branch_spinner),
                branch
            )

            selectSpinnerItem(
                root.findViewById(R.id.contract_type_spinner),
                user.contractType
            )

            val roleId = when (user.role) {
                getString(R.string.role_barista) -> R.id.role_barista
                getString(R.string.role_baker) -> R.id.role_baker
                getString(R.string.role_cashier) -> R.id.role_cashier
                getString(R.string.role_manager) -> R.id.role_manager
                getString(R.string.role_super_admin) -> R.id.role_super_admin
                "Vendedor" -> R.id.role_barista
                else -> null
            }

            roleId?.let {
                selectRole(root, it)
            }
        }

        root.findViewById<EditText>(R.id.document_number_input)
            .setText(user.documentNumber)

        root.findViewById<EditText>(R.id.full_name_input)
            .setText(user.name)

        root.findViewById<EditText>(R.id.corporate_email_input)
            .setText(user.email)

        root.findViewById<EditText>(R.id.corporate_phone_input)
            .setText(user.phone)

        selectSpinnerItem(
            root.findViewById(R.id.document_type_spinner),
            user.documentType
        )

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

    // Bloquea los botones mientras se escribe en SQLite
    private fun setLoading(loading: Boolean) {
        val createButton = requireView()
            .findViewById<View>(R.id.create_and_activate_button)

        val cancelButton = requireView()
            .findViewById<View>(R.id.cancel_registration_button)

        createButton.isEnabled = !loading
        createButton.alpha = if (loading) 0.7f else 1f

        cancelButton.isEnabled = !loading
    }

    // Muestra la contraseña temporal de la cuenta recién creada
    private fun showCreatedUserDialog(
        user: User,
        temporaryPassword: String
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.user_created_title)
            .setMessage(
                getString(
                    R.string.user_created_message,
                    user.fullName,
                    temporaryPassword
                )
            )
            .setPositiveButton(R.string.understood) { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
