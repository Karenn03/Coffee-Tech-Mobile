package com.example.coffeetechmobile.data.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias del hash de contraseñas.
 *
 * Se usan pocas iteraciones para que la prueba sea rápida; la aplicación real
 * usa 120.000 iteraciones.
 */
class PasswordHasherTest {

    private val hasher = PasswordHasher(iterations = 1_000)

    @Test
    fun `dos hashes de la misma contrasena son diferentes por la sal`() {
        val firstSalt = hasher.generateSalt()
        val secondSalt = hasher.generateSalt()

        val firstHash = hasher.hash("Coffee123*", firstSalt)
        val secondHash = hasher.hash("Coffee123*", secondSalt)

        assertNotEquals(firstHash, secondHash)
    }

    @Test
    fun `verify acepta la contrasena correcta`() {
        val salt = hasher.generateSalt()
        val hash = hasher.hash("Coffee123*", salt)

        assertTrue(hasher.verify("Coffee123*", salt, hash))
    }

    @Test
    fun `verify rechaza una contrasena incorrecta`() {
        val salt = hasher.generateSalt()
        val hash = hasher.hash("Coffee123*", salt)

        assertFalse(hasher.verify("OtraClave123*", salt, hash))
    }
}
