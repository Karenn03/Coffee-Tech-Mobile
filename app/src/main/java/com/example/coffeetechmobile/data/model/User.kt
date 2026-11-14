package com.example.coffeetechmobile.data.model

/**
 * Cuenta de usuario tal como se expone al resto de la aplicación
 *
 * No incluye la contraseña ni la sal: esos datos solo viven en la capa de
 * persistencia y se consultan mediante [StoredUser] cuando se valida un login
 */
data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val document: String = "",
    val documentType: String = "CC (Cédula)",
    val jobTitle: String = "",
    val branch: String = "",
    val contractType: String = "",
    val role: UserRole = UserRole.BUYER,
    val location: String = "",
    val active: Boolean = true,
    val biometricEnabled: Boolean = false,
    val createdAt: Long = 0L
)

/**
 * Resultado interno de una consulta a la tabla de usuarios
 *
 * Mantiene el hash y la sal separados del modelo para evitar que
 * credenciales sensibles circulen por las pantallas o por la sesión
 */
data class StoredUser(
    val user: User,
    val passwordHash: String,
    val passwordSalt: String
)
