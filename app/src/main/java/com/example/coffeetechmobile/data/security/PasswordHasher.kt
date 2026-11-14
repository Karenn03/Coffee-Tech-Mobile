package com.example.coffeetechmobile.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Genera y verifica contraseñas usando PBKDF2 con HMAC-SHA256.
 *
 * Cada usuario tiene una sal aleatoria distinta, por lo que dos personas con la
 * misma contraseña producen hashes diferentes. La contraseña en texto plano
 * nunca se guarda en SQLite.
 */
class PasswordHasher(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val keyLength: Int = DEFAULT_KEY_LENGTH,
    private val saltLength: Int = DEFAULT_SALT_LENGTH
) {

    private val secureRandom = SecureRandom()

    // Crea una sal aleatoria de 16 bytes y la devuelve codificada en Base64
    fun generateSalt(): String {
        val salt = ByteArray(saltLength)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    // Calcula el hash PBKDF2 de la contraseña usando la sal recibida
    fun hash(password: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLength
        )

        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = factory.generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(hash)
        } finally {
            // Limpia la contraseña temporal de la memoria.
            spec.clearPassword()
        }
    }

    /**
     * Compara la contraseña ingresada con el hash almacenado.
     *
     * Se usa [MessageDigest.isEqual] para hacer la comparación en tiempo
     * constante y evitar ataques de temporización.
     */
    fun verify(
        password: String,
        saltBase64: String,
        expectedHashBase64: String
    ): Boolean {
        return try {
            val actual = hash(password, saltBase64)
            MessageDigest.isEqual(
                Base64.getDecoder().decode(actual),
                Base64.getDecoder().decode(expectedHashBase64)
            )
        } catch (exception: IllegalArgumentException) {
            // Un hash corrupto en la base no debe romper el login.
            false
        }
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val DEFAULT_ITERATIONS = 120_000
        const val DEFAULT_KEY_LENGTH = 256
        const val DEFAULT_SALT_LENGTH = 16
    }
}
