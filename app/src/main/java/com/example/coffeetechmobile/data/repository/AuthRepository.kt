package com.example.coffeetechmobile.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.local.UserDao
import com.example.coffeetechmobile.data.model.StoredUser
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.security.PasswordHasher
import com.example.coffeetechmobile.data.session.SessionManager
import com.example.coffeetechmobile.data.session.SessionUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reglas de negocio de autenticación
 *
 * Centraliza el acceso a SQLite, el hash de contraseñas y la sesión. Los
 * ViewModels solo llaman a estos métodos y reaccionan con LiveData, sin
 * conocer SQL ni SharedPreferences
 */
class AuthRepository(context: Context) {

    private val userDao = UserDao(
        CoffeeTechDatabase.getInstance(context)
    )
    private val passwordHasher = PasswordHasher()
    private val sessionManager = SessionManager(context)

    /**
     * Valida correo y contraseña en la base local
     *
     * Si el login es correcto, activa el ingreso con huella para esa cuenta y
     * guarda la sesión. La activación automática evita tener que pedir el
     * correo dos veces en la siguiente autenticación biométrica
     */
    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val stored = userDao.findByEmail(normalizeEmail(email))
            ?: return@withContext AuthResult.Error(
                INVALID_CREDENTIALS_MESSAGE
            )

        if (!stored.user.active) {
            return@withContext AuthResult.Error(INACTIVE_ACCOUNT_MESSAGE)
        }

        val passwordMatches = passwordHasher.verify(
            password = password,
            saltBase64 = stored.passwordSalt,
            expectedHashBase64 = stored.passwordHash
        )

        if (!passwordMatches) {
            return@withContext AuthResult.Error(INVALID_CREDENTIALS_MESSAGE)
        }

        userDao.setBiometricEnabled(stored.user.id, true)
        val authenticatedUser = stored.user.copy(biometricEnabled = true)
        sessionManager.save(authenticatedUser)

        AuthResult.Success(authenticatedUser)
    }

    /**
     * Registra un comprador nuevo
     *
     * El rol se asigna por defecto a BUYER porque la pantalla de registro no
     * incluye selector de rol. Los roles ADMIN y SELLER se crean como cuentas
     * semilla y, más adelante, desde el CRUD del administrador
     */
    suspend fun signUp(
        fullName: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = normalizeEmail(email)

        if (userDao.existsByEmail(normalizedEmail)) {
            return@withContext AuthResult.Error(
                "Ya existe una cuenta registrada con este correo."
            )
        }

        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hash(password, salt)
        val now = System.currentTimeMillis()

        val userId = try {
            userDao.insert(
                fullName = fullName.trim(),
                email = normalizedEmail,
                phone = "",
                document = "",
                documentType = "CC (Cédula)",
                jobTitle = "Cliente",
                branch = "",
                contractType = "",
                role = UserRole.BUYER,
                passwordHash = hash,
                passwordSalt = salt,
                location = "",
                biometricEnabled = true,
                createdAt = now
            )
        } catch (exception: SQLiteConstraintException) {
            return@withContext AuthResult.Error(
                "Ya existe una cuenta registrada con este correo."
            )
        }

        val newUser = User(
            id = userId,
            fullName = fullName.trim(),
            email = normalizedEmail,
            documentType = "CC (Cédula)",
            jobTitle = "Cliente",
            role = UserRole.BUYER,
            biometricEnabled = true,
            createdAt = now
        )

        // El registro deja la sesión iniciada para que el
        // comprador entre directamente a su catálogo
        sessionManager.save(newUser)

        AuthResult.Success(newUser)
    }

    /**
     * Comprueba que el correo exista antes de abrir el formulario de nueva
     * contraseña. No revela más datos que la existencia de la cuenta
     */
    suspend fun verifyRecoveryEmail(email: String): AuthResult =
        withContext(Dispatchers.IO) {
            val stored = userDao.findByEmail(normalizeEmail(email))
                ?: return@withContext AuthResult.Error(
                    "No encontramos una cuenta asociada a este correo."
                )

            if (!stored.user.active) {
                return@withContext AuthResult.Error(
                    INACTIVE_ACCOUNT_MESSAGE
                )
            }

            AuthResult.Success(stored.user)
        }

    /**
     * Cambia la contraseña de una cuenta existente
     *
     * En un backend real este paso se haría después de validar un enlace o
     * código enviado al correo. Con SQLite se valida la existencia de la
     * cuenta y se guarda el nuevo hash localmente
     */
    suspend fun resetPassword(
        email: String,
        newPassword: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val stored = userDao.findByEmail(normalizeEmail(email))
            ?: return@withContext AuthResult.Error(
                "No encontramos una cuenta asociada a este correo."
            )

        if (!stored.user.active) {
            return@withContext AuthResult.Error(INACTIVE_ACCOUNT_MESSAGE)
        }

        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hash(newPassword, salt)
        userDao.updatePassword(
            userId = stored.user.id,
            passwordHash = hash,
            passwordSalt = salt
        )

        AuthResult.Success(stored.user)
    }

    /**
     * Verifica que la cuenta tenga la huella habilitada antes
     * de mostrar el diálogo biométrico del sistema
     */
    suspend fun prepareBiometricSignIn(email: String): AuthResult =
        withContext(Dispatchers.IO) {
            val stored = userDao.findByEmail(normalizeEmail(email))
                ?: return@withContext AuthResult.Error(
                    "No existe una cuenta con este correo."
                )

            if (!stored.user.active) {
                return@withContext AuthResult.Error(INACTIVE_ACCOUNT_MESSAGE)
            }

            if (!stored.user.biometricEnabled) {
                return@withContext AuthResult.Error(
                    "Esta cuenta todavía no tiene la huella habilitada. " +
                        "Inicia sesión una vez con tu contraseña."
                )
            }

            AuthResult.Success(stored.user)
        }

    /**
     * Abre la sesión después de que el sistema confirmó la huella
     *
     * Se vuelve a consultar la cuenta para no confiar únicamente
     * en el correo que llegó desde la pantalla
     */
    suspend fun signInWithBiometrics(email: String): AuthResult =
        withContext(Dispatchers.IO) {
            val stored = userDao.findByEmail(normalizeEmail(email))
                ?: return@withContext AuthResult.Error(
                    "No existe una cuenta con este correo."
                )

            if (!stored.user.active) {
                return@withContext AuthResult.Error(INACTIVE_ACCOUNT_MESSAGE)
            }

            if (!stored.user.biometricEnabled) {
                return@withContext AuthResult.Error(
                    "La huella no está habilitada para esta cuenta."
                )
            }

            sessionManager.save(stored.user)
            AuthResult.Success(stored.user)
        }

    // Devuelve la sesión activa, si existe
    fun currentSession(): SessionUser? = sessionManager.current()

    // Cierra la sesión activa sin borrar las cuentas ni sus contraseñas
    fun signOut() {
        sessionManager.clear()
    }

    /**
     * Normaliza el correo para que la búsqueda sea consistente
     * con la restricción COLLATE NOCASE de SQLite
     */
    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    private companion object {
        const val INVALID_CREDENTIALS_MESSAGE =
            "Correo o contraseña incorrectos."
        const val INACTIVE_ACCOUNT_MESSAGE =
            "Esta cuenta está desactivada. Contacta al administrador."
    }
}
