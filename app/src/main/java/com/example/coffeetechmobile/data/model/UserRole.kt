package com.example.coffeetechmobile.data.model

/**
 * Roles disponibles dentro de Coffee Tech
 *
 * Se usa un único enum para que la base de datos, la sesión
 * y las pantallas compartan exactamente los mismos valores
 */
enum class UserRole {

    // Administrador: gestiona usuarios, productos y reportes
    ADMIN,

    // Vendedor: consulta y actualiza los pedidos recibidos
    SELLER,

    // Comprador: navega el catálogo, usa el carrito y realiza pedidos
    BUYER;

    companion object {

        // Convierte el valor almacenado en SQLite a un rol válido
        // Si el valor viene vacío o corrupto, se usa BUYER por seguridad
        fun fromStorage(value: String?): UserRole {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: BUYER
        }
    }
}
