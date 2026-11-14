package com.example.coffeetechmobile.data.session

import android.content.Context
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole

// Datos mínimos que se conservan mientras hay una sesión activa
data class SessionUser(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: UserRole
)

/**
 * Guarda la sesión activa en SharedPreferences
 *
 * No se almacenan contraseñas ni hashes: solo el id, el nombre, el correo y el
 * rol necesarios para restaurar la navegación al abrir la aplicación
 */
class SessionManager(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    // Registra la cuenta que acaba de autenticarse
    fun save(user: User) {
        preferences.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ROLE, user.role.name)
            .apply()
    }

    // Devuelve la sesión guardada o null si el usuario no ha iniciado sesión
    fun current(): SessionUser? {
        val userId = preferences.getLong(KEY_USER_ID, NO_SESSION)
        if (userId <= 0L) {
            return null
        }

        return SessionUser(
            id = userId,
            fullName = preferences.getString(KEY_FULL_NAME, "").orEmpty(),
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            role = UserRole.fromStorage(
                preferences.getString(KEY_ROLE, null)
            )
        )
    }

    /**
     * Elimina la sesión activa
     *
     * La habilitación biométrica de la cuenta se conserva en la tabla `users`
     * para que el usuario pueda volver a entrar con huella después del logout
     */
    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "coffee_tech_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val NO_SESSION = -1L
    }
}
