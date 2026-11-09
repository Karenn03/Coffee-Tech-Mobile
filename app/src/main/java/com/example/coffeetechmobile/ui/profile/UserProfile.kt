package com.example.coffeetechmobile.ui.profile

enum class UserRole {
    BUYER,
    SELLER
}

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
        }

    val initials: String
        get() = name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
