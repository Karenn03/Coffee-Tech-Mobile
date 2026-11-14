package com.example.coffeetechmobile.ui.profile

import com.example.coffeetechmobile.data.model.UserRole

/**
 * Modelo de presentación del perfil.
 *
 * Es independiente de la entidad de SQLite para que la pantalla pueda mostrar
 * datos calculados (iniciales, etiqueta del rol) sin arrastrar credenciales.
 */
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val document: String,
    val location: String
) {
    val roleLabel: String
        get() = when (role) {
            UserRole.BUYER -> "Cliente"
            UserRole.SELLER -> "Vendedor"
            UserRole.ADMIN -> "Administrador"
        }

    val initials: String
        get() = name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
