package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.database.Cursor
import com.example.coffeetechmobile.data.model.StoredCartItem

/**
 * Acceso a la tabla `cart_items`
 *
 * Cada fila representa un producto con una variante concreta (tamaño, leche,
 * etc.) dentro del carrito de un comprador. Las operaciones son síncronas; el
 * repositorio las ejecuta en Dispatchers.IO
 */
class CartDao(private val database: CoffeeTechDatabase) {

    // Devuelve el carrito completo de un comprador
    fun findAll(userId: Long): List<StoredCartItem> {
        val items = mutableListOf<StoredCartItem>()

        database.readableDatabase.query(
            TABLE_CART_ITEMS,
            ALL_COLUMNS,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_CREATED_AT ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(cursor.toStoredCartItem())
            }
        }

        return items
    }

    /**
     * Agrega un producto al carrito.
     *
     * Si el comprador ya tiene ese producto con la misma variante, se suma la
     * cantidad y se actualizan los datos visibles (precio, imagen, etc.)
     */
    fun addOrIncrement(
        userId: Long,
        productId: Long,
        variantKey: String,
        name: String,
        description: String,
        unitPrice: Int,
        imageResName: String,
        imagePath: String,
        quantity: Int
    ): Long {
        val db = database.writableDatabase
        val now = System.currentTimeMillis()

        db.beginTransaction()

        try {
            var cartItemId = NO_ITEM

            db.query(
                TABLE_CART_ITEMS,
                arrayOf(COLUMN_ID, COLUMN_QUANTITY),
                "$COLUMN_USER_ID = ? AND $COLUMN_PRODUCT_ID = ? " +
                    "AND $COLUMN_VARIANT_KEY = ?",
                arrayOf(
                    userId.toString(),
                    productId.toString(),
                    variantKey
                ),
                null,
                null,
                null,
                "1"
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val currentId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COLUMN_ID)
                    )

                    val currentQuantity = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)
                    )

                    val values = ContentValues().apply {
                        put(
                            COLUMN_QUANTITY,
                            currentQuantity + quantity
                        )
                        put(COLUMN_NAME, name)
                        put(COLUMN_DESCRIPTION, description)
                        put(COLUMN_UNIT_PRICE, unitPrice)
                        put(COLUMN_IMAGE_RES_NAME, imageResName)
                        put(COLUMN_IMAGE_PATH, imagePath)
                        put(COLUMN_UPDATED_AT, now)
                    }

                    db.update(
                        TABLE_CART_ITEMS,
                        values,
                        "$COLUMN_ID = ?",
                        arrayOf(currentId.toString())
                    )

                    cartItemId = currentId
                }
            }

            if (cartItemId == NO_ITEM) {
                val values = ContentValues().apply {
                    put(COLUMN_USER_ID, userId)
                    put(COLUMN_PRODUCT_ID, productId)
                    put(COLUMN_VARIANT_KEY, variantKey)
                    put(COLUMN_NAME, name)
                    put(COLUMN_DESCRIPTION, description)
                    put(COLUMN_UNIT_PRICE, unitPrice)
                    put(COLUMN_IMAGE_RES_NAME, imageResName)
                    put(COLUMN_IMAGE_PATH, imagePath)
                    put(COLUMN_QUANTITY, quantity)
                    put(COLUMN_CREATED_AT, now)
                    put(COLUMN_UPDATED_AT, now)
                }

                cartItemId = db.insertOrThrow(
                    TABLE_CART_ITEMS,
                    null,
                    values
                )
            }

            db.setTransactionSuccessful()

            return cartItemId
        } finally {
            db.endTransaction()
        }
    }

    // Suma una unidad a un producto del carrito
    fun increaseQuantity(userId: Long, cartItemId: Long) {
        database.writableDatabase.execSQL(
            "UPDATE $TABLE_CART_ITEMS " +
                "SET $COLUMN_QUANTITY = $COLUMN_QUANTITY + 1, " +
                "$COLUMN_UPDATED_AT = ? " +
                "WHERE $COLUMN_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(
                System.currentTimeMillis(),
                cartItemId,
                userId
            )
        )
    }

    // Resta una unidad. Si el producto queda en cero se elimina del carrito
    fun decreaseQuantity(userId: Long, cartItemId: Long) {
        val db = database.writableDatabase

        db.beginTransaction()

        try {
            db.execSQL(
                "UPDATE $TABLE_CART_ITEMS " +
                    "SET $COLUMN_QUANTITY = $COLUMN_QUANTITY - 1, " +
                    "$COLUMN_UPDATED_AT = ? " +
                    "WHERE $COLUMN_ID = ? AND $COLUMN_USER_ID = ? " +
                    "AND $COLUMN_QUANTITY > 1",
                arrayOf(
                    System.currentTimeMillis(),
                    cartItemId,
                    userId
                )
            )

            db.delete(
                TABLE_CART_ITEMS,
                "$COLUMN_ID = ? AND $COLUMN_USER_ID = ? " +
                    "AND $COLUMN_QUANTITY <= 1",
                arrayOf(cartItemId.toString(), userId.toString())
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Elimina un producto puntual del carrito
    fun delete(userId: Long, cartItemId: Long): Int {
        return database.writableDatabase.delete(
            TABLE_CART_ITEMS,
            "$COLUMN_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(cartItemId.toString(), userId.toString())
        )
    }

    // Vacía el carrito (después de confirmar una orden)
    fun clear(userId: Long): Int {
        return database.writableDatabase.delete(
            TABLE_CART_ITEMS,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
    }

    // Convierte la fila actual en el modelo guardado del carrito
    private fun Cursor.toStoredCartItem(): StoredCartItem {
        return StoredCartItem(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            productId = getLong(
                getColumnIndexOrThrow(COLUMN_PRODUCT_ID)
            ),
            variantKey = getString(
                getColumnIndexOrThrow(COLUMN_VARIANT_KEY)
            ).orEmpty(),
            name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
            description = getString(
                getColumnIndexOrThrow(COLUMN_DESCRIPTION)
            ).orEmpty(),
            unitPrice = getInt(
                getColumnIndexOrThrow(COLUMN_UNIT_PRICE)
            ),
            imageResName = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_RES_NAME)
            ).orEmpty(),
            imagePath = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_PATH)
            ).orEmpty(),
            quantity = getInt(
                getColumnIndexOrThrow(COLUMN_QUANTITY)
            )
        )
    }

    companion object {
        const val TABLE_CART_ITEMS = "cart_items"

        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_PRODUCT_ID = "product_id"
        const val COLUMN_VARIANT_KEY = "variant_key"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_UNIT_PRICE = "unit_price"
        const val COLUMN_IMAGE_RES_NAME = "image_res_name"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        private const val NO_ITEM = -1L

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_USER_ID,
            COLUMN_PRODUCT_ID,
            COLUMN_VARIANT_KEY,
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_UNIT_PRICE,
            COLUMN_IMAGE_RES_NAME,
            COLUMN_IMAGE_PATH,
            COLUMN_QUANTITY,
            COLUMN_CREATED_AT
        )
    }
}
