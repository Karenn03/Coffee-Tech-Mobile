package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.database.Cursor
import com.example.coffeetechmobile.data.model.StoredUser
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole

/**
 * Acceso a la tabla `users`
 *
 * Esta clase concentra el SQL de usuarios para que el repositorio no dependa
 * de cursores ni de nombres de columnas. Todas las operaciones son síncronas:
 * el repositorio las ejecuta fuera del hilo principal
 */
class UserDao(private val database: CoffeeTechDatabase) {

    /**
     * Inserta una cuenta nueva y devuelve el id generado por SQLite
     * Lanza SQLiteConstraintException si el correo ya existe
     */
    fun insert(
        fullName: String,
        email: String,
        phone: String,
        document: String,
        documentType: String,
        jobTitle: String,
        branch: String,
        contractType: String,
        role: UserRole,
        passwordHash: String,
        passwordSalt: String,
        location: String,
        biometricEnabled: Boolean,
        createdAt: Long
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DOCUMENT, document)
            put(COLUMN_DOCUMENT_TYPE, documentType)
            put(COLUMN_JOB_TITLE, jobTitle)
            put(COLUMN_BRANCH, branch)
            put(COLUMN_CONTRACT_TYPE, contractType)
            put(COLUMN_ROLE, role.name)
            put(COLUMN_PASSWORD_HASH, passwordHash)
            put(COLUMN_PASSWORD_SALT, passwordSalt)
            put(COLUMN_LOCATION, location)
            put(COLUMN_ACTIVE, 1)
            put(COLUMN_BIOMETRIC_ENABLED, if (biometricEnabled) 1 else 0)
            put(COLUMN_CREATED_AT, createdAt)
            put(COLUMN_UPDATED_AT, createdAt)
        }

        return database.writableDatabase.insertOrThrow(
            TABLE_USERS,
            null,
            values
        )
    }

    // Busca una cuenta por correo (sin distinguir mayúsculas)
    fun findByEmail(email: String): StoredUser? {
        return queryOne(
            selection = "$COLUMN_EMAIL = ?",
            arguments = arrayOf(email)
        )
    }

    // Busca una cuenta por su id
    fun findById(id: Long): StoredUser? {
        return queryOne(
            selection = "$COLUMN_ID = ?",
            arguments = arrayOf(id.toString())
        )
    }

    // Indica si ya existe una cuenta con el correo indicado
    fun existsByEmail(email: String): Boolean {
        return findByEmail(email) != null
    }

    /**
     * Devuelve todas las cuentas ordenadas de la más reciente a la más antigua.
     * Se usa en el CRUD del administrador.
     */
    fun findAll(): List<User> {
        val users = mutableListOf<User>()

        database.readableDatabase.query(
            TABLE_USERS,
            ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                users.add(cursor.toUser())
            }
        }

        return users
    }

    /**
     * Actualiza los datos editables desde el CRUD del administrador.
     * No modifica el hash ni la sal de la contraseña.
     */
    fun updateUser(
        userId: Long,
        fullName: String,
        email: String,
        phone: String,
        document: String,
        documentType: String,
        jobTitle: String,
        branch: String,
        contractType: String,
        role: UserRole,
        location: String
    ): Int {
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DOCUMENT, document)
            put(COLUMN_DOCUMENT_TYPE, documentType)
            put(COLUMN_JOB_TITLE, jobTitle)
            put(COLUMN_BRANCH, branch)
            put(COLUMN_CONTRACT_TYPE, contractType)
            put(COLUMN_ROLE, role.name)
            put(COLUMN_LOCATION, location)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        return database.writableDatabase.update(
            TABLE_USERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }

    // Elimina una cuenta por id. Devuelve el número de filas afectadas
    fun deleteById(userId: Long): Int {
        return database.writableDatabase.delete(
            TABLE_USERS,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }

    // Actualiza únicamente el hash y la sal de la contraseña
    fun updatePassword(
        userId: Long,
        passwordHash: String,
        passwordSalt: String
    ) {
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD_HASH, passwordHash)
            put(COLUMN_PASSWORD_SALT, passwordSalt)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        database.writableDatabase.update(
            TABLE_USERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }

    // Activa o desactiva el ingreso con huella para una cuenta
    fun setBiometricEnabled(userId: Long, enabled: Boolean) {
        val values = ContentValues().apply {
            put(COLUMN_BIOMETRIC_ENABLED, if (enabled) 1 else 0)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        database.writableDatabase.update(
            TABLE_USERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }

    // Cuenta los usuarios registrados (útil para los indicadores del panel)
    fun count(): Int {
        database.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_USERS",
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun queryOne(
        selection: String,
        arguments: Array<String>
    ): StoredUser? {
        database.readableDatabase.query(
            TABLE_USERS,
            ALL_COLUMNS,
            selection,
            arguments,
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.toStoredUser()
            } else {
                null
            }
        }
    }

    // Convierte la fila actual del cursor en el modelo de dominio
    private fun Cursor.toUser(): User {
        return User(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            fullName = getString(getColumnIndexOrThrow(COLUMN_FULL_NAME)),
            email = getString(getColumnIndexOrThrow(COLUMN_EMAIL)),
            phone = getString(getColumnIndexOrThrow(COLUMN_PHONE)).orEmpty(),
            document = getString(getColumnIndexOrThrow(COLUMN_DOCUMENT)).orEmpty(),
            documentType = getString(
                getColumnIndexOrThrow(COLUMN_DOCUMENT_TYPE)
            ).orEmpty(),
            jobTitle = getString(
                getColumnIndexOrThrow(COLUMN_JOB_TITLE)
            ).orEmpty(),
            branch = getString(getColumnIndexOrThrow(COLUMN_BRANCH)).orEmpty(),
            contractType = getString(
                getColumnIndexOrThrow(COLUMN_CONTRACT_TYPE)
            ).orEmpty(),
            role = UserRole.fromStorage(
                getString(getColumnIndexOrThrow(COLUMN_ROLE))
            ),
            location = getString(getColumnIndexOrThrow(COLUMN_LOCATION)).orEmpty(),
            active = getInt(getColumnIndexOrThrow(COLUMN_ACTIVE)) == 1,
            biometricEnabled =
                getInt(getColumnIndexOrThrow(COLUMN_BIOMETRIC_ENABLED)) == 1,
            createdAt = getLong(getColumnIndexOrThrow(COLUMN_CREATED_AT))
        )
    }

    // Convierte la fila en el modelo con credenciales para el login
    private fun Cursor.toStoredUser(): StoredUser {
        return StoredUser(
            user = toUser(),
            passwordHash = getString(
                getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)
            ),
            passwordSalt = getString(
                getColumnIndexOrThrow(COLUMN_PASSWORD_SALT)
            )
        )
    }

    companion object {
        const val TABLE_USERS = "users"

        const val COLUMN_ID = "id"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DOCUMENT = "document_number"
        const val COLUMN_DOCUMENT_TYPE = "document_type"
        const val COLUMN_JOB_TITLE = "job_title"
        const val COLUMN_BRANCH = "branch"
        const val COLUMN_CONTRACT_TYPE = "contract_type"
        const val COLUMN_ROLE = "role"
        const val COLUMN_PASSWORD_HASH = "password_hash"
        const val COLUMN_PASSWORD_SALT = "password_salt"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_BIOMETRIC_ENABLED = "biometric_enabled"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_FULL_NAME,
            COLUMN_EMAIL,
            COLUMN_PHONE,
            COLUMN_DOCUMENT,
            COLUMN_DOCUMENT_TYPE,
            COLUMN_JOB_TITLE,
            COLUMN_BRANCH,
            COLUMN_CONTRACT_TYPE,
            COLUMN_ROLE,
            COLUMN_PASSWORD_HASH,
            COLUMN_PASSWORD_SALT,
            COLUMN_LOCATION,
            COLUMN_ACTIVE,
            COLUMN_BIOMETRIC_ENABLED,
            COLUMN_CREATED_AT
        )
    }
}
