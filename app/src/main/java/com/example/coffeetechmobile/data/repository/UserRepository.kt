package com.example.coffeetechmobile.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.local.UserDao
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Datos que el administrador captura en el formulario de usuarios
 *
 * [jobTitle] es el cargo visible (Barista, Gerente, etc.) y [role] es el rol
 * técnico que decide a qué panel entra la cuenta al iniciar sesión
 */
data class UserInput(
    val fullName: String,
    val documentType: String,
    val documentNumber: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val jobTitle: String,
    val branch: String,
    val contractType: String
)

// Resultado de crear, actualizar o consultar usuarios
sealed interface UserResult {

    /**
     * [temporaryPassword] solo viene informado al crear una cuenta, para que
     * el administrador pueda entregarle la clave inicial al colaborador.
     */
    data class Success(
        val user: User,
        val temporaryPassword: String? = null
    ) : UserResult

    data class Error(val message: String) : UserResult
}

/**
 * CRUD de usuarios del administrador sobre SQLite.
 *
 * Centraliza la validación de correo único, el hash de la contraseña temporal
 * y las operaciones de lectura, actualización y eliminación.
 */
class UserRepository(context: Context) {

    private val userDao = UserDao(
        CoffeeTechDatabase.getInstance(context)
    )
    private val passwordHasher = PasswordHasher()

    // Lista todas las cuentas para la vista de gestión de usuarios
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.findAll()
    }

    /**
     * Crea un colaborador con una contraseña temporal.
     *
     * El administrador no escribe la contraseña en el formulario; el sistema
     * asigna una clave conocida y la muestra al terminar la creación.
     */
    suspend fun createUser(input: UserInput): UserResult =
        withContext(Dispatchers.IO) {
            val email = normalizeEmail(input.email)

            if (userDao.existsByEmail(email)) {
                return@withContext UserResult.Error(
                    "Ya existe una cuenta con este correo."
                )
            }

            val salt = passwordHasher.generateSalt()
            val hash = passwordHasher.hash(TEMPORARY_PASSWORD, salt)
            val now = System.currentTimeMillis()

            val userId = try {
                userDao.insert(
                    fullName = input.fullName.trim(),
                    email = email,
                    phone = input.phone.trim(),
                    document = input.documentNumber.trim(),
                    documentType = input.documentType,
                    jobTitle = input.jobTitle,
                    branch = input.branch,
                    contractType = input.contractType,
                    role = input.role,
                    passwordHash = hash,
                    passwordSalt = salt,
                    location = input.branch,
                    biometricEnabled = false,
                    createdAt = now
                )
            } catch (exception: SQLiteConstraintException) {
                return@withContext UserResult.Error(
                    "Ya existe una cuenta con este correo."
                )
            }

            val user = User(
                id = userId,
                fullName = input.fullName.trim(),
                email = email,
                phone = input.phone.trim(),
                document = input.documentNumber.trim(),
                documentType = input.documentType,
                jobTitle = input.jobTitle,
                branch = input.branch,
                contractType = input.contractType,
                role = input.role,
                location = input.branch,
                active = true,
                biometricEnabled = false,
                createdAt = now
            )

            UserResult.Success(user, TEMPORARY_PASSWORD)
        }

    /**
     * Actualiza los datos del usuario sin tocar su contraseña.
     */
    suspend fun updateUser(
        userId: Long,
        input: UserInput
    ): UserResult = withContext(Dispatchers.IO) {
        val current = userDao.findById(userId)
            ?: return@withContext UserResult.Error(
                "El usuario ya no existe."
            )

        val email = normalizeEmail(input.email)
        val existing = userDao.findByEmail(email)

        if (existing != null && existing.user.id != userId) {
            return@withContext UserResult.Error(
                "Ya existe otra cuenta con este correo."
            )
        }

        // En compradores se conserva la dirección de entrega; en empleados
        // la sede seleccionada pasa a ser la ubicación del perfil.
        val location = if (input.role == UserRole.BUYER) {
            current.user.location
        } else {
            input.branch
        }

        val updatedRows = try {
            userDao.updateUser(
                userId = userId,
                fullName = input.fullName.trim(),
                email = email,
                phone = input.phone.trim(),
                document = input.documentNumber.trim(),
                documentType = input.documentType,
                jobTitle = input.jobTitle,
                branch = input.branch,
                contractType = input.contractType,
                role = input.role,
                location = location
            )
        } catch (exception: SQLiteConstraintException) {
            return@withContext UserResult.Error(
                "Ya existe otra cuenta con este correo."
            )
        }

        if (updatedRows <= 0) {
            return@withContext UserResult.Error(
                "No se pudo actualizar el usuario."
            )
        }

        UserResult.Success(
            current.user.copy(
                fullName = input.fullName.trim(),
                email = email,
                phone = input.phone.trim(),
                document = input.documentNumber.trim(),
                documentType = input.documentType,
                jobTitle = input.jobTitle,
                branch = input.branch,
                contractType = input.contractType,
                role = input.role,
                location = location
            )
        )
    }

    /**
     * Elimina una cuenta. Devuelve true si la fila existía.
     */
    suspend fun deleteUser(userId: Long): Boolean =
        withContext(Dispatchers.IO) {
            userDao.deleteById(userId) > 0
        }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    companion object {
        /** Contraseña inicial para las cuentas creadas por el administrador. */
        const val TEMPORARY_PASSWORD = "Coffee123*"
    }
}
