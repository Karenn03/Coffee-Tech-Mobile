package com.example.coffeetechmobile.ui.common

import android.view.View
import android.widget.TextView
import androidx.navigation.NavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.session.SessionManager

// Etiqueta visible del rol de la cuenta
fun UserRole.headerLabel(): String {
    return when (this) {
        UserRole.ADMIN -> "Gerente"
        UserRole.SELLER -> "Vendedor"
        UserRole.BUYER -> "Cliente"
    }
}

/**
 * Pinta el nombre y el rol del usuario con sesión activa en los encabezados
 * de las pantallas. Cada layout usa sus propios ids, por eso se intentan
 * todos los pares conocidos.
 */
fun bindSessionHeader(root: View) {
    val session = SessionManager(root.context).current() ?: return

    val headers = listOf(
        R.id.header_user_name to R.id.header_user_role,
        R.id.customer_name to R.id.home_header_role,
        R.id.cart_customer_name to R.id.cart_header_role,
        R.id.product_header_name to R.id.product_header_role,
        R.id.seller_home_header_name to R.id.seller_home_header_role,
        R.id.seller_orders_header_name to R.id.seller_orders_header_role,
        R.id.profile_header_name to R.id.profile_header_role
    )

    headers.forEach { (nameId, roleId) ->
        root.findViewById<TextView>(nameId)?.text = session.fullName
        root.findViewById<TextView>(roleId)?.text =
            session.role.headerLabel()
    }
}

/**
 * Conecta el icono de perfil de los encabezados con la pantalla de perfil.
 *
 * Cada rol usa un id distinto en su layout; se intentan todos y solo se
 * enlaza el que exista en la pantalla actual.
 */
fun bindHeaderProfileButton(
    root: View,
    navController: NavController
) {
    val profileButtonIds = listOf(
        R.id.profile_button,
        R.id.cart_profile_button,
        R.id.product_profile_button,
        R.id.manager_profile_button,
        R.id.seller_home_profile_button
    )

    profileButtonIds.forEach { buttonId ->
        root.findViewById<View>(buttonId)?.setOnClickListener {
            navController.navigate(R.id.profileFragment)
        }
    }
}
