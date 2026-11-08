package com.example.coffeetechmobile.ui.manager.users

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R

class UserManagementFragment : Fragment(R.layout.fragment_user_management) {

    private val users = getSampleUsers().toMutableList()
    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usuarios globales
        bindStat(
            root = view,
            cardId = R.id.stat_total,
            iconRes = R.drawable.ic_users,
            badge = "Total",
            value = "1.420",
            label = "Usuarios globales",
            backgroundRes = R.drawable.bg_stat_total,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#916A45",
            badgeTextColor = "#8D6849"
        )

        // Clientes activos
        bindStat(
            root = view,
            cardId = R.id.stat_clients,
            iconRes = R.drawable.ic_shopping_bag,
            badge = "97.5%",
            value = "1.385",
            label = "Clientes activos",
            backgroundRes = R.drawable.bg_stat_clients,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#8A5E35",
            badgeTextColor = "#8D6849"
        )

        // Ventas del día
        bindStat(
            root = view,
            cardId = R.id.stat_sales,
            iconRes = R.drawable.ic_shopping_basket,
            badge = "12 uds",
            value = "7",
            label = "Ventas del día",
            backgroundRes = R.drawable.bg_stat_sales,
            badgeBackgroundRes = R.drawable.bg_stat_badge_dark,
            foregroundColor = "#FCE6DC",
            badgeTextColor = "#FCE6DC"
        )

        // Empleados
        bindStat(
            root = view,
            cardId = R.id.stat_employees,
            iconRes = R.drawable.ic_coffee_maker,
            badge = "Barra activa",
            value = "28",
            label = "Empleados",
            backgroundRes = R.drawable.bg_stat_employees,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#B77737",
            badgeTextColor = "#A9682E"
        )

        // Listado de usuarios
        val usersRecyclerView = view.findViewById<RecyclerView>(
            R.id.users_recycler_view
        )

        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.isNestedScrollingEnabled = false

        userAdapter = UserAdapter(
            users = users,

            // Acción: editar usuario
            onEdit = { user ->
                val bundle = bundleOf("user_to_edit" to user)

                findNavController().navigate(
                    R.id.action_userManagementFragment_to_createUserFragment,
                    bundle
                )
            },

            // Acción: eliminar usuario
            onDelete = { user ->
                showDeleteConfirmation(user, userAdapter)
            }
        )

        usersRecyclerView.adapter = userAdapter

        // Recibir los cambios realizados en el formulario
        val result = findNavController()
            .currentBackStackEntry
            ?.savedStateHandle

        result?.getLiveData<UserItem>("created_user")
            ?.observe(viewLifecycleOwner) { newUser ->

                userAdapter.addUser(newUser)

                result.remove<UserItem>("created_user")
            }

        result?.getLiveData<UserItem>("updated_user")
            ?.observe(viewLifecycleOwner) { updatedUser ->

                userAdapter.updateUser(updatedUser)

                result.remove<UserItem>("updated_user")
            }

        // Navegar al formulario de creación
        view.findViewById<View>(R.id.create_user_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_userManagementFragment_to_createUserFragment
                )
            }

    }

    private fun bindStat(
        root: View,
        cardId: Int,
        iconRes: Int,
        badge: String,
        value: String,
        label: String,
        backgroundRes: Int,
        badgeBackgroundRes: Int,
        foregroundColor: String,
        badgeTextColor: String
    ) {
        val card = root.findViewById<View>(cardId)

        card.setBackgroundResource(backgroundRes)

        val icon = card.findViewById<ImageView>(R.id.stat_icon)
        icon.setImageResource(iconRes)
        icon.imageTintList =
            ColorStateList.valueOf(Color.parseColor(foregroundColor))

        val badgeView = card.findViewById<TextView>(R.id.stat_badge)
        badgeView.text = badge
        badgeView.setBackgroundResource(badgeBackgroundRes)
        badgeView.setTextColor(Color.parseColor(badgeTextColor))

        val valueView = card.findViewById<TextView>(R.id.stat_value)
        valueView.text = value
        valueView.setTextColor(Color.parseColor(foregroundColor))

        val labelView = card.findViewById<TextView>(R.id.stat_label)
        labelView.text = label
        labelView.setTextColor(Color.parseColor(foregroundColor))
    }

    private fun getSampleUsers(): List<UserItem> {
        return listOf(
            UserItem(
                name = "Carlos Barragan",
                initials = "CB",
                role = "Barista",
                document = "CC 1.020.491.823",
                primaryIcon = R.drawable.ic_store,
                primaryLabel = "Sede:",
                primaryValue = "Bogotá - Usaquén",
                secondaryIcon = R.drawable.ic_clipboard_clock,
                secondaryLabel = "Turno:",
                secondaryValue = "Mañana"
            ),
            UserItem(
                name = "María Martínez",
                initials = "MM",
                role = "Cliente",
                document = "CC 52.891.044",
                primaryIcon = R.drawable.ic_receipt,
                primaryLabel = "Última compra:",
                primaryValue = "Hoy 10:45 a.m."
            ),
            UserItem(
                name = "Laura Zapata",
                initials = "LZ",
                role = "Panadero",
                document = "CC 52.938.410",
                primaryIcon = R.drawable.ic_store,
                primaryLabel = "Sede:",
                primaryValue = "Bogotá - Chapinero",
                secondaryIcon = R.drawable.ic_clipboard_clock,
                secondaryLabel = "Turno:",
                secondaryValue = "Tarde"
            ),
            UserItem(
                name = "Santiago Gómez",
                initials = "SG",
                role = "Cajero",
                document = "CC 1.020.784.912",
                primaryIcon = R.drawable.ic_store,
                primaryLabel = "Sede:",
                primaryValue = "Bogotá - Engativá",
                secondaryIcon = R.drawable.ic_clipboard_clock,
                secondaryLabel = "Turno:",
                secondaryValue = "Mañana"
            ),
            UserItem(
                name = "Claudia Monroy",
                initials = "CM",
                role = "Super Admin",
                document = "CC 43.902.184",
                primaryIcon = R.drawable.ic_user_key,
                primaryLabel = "Último inicio de sesión:",
                primaryValue = "Hoy 11:32 a.m."
            )
        )
    }

    private fun showDeleteConfirmation(
        user: UserItem,
        adapter: UserAdapter
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage(
                "¿Estás seguro de que deseas eliminar a ${user.name}?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                adapter.removeUser(user)

                Toast.makeText(
                    requireContext(),
                    "Usuario eliminado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

}
