package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.security.PasswordHasher

class CoffeeTechDatabase private constructor(
    context: Context
) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onConfigure(database: SQLiteDatabase) {
        super.onConfigure(database)
        database.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(database: SQLiteDatabase) {
        createUsersTable(database)
        seedDemoUsers(database)
    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Las migraciones se irán agregando aquí
        // Importante: nunca recrear `users` para no perder los usuarios

        if (oldVersion < 2) {
            // Campos usados por el CRUD de usuarios del administrador
            database.execSQL(
                "ALTER TABLE ${UserDao.TABLE_USERS} " +
                    "ADD COLUMN ${UserDao.COLUMN_DOCUMENT_TYPE} TEXT NOT NULL DEFAULT 'CC (Cédula)'"
            )
            database.execSQL(
                "ALTER TABLE ${UserDao.TABLE_USERS} " +
                    "ADD COLUMN ${UserDao.COLUMN_JOB_TITLE} TEXT NOT NULL DEFAULT 'Cliente'"
            )
            database.execSQL(
                "ALTER TABLE ${UserDao.TABLE_USERS} " +
                    "ADD COLUMN ${UserDao.COLUMN_BRANCH} TEXT NOT NULL DEFAULT ''"
            )
            database.execSQL(
                "ALTER TABLE ${UserDao.TABLE_USERS} " +
                    "ADD COLUMN ${UserDao.COLUMN_CONTRACT_TYPE} TEXT NOT NULL DEFAULT ''"
            )

            // Conserva un cargo visible para las cuentas que ya existían
            database.execSQL(
                """
                UPDATE ${UserDao.TABLE_USERS}
                SET ${UserDao.COLUMN_JOB_TITLE} = CASE ${UserDao.COLUMN_ROLE}
                    WHEN 'ADMIN' THEN 'Gerente'
                    WHEN 'SELLER' THEN 'Barista'
                    ELSE 'Cliente'
                END
                """.trimIndent()
            )
        }
    }

    // Crea la tabla de usuarios
    // El correo es único y no distingue mayúsculas/minúsculas
    private fun createUsersTable(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE ${UserDao.TABLE_USERS} (
                ${UserDao.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${UserDao.COLUMN_FULL_NAME} TEXT NOT NULL,
                ${UserDao.COLUMN_EMAIL} TEXT NOT NULL COLLATE NOCASE UNIQUE,
                ${UserDao.COLUMN_PHONE} TEXT NOT NULL DEFAULT '',
                ${UserDao.COLUMN_DOCUMENT} TEXT NOT NULL DEFAULT '',
                ${UserDao.COLUMN_DOCUMENT_TYPE} TEXT NOT NULL DEFAULT 'CC (Cédula)',
                ${UserDao.COLUMN_JOB_TITLE} TEXT NOT NULL DEFAULT 'Cliente',
                ${UserDao.COLUMN_BRANCH} TEXT NOT NULL DEFAULT '',
                ${UserDao.COLUMN_CONTRACT_TYPE} TEXT NOT NULL DEFAULT '',
                ${UserDao.COLUMN_ROLE} TEXT NOT NULL DEFAULT 'BUYER'
                    CHECK (${UserDao.COLUMN_ROLE} IN ('ADMIN', 'SELLER', 'BUYER')),
                ${UserDao.COLUMN_PASSWORD_HASH} TEXT NOT NULL,
                ${UserDao.COLUMN_PASSWORD_SALT} TEXT NOT NULL,
                ${UserDao.COLUMN_LOCATION} TEXT NOT NULL DEFAULT '',
                ${UserDao.COLUMN_ACTIVE} INTEGER NOT NULL DEFAULT 1,
                ${UserDao.COLUMN_BIOMETRIC_ENABLED} INTEGER NOT NULL DEFAULT 0,
                ${UserDao.COLUMN_CREATED_AT} INTEGER NOT NULL,
                ${UserDao.COLUMN_UPDATED_AT} INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    /**
     * Crea los usuarios de demostración de los tres roles
     *
     * Las contraseñas se guardan con PBKDF2, igual que las cuentas creadas
     * desde la aplicación. Estas cuentas permiten probar el enrutamiento por
     * rol
     */
    private fun seedDemoUsers(database: SQLiteDatabase) {
        val hasher = PasswordHasher()
        val now = System.currentTimeMillis()

        insertSeedUser(
            database = database,
            hasher = hasher,
            fullName = "Administrador Coffee Tech",
            email = "admin@coffeetech.com",
            password = "Admin123*",
            role = UserRole.ADMIN,
            jobTitle = "Gerente",
            documentType = "CC (Cédula)",
            branch = "Bogotá - Usaquén",
            contractType = "Término indefinido",
            location = "Sede principal",
            createdAt = now
        )

        insertSeedUser(
            database = database,
            hasher = hasher,
            fullName = "Vendedor Coffee Tech",
            email = "vendedor@coffeetech.com",
            password = "Vendedor123*",
            role = UserRole.SELLER,
            jobTitle = "Barista",
            documentType = "CC (Cédula)",
            branch = "Bogotá - Chapinero",
            contractType = "Término indefinido",
            location = "Sede Chapinero Alto",
            createdAt = now
        )

        insertSeedUser(
            database = database,
            hasher = hasher,
            fullName = "Cliente Coffee Tech",
            email = "cliente@coffeetech.com",
            password = "Cliente123*",
            role = UserRole.BUYER,
            jobTitle = "Cliente",
            documentType = "CC (Cédula)",
            branch = "",
            contractType = "",
            location = "Bogotá, Colombia",
            createdAt = now
        )
    }

    private fun insertSeedUser(
        database: SQLiteDatabase,
        hasher: PasswordHasher,
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        jobTitle: String,
        documentType: String,
        branch: String,
        contractType: String,
        location: String,
        createdAt: Long
    ) {
        val salt = hasher.generateSalt()
        val hash = hasher.hash(password, salt)

        val values = ContentValues().apply {
            put(UserDao.COLUMN_FULL_NAME, fullName)
            put(UserDao.COLUMN_EMAIL, email)
            put(UserDao.COLUMN_PHONE, "")
            put(UserDao.COLUMN_DOCUMENT, "")
            put(UserDao.COLUMN_DOCUMENT_TYPE, documentType)
            put(UserDao.COLUMN_JOB_TITLE, jobTitle)
            put(UserDao.COLUMN_BRANCH, branch)
            put(UserDao.COLUMN_CONTRACT_TYPE, contractType)
            put(UserDao.COLUMN_ROLE, role.name)
            put(UserDao.COLUMN_PASSWORD_HASH, hash)
            put(UserDao.COLUMN_PASSWORD_SALT, salt)
            put(UserDao.COLUMN_LOCATION, location)
            put(UserDao.COLUMN_ACTIVE, 1)
            put(UserDao.COLUMN_BIOMETRIC_ENABLED, 0)
            put(UserDao.COLUMN_CREATED_AT, createdAt)
            put(UserDao.COLUMN_UPDATED_AT, createdAt)
        }

        database.insertWithOnConflict(
            UserDao.TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    companion object {
        private const val DATABASE_NAME = "coffeetech.db"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var instance: CoffeeTechDatabase? = null

        // Devuelve una única instancia de la base para toda la aplicación
        fun getInstance(context: Context): CoffeeTechDatabase {
            return instance ?: synchronized(this) {
                instance ?: CoffeeTechDatabase(context).also {
                    instance = it
                }
            }
        }
    }
}
