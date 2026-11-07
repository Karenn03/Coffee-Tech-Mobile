package com.example.coffeetechmobile.ui.manager.users

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R

class UserManagementFragment : Fragment(R.layout.fragment_user_management) {

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
        val usersRecyclerView =
            view.findViewById<RecyclerView>(R.id.users_recycler_view)

        usersRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        usersRecyclerView.isNestedScrollingEnabled = false

        usersRecyclerView.adapter = UserAdapter(getSampleUsers())

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

}


private data class UserItem(
    val name: String,
    val initials: String,
    val role: String,
    val document: String,
    val primaryIcon: Int,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryIcon: Int? = null,
    val secondaryLabel: String? = null,
    val secondaryValue: String? = null
)

private class UserAdapter(
    private val users: List<UserItem>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(user: UserItem) {

            itemView.findViewById<TextView>(
                R.id.user_initials
            ).text = user.initials

            itemView.findViewById<TextView>(
                R.id.user_name
            ).text = user.name

            itemView.findViewById<TextView>(
                R.id.user_role
            ).text = user.role

            itemView.findViewById<TextView>(
                R.id.user_document
            ).text = user.document

            bindDetail(
                R.id.user_detail_primary,
                R.id.user_detail_primary_icon,
                R.id.user_detail_primary_label,
                R.id.user_detail_primary_value,
                user.primaryIcon,
                user.primaryLabel,
                user.primaryValue
            )

            val secondary = itemView.findViewById<View>(
                R.id.user_detail_secondary
            )

            if (
                user.secondaryIcon != null &&
                user.secondaryLabel != null &&
                user.secondaryValue != null
            ) {
                secondary.visibility = View.VISIBLE

                bindDetail(
                    R.id.user_detail_secondary,
                    R.id.user_detail_secondary_icon,
                    R.id.user_detail_secondary_label,
                    R.id.user_detail_secondary_value,
                    user.secondaryIcon,
                    user.secondaryLabel,
                    user.secondaryValue
                )
            } else {
                secondary.visibility = View.GONE
            }
        }

        private fun bindDetail(
            containerId: Int,
            iconId: Int,
            labelId: Int,
            valueId: Int,
            iconRes: Int,
            label: String,
            value: String
        ) {
            val container = itemView.findViewById<View>(containerId)

            container.findViewById<ImageView>(iconId)
                .setImageResource(iconRes)

            container.findViewById<TextView>(labelId).text = label

            container.findViewById<TextView>(valueId).text = value
        }
    }
}

