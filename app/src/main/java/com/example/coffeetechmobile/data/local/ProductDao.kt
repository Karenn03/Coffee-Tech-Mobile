package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.database.Cursor
import com.example.coffeetechmobile.data.model.Product

/**
 * Acceso a la tabla `products`
 *
 * Todas las consultas son síncronas; el repositorio las ejecuta en
 * Dispatchers.IO para no bloquear la interfaz
 */
class ProductDao(private val database: CoffeeTechDatabase) {

    // Inserta un producto y devuelve su id generado
    fun insert(
        name: String,
        category: String,
        description: String,
        salePrice: Int,
        preparationCost: Int,
        taxRate: Int,
        stock: Int,
        minimumStock: Int,
        imagePath: String,
        imageResName: String,
        createdAt: Long
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_SALE_PRICE, salePrice)
            put(COLUMN_PREPARATION_COST, preparationCost)
            put(COLUMN_TAX_RATE, taxRate)
            put(COLUMN_STOCK, stock)
            put(COLUMN_MINIMUM_STOCK, minimumStock)
            put(COLUMN_IMAGE_PATH, imagePath)
            put(COLUMN_IMAGE_RES_NAME, imageResName)
            put(COLUMN_ACTIVE, 1)
            put(COLUMN_CREATED_AT, createdAt)
            put(COLUMN_UPDATED_AT, createdAt)
        }

        return database.writableDatabase.insertOrThrow(
            TABLE_PRODUCTS,
            null,
            values
        )
    }

    // Devuelve todos los productos ordenados por nombre
    fun findAll(): List<Product> {
        val products = mutableListOf<Product>()

        database.readableDatabase.query(
            TABLE_PRODUCTS,
            ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                products.add(cursor.toProduct())
            }
        }

        return products
    }

    // Busca un producto por su id
    fun findById(productId: Long): Product? {
        database.readableDatabase.query(
            TABLE_PRODUCTS,
            ALL_COLUMNS,
            "$COLUMN_ID = ?",
            arrayOf(productId.toString()),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.toProduct()
            } else {
                null
            }
        }
    }

    // Actualiza los campos editables del producto
    fun update(
        productId: Long,
        name: String,
        category: String,
        description: String,
        salePrice: Int,
        preparationCost: Int,
        taxRate: Int,
        stock: Int,
        minimumStock: Int,
        imagePath: String,
        imageResName: String
    ): Int {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_SALE_PRICE, salePrice)
            put(COLUMN_PREPARATION_COST, preparationCost)
            put(COLUMN_TAX_RATE, taxRate)
            put(COLUMN_STOCK, stock)
            put(COLUMN_MINIMUM_STOCK, minimumStock)
            put(COLUMN_IMAGE_PATH, imagePath)
            put(COLUMN_IMAGE_RES_NAME, imageResName)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        return database.writableDatabase.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(productId.toString())
        )
    }

    // Elimina un producto y devuelve el número de filas afectadas
    fun deleteById(productId: Long): Int {
        return database.writableDatabase.delete(
            TABLE_PRODUCTS,
            "$COLUMN_ID = ?",
            arrayOf(productId.toString())
        )
    }

    // Convierte la fila actual en el modelo de dominio
    private fun Cursor.toProduct(): Product {
        return Product(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
            category = getString(getColumnIndexOrThrow(COLUMN_CATEGORY)),
            description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            salePrice = getInt(getColumnIndexOrThrow(COLUMN_SALE_PRICE)),
            preparationCost = getInt(
                getColumnIndexOrThrow(COLUMN_PREPARATION_COST)
            ),
            taxRate = getInt(getColumnIndexOrThrow(COLUMN_TAX_RATE)),
            stock = getInt(getColumnIndexOrThrow(COLUMN_STOCK)),
            minimumStock = getInt(
                getColumnIndexOrThrow(COLUMN_MINIMUM_STOCK)
            ),
            imagePath = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_PATH)
            ).orEmpty(),
            imageResName = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_RES_NAME)
            ).orEmpty(),
            active = getInt(getColumnIndexOrThrow(COLUMN_ACTIVE)) == 1,
            createdAt = getLong(getColumnIndexOrThrow(COLUMN_CREATED_AT))
        )
    }

    companion object {
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_SALE_PRICE = "sale_price"
        const val COLUMN_PREPARATION_COST = "preparation_cost"
        const val COLUMN_TAX_RATE = "tax_rate"
        const val COLUMN_STOCK = "stock"
        const val COLUMN_MINIMUM_STOCK = "minimum_stock"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_IMAGE_RES_NAME = "image_res_name"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_CATEGORY,
            COLUMN_DESCRIPTION,
            COLUMN_SALE_PRICE,
            COLUMN_PREPARATION_COST,
            COLUMN_TAX_RATE,
            COLUMN_STOCK,
            COLUMN_MINIMUM_STOCK,
            COLUMN_IMAGE_PATH,
            COLUMN_IMAGE_RES_NAME,
            COLUMN_ACTIVE,
            COLUMN_CREATED_AT
        )
    }
}
