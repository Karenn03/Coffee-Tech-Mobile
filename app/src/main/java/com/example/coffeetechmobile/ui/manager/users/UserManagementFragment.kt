package com.example.coffeetechmobile.ui.manager.users

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
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
}
