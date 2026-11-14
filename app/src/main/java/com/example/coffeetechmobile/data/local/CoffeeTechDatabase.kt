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
        createProductsTable(database)
        seedProducts(database)
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

        if (oldVersion < 3) {
            // Menú de productos administrado desde el CRUD del gerente
            createProductsTable(database)
            seedProducts(database)
        }

        if (oldVersion < 4) {
            // Unifica los nombres de categoría con la segunda palabra en
            // minúscula, tal como aparecen en el menú oficial
            database.execSQL(
                """
                UPDATE ${ProductDao.TABLE_PRODUCTS}
                SET ${ProductDao.COLUMN_CATEGORY} = 'Panadería y pastelería'
                WHERE ${ProductDao.COLUMN_CATEGORY} = 'Panadería y Pastelería'
                """.trimIndent()
            )
            database.execSQL(
                """
                UPDATE ${ProductDao.TABLE_PRODUCTS}
                SET ${ProductDao.COLUMN_CATEGORY} = 'Desayunos y brunch'
                WHERE ${ProductDao.COLUMN_CATEGORY} = 'Desayunos y Brunch'
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

    // Crea la tabla del menú de productos
    private fun createProductsTable(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE ${ProductDao.TABLE_PRODUCTS} (
                ${ProductDao.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ProductDao.COLUMN_NAME} TEXT NOT NULL,
                ${ProductDao.COLUMN_CATEGORY} TEXT NOT NULL,
                ${ProductDao.COLUMN_DESCRIPTION} TEXT NOT NULL,
                ${ProductDao.COLUMN_SALE_PRICE} INTEGER NOT NULL,
                ${ProductDao.COLUMN_PREPARATION_COST} INTEGER NOT NULL,
                ${ProductDao.COLUMN_TAX_RATE} INTEGER NOT NULL,
                ${ProductDao.COLUMN_STOCK} INTEGER NOT NULL,
                ${ProductDao.COLUMN_MINIMUM_STOCK} INTEGER NOT NULL,
                ${ProductDao.COLUMN_IMAGE_PATH} TEXT NOT NULL DEFAULT '',
                ${ProductDao.COLUMN_IMAGE_RES_NAME} TEXT NOT NULL DEFAULT '',
                ${ProductDao.COLUMN_ACTIVE} INTEGER NOT NULL DEFAULT 1,
                ${ProductDao.COLUMN_CREATED_AT} INTEGER NOT NULL,
                ${ProductDao.COLUMN_UPDATED_AT} INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    // Datos de los productos semilla
    private data class SeedProduct(
        val name: String,
        val category: String,
        val description: String,
        val salePrice: Int,
        val preparationCost: Int,
        val taxRate: Int,
        val stock: Int,
        val minimumStock: Int,
        val imageResName: String
    )

    /**
     * Inserta el menú inicial de Coffee Tech con las imágenes
     * que ya existen en `res/drawable`
     */
    private fun seedProducts(database: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        val products = listOf(
            SeedProduct(
                name = "Americano",
                category = "Bebidas calientes",
                description = "Un espresso diluido con agua caliente para crear una taza de café negro, suave y aromático. Perfecto para aquellos que disfrutan de un café menos concentrado.",
                salePrice = 5000,
                preparationCost = 2000,
                taxRate = 19,
                stock = 40,
                minimumStock = 10,
                imageResName = "img_americano"
            ),
            SeedProduct(
                name = "Cappuccino Americano",
                category = "Bebidas calientes",
                description = "Un clásico cappuccino preparado con una dosis de espresso, leche vaporizada y una generosa capa de espuma de leche. Este café es un equilibrio ideal entre lo fuerte y lo cremoso.",
                salePrice = 6500,
                preparationCost = 2600,
                taxRate = 19,
                stock = 40,
                minimumStock = 10,
                imageResName = "img_cappuccino_americano"
            ),
            SeedProduct(
                name = "Cappuccino",
                category = "Bebidas calientes",
                description = "Una combinación de un espresso fuerte con partes iguales de leche vaporizada y espuma de leche. Coronado con una espolvoreada de cacao o canela, favorito de los amantes del café.",
                salePrice = 6000,
                preparationCost = 2400,
                taxRate = 19,
                stock = 40,
                minimumStock = 10,
                imageResName = "img_cappuccino"
            ),
            SeedProduct(
                name = "Caramel Macchiato",
                category = "Bebidas calientes",
                description = "Un delicioso espresso combinado con leche vaporizada y un toque de vainilla, con un generoso chorrito de caramelo. Ideal para quienes disfrutan de un café dulce y cremoso.",
                salePrice = 7000,
                preparationCost = 2800,
                taxRate = 19,
                stock = 40,
                minimumStock = 10,
                imageResName = "img_caramel_macchiato"
            ),
            SeedProduct(
                name = "Espresso Doble",
                category = "Bebidas calientes",
                description = "Una porción doble de espresso, rica y fuerte, perfecta para un impulso de energía adicional o para los verdaderos amantes del café intenso.",
                salePrice = 5500,
                preparationCost = 2200,
                taxRate = 19,
                stock = 40,
                minimumStock = 10,
                imageResName = "img_espresso_doble"
            ),
            SeedProduct(
                name = "Frappé Mocca",
                category = "Bebidas frías",
                description = "Una deliciosa mezcla de café, chocolate y hielo, todo licuado hasta obtener una textura suave y cremosa. Perfecto para los amantes del café y el chocolate.",
                salePrice = 7000,
                preparationCost = 2800,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_frappe_mocca"
            ),
            SeedProduct(
                name = "Frappé Oreo",
                category = "Bebidas frías",
                description = "Una deliciosa combinación de espresso, chocolate caliente y leche vaporizada, coronada con crema batida. Perfecta para aquellos que aman el chocolate y el café.",
                salePrice = 8000,
                preparationCost = 3200,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_frappe_oreo"
            ),
            SeedProduct(
                name = "Iced Cappuccino",
                category = "Bebidas frías",
                description = "Un espresso frío combinado con leche vaporizada y cubierto con una capa de espuma, servido con hielo. Refrescante y con el equilibrio perfecto entre fuerte y cremoso.",
                salePrice = 6500,
                preparationCost = 2600,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_iced_cappuccino"
            ),
            SeedProduct(
                name = "Iced Caramel Latte",
                category = "Bebidas frías",
                description = "Café espresso mezclado con leche fría y un toque de caramelo, servido sobre hielo. Dulce, cremoso y refrescante.",
                salePrice = 7000,
                preparationCost = 2800,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_iced_caramel_latte"
            ),
            SeedProduct(
                name = "Iced Caramel Macchiato",
                category = "Bebidas frías",
                description = "Un espresso frío con leche, vainilla y un generoso chorrito de caramelo por encima, servido con hielo. Un deleite visual y gustativo.",
                salePrice = 7500,
                preparationCost = 3000,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_iced_caramel_macchiato"
            ),
            SeedProduct(
                name = "Iced Macchiato",
                category = "Bebidas frías",
                description = "Espresso manchado con leche fría y servido con hielo. Una opción ligera y refrescante para los amantes del café.",
                salePrice = 6500,
                preparationCost = 2600,
                taxRate = 19,
                stock = 35,
                minimumStock = 8,
                imageResName = "img_iced_macchiato"
            ),
            SeedProduct(
                name = "Cheesecake de Fresa",
                category = "Panadería y pastelería",
                description = "Delicioso cheesecake cremoso con una base de galleta crujiente, cubierto con una generosa capa de fresas frescas y glaseado de fresa. Un clásico irresistible.",
                salePrice = 8000,
                preparationCost = 3200,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_cheesecake_fresa"
            ),
            SeedProduct(
                name = "Croissant de Almendras",
                category = "Panadería y pastelería",
                description = "Croissant hojaldrado y crujiente, relleno y cubierto con una rica pasta de almendras, y espolvoreado con almendras laminadas.",
                salePrice = 6000,
                preparationCost = 2400,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_croissant_almendras"
            ),
            SeedProduct(
                name = "Croissant de Chocolate",
                category = "Panadería y pastelería",
                description = "Croissant hojaldrado relleno y cubierto de un suave chocolate derretido, perfecto para los amantes del chocolate.",
                salePrice = 5500,
                preparationCost = 2200,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_croissant_chocolate"
            ),
            SeedProduct(
                name = "Croissant de Jamón y Queso",
                category = "Panadería y pastelería",
                description = "Croissant fresco y crujiente, relleno de jamón y queso derretido. Ideal para un desayuno o merienda.",
                salePrice = 6500,
                preparationCost = 2600,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_croissant_jamon_queso"
            ),
            SeedProduct(
                name = "Croissant de Mantequilla",
                category = "Panadería y pastelería",
                description = "Clásico croissant francés, ligero y hojaldrado, con un delicioso sabor a mantequilla. Perfecto para cualquier momento del día.",
                salePrice = 5000,
                preparationCost = 2000,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_croissant_mantequilla"
            ),
            SeedProduct(
                name = "Muffin de Red Velvet",
                category = "Panadería y pastelería",
                description = "Muffin suave y esponjoso, con el clásico color rojo y un toque de sabor a chocolate, coronado con un glaseado de queso crema.",
                salePrice = 5000,
                preparationCost = 2000,
                taxRate = 5,
                stock = 24,
                minimumStock = 6,
                imageResName = "img_muffin_red_velvet"
            ),
            SeedProduct(
                name = "Bagel de Jamón y Queso",
                category = "Desayunos y brunch",
                description = "Bagel tostado con una generosa porción de jamón y queso derretido, ideal para un desayuno o merienda rápida y deliciosa.",
                salePrice = 8000,
                preparationCost = 3200,
                taxRate = 19,
                stock = 20,
                minimumStock = 5,
                imageResName = "img_bagel_jamon_queso"
            ),
            SeedProduct(
                name = "Bagel de Queso Crema",
                category = "Desayunos y brunch",
                description = "Bagel tostado, recubierto con una capa generosa de queso crema. Un clásico simple y delicioso.",
                salePrice = 7000,
                preparationCost = 2800,
                taxRate = 19,
                stock = 20,
                minimumStock = 5,
                imageResName = "img_bagel_queso_crema"
            ),
            SeedProduct(
                name = "Bagel de Salmón Ahumado",
                category = "Desayunos y brunch",
                description = "Bagel tostado con queso crema y salmón ahumado. Un desayuno gourmet.",
                salePrice = 12000,
                preparationCost = 4800,
                taxRate = 19,
                stock = 20,
                minimumStock = 5,
                imageResName = "img_bagel_salmon_ahumado"
            ),
            SeedProduct(
                name = "Benedictinos",
                category = "Desayunos y brunch",
                description = "Huevos pochados sobre muffins ingleses, acompañados de jamón o salmón ahumado, cubiertos con salsa holandesa.",
                salePrice = 14000,
                preparationCost = 5600,
                taxRate = 19,
                stock = 20,
                minimumStock = 5,
                imageResName = "img_benedictinos"
            ),
            SeedProduct(
                name = "Bowl de Açaí",
                category = "Desayunos y brunch",
                description = "Refrescante bowl de açai mezclado con frutas frescas, granola y miel. Perfecto para un desayuno energético y saludable.",
                salePrice = 10000,
                preparationCost = 4000,
                taxRate = 19,
                stock = 20,
                minimumStock = 5,
                imageResName = "img_bowl_acai"
            )
        )

        products.forEach { product ->
            insertSeedProduct(database, product, now)
        }
    }

    private fun insertSeedProduct(
        database: SQLiteDatabase,
        product: SeedProduct,
        createdAt: Long
    ) {
        val values = ContentValues().apply {
            put(ProductDao.COLUMN_NAME, product.name)
            put(ProductDao.COLUMN_CATEGORY, product.category)
            put(ProductDao.COLUMN_DESCRIPTION, product.description)
            put(ProductDao.COLUMN_SALE_PRICE, product.salePrice)
            put(ProductDao.COLUMN_PREPARATION_COST, product.preparationCost)
            put(ProductDao.COLUMN_TAX_RATE, product.taxRate)
            put(ProductDao.COLUMN_STOCK, product.stock)
            put(ProductDao.COLUMN_MINIMUM_STOCK, product.minimumStock)
            put(ProductDao.COLUMN_IMAGE_PATH, "")
            put(ProductDao.COLUMN_IMAGE_RES_NAME, product.imageResName)
            put(ProductDao.COLUMN_ACTIVE, 1)
            put(ProductDao.COLUMN_CREATED_AT, createdAt)
            put(ProductDao.COLUMN_UPDATED_AT, createdAt)
        }

        database.insertWithOnConflict(
            ProductDao.TABLE_PRODUCTS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    companion object {
        private const val DATABASE_NAME = "coffeetech.db"
        private const val DATABASE_VERSION = 4

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
