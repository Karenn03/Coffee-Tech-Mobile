package com.example.coffeetechmobile.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole

/**
 * Centraliza el destino inicial de cada rol
 *
 * Así el login, el registro y la restauración de sesión navegan al
 * mismo lugar sin repetir un `when` en cada pantalla
 */
object RoleNavigator {

    // Devuelve el id del fragmento principal según el rol autenticado
    fun destinationFor(role: UserRole): Int {
        return when (role) {
            UserRole.ADMIN -> R.id.adminDashboardFragment
            UserRole.SELLER -> R.id.sellerOrdersFragment
            UserRole.BUYER -> R.id.homeFragment
        }
    }

    /**
     * Limpia el back stack de autenticación y abre el panel del rol
     *
     * Se elimina todo el grafo anterior para que el botón atrás
     * no regrese al login después de autenticarse
     */
    fun navigateToRoleHome(
        navController: NavController,
        role: UserRole
    ) {
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.auth_nav_graph, true)
            .setLaunchSingleTop(true)
            .build()

        navController.navigate(
            destinationFor(role),
            null,
            options
        )
    }
}
