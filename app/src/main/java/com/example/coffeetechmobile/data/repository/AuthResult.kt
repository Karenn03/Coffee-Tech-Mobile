package com.example.coffeetechmobile.data.repository

import com.example.coffeetechmobile.data.model.User

/**
 * Resultado de una operación de autenticación
 *
 * El repositorio no conoce UI: devuelve datos o un mensaje de error
 * que el ViewModel transforma en estado para la pantalla
 */
sealed interface AuthResult {

    // Operación exitosa con la cuenta afectada
    data class Success(val user: User) : AuthResult

    // Operación rechazada con un mensaje legible para el usuario
    data class Error(val message: String) : AuthResult
}
