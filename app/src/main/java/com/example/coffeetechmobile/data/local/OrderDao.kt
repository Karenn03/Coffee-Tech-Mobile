package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.coffeetechmobile.data.model.StoredCartItem
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.model.StoredOrderItem

/**
 * Acceso a las tablas `orders` y `order_items`
 *
 * La creación de una orden y el vaciado del carrito se hace en una sola
 * transacción desde [com.example.coffeetechmobile.data.repository.OrderRepository]
 */
class OrderDao(private val database: CoffeeTechDatabase) {

    /**
     * Guarda una orden con la copia de sus productos.
     *
     * Devuelve el número visible del pedido, por ejemplo `CT-1001`
     */
    fun createOrder(
        userId: Long,
        customerName: String,
        deliveryAddress: String,
        deliveryType: String,
        paymentMethod: String,
        subtotal: Int,
        discount: Int,
        shipping: Int,
        total: Int,
        customerNote: String,
        items: List<StoredCartItem>,
        createdAt: Long
    ): String {
        val db = database.writableDatabase

        db.beginTransaction()

        try {
            val nextId = nextOrderId(db)

            // Número visible y estable para el historial del vendedor
            val orderNumber = "CT-${ORDER_NUMBER_BASE + nextId}"

            val orderValues = ContentValues().apply {
                put(COLUMN_ORDER_NUMBER, orderNumber)
                put(COLUMN_USER_ID, userId)
                put(COLUMN_CUSTOMER_NAME, customerName)
                put(COLUMN_DELIVERY_ADDRESS, deliveryAddress)
                put(COLUMN_DELIVERY_TYPE, deliveryType)
                put(COLUMN_PAYMENT_METHOD, paymentMethod)
                put(COLUMN_SUBTOTAL, subtotal)
                put(COLUMN_DISCOUNT, discount)
                put(COLUMN_SHIPPING, shipping)
                put(COLUMN_TOTAL, total)
                put(COLUMN_STATUS, STATUS_RECEIVED)
                put(COLUMN_CUSTOMER_NOTE, customerNote)
                put(COLUMN_CREATED_AT, createdAt)
            }

            val orderId = db.insertOrThrow(
                TABLE_ORDERS,
                null,
                orderValues
            )

            items.forEach { item ->
                val itemValues = ContentValues().apply {
                    put(COLUMN_ORDER_ID, orderId)
                    put(COLUMN_ORDER_PRODUCT_ID, item.productId)
                    put(COLUMN_PRODUCT_NAME, item.name)
                    put(COLUMN_PRODUCT_DESCRIPTION, item.description)
                    put(COLUMN_UNIT_PRICE, item.unitPrice)
                    put(COLUMN_QUANTITY, item.quantity)
                    put(COLUMN_IMAGE_RES_NAME, item.imageResName)
                    put(COLUMN_IMAGE_PATH, item.imagePath)
                }

                db.insertOrThrow(
                    TABLE_ORDER_ITEMS,
                    null,
                    itemValues
                )
            }

            // El carrito se vacía en la misma transacción que crea la orden
            db.delete(
                CartDao.TABLE_CART_ITEMS,
                "${CartDao.COLUMN_USER_ID} = ?",
                arrayOf(userId.toString())
            )

            db.setTransactionSuccessful()

            return orderNumber
        } finally {
            db.endTransaction()
        }
    }

    // Devuelve todas las órdenes de la más reciente a la más antigua
    fun findAll(): List<StoredOrder> {
        val orders = mutableListOf<StoredOrder>()

        database.readableDatabase.query(
            TABLE_ORDERS,
            ALL_ORDER_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC, $COLUMN_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                orders.add(cursor.toStoredOrder())
            }
        }

        return orders.map { order ->
            order.copy(items = findItems(order.id))
        }
    }

    // Actualiza el estado de un pedido (Recibido → Preparando → Listo → Entregado)
    fun updateStatus(orderId: Long, status: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_STATUS, status)
        }

        return database.writableDatabase.update(
            TABLE_ORDERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(orderId.toString())
        )
    }

    // Devuelve los productos de una orden puntual
    private fun findItems(orderId: Long): List<StoredOrderItem> {
        val items = mutableListOf<StoredOrderItem>()

        database.readableDatabase.query(
            TABLE_ORDER_ITEMS,
            ALL_ITEM_COLUMNS,
            "$COLUMN_ORDER_ID = ?",
            arrayOf(orderId.toString()),
            null,
            null,
            "$COLUMN_ID ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(cursor.toStoredOrderItem())
            }
        }

        return items
    }

    // Calcula el siguiente id y así conservar una numeración consecutiva
    private fun nextOrderId(db: SQLiteDatabase): Long {
        db.rawQuery(
            "SELECT IFNULL(MAX($COLUMN_ID), 0) + 1 FROM $TABLE_ORDERS",
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getLong(0)
            } else {
                1L
            }
        }
    }

    private fun Cursor.toStoredOrder(): StoredOrder {
        val id = getLong(getColumnIndexOrThrow(COLUMN_ID))

        val userIdIndex = getColumnIndexOrThrow(COLUMN_USER_ID)

        return StoredOrder(
            id = id,
            orderNumber = getString(
                getColumnIndexOrThrow(COLUMN_ORDER_NUMBER)
            ),
            userId = if (isNull(userIdIndex)) {
                null
            } else {
                getLong(userIdIndex)
            },
            customerName = getString(
                getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)
            ),
            deliveryAddress = getString(
                getColumnIndexOrThrow(COLUMN_DELIVERY_ADDRESS)
            ).orEmpty(),
            deliveryType = getString(
                getColumnIndexOrThrow(COLUMN_DELIVERY_TYPE)
            ).orEmpty(),
            paymentMethod = getString(
                getColumnIndexOrThrow(COLUMN_PAYMENT_METHOD)
            ).orEmpty(),
            subtotal = getInt(getColumnIndexOrThrow(COLUMN_SUBTOTAL)),
            discount = getInt(getColumnIndexOrThrow(COLUMN_DISCOUNT)),
            shipping = getInt(getColumnIndexOrThrow(COLUMN_SHIPPING)),
            total = getInt(getColumnIndexOrThrow(COLUMN_TOTAL)),
            status = getString(
                getColumnIndexOrThrow(COLUMN_STATUS)
            ).orEmpty(),
            customerNote = getString(
                getColumnIndexOrThrow(COLUMN_CUSTOMER_NOTE)
            ).orEmpty(),
            createdAt = getLong(
                getColumnIndexOrThrow(COLUMN_CREATED_AT)
            ),
            items = emptyList()
        )
    }

    private fun Cursor.toStoredOrderItem(): StoredOrderItem {
        val productIdIndex = getColumnIndexOrThrow(
            COLUMN_ORDER_PRODUCT_ID
        )

        return StoredOrderItem(
            productId = if (isNull(productIdIndex)) {
                null
            } else {
                getLong(productIdIndex)
            },
            name = getString(getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)),
            description = getString(
                getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)
            ).orEmpty(),
            unitPrice = getInt(getColumnIndexOrThrow(COLUMN_UNIT_PRICE)),
            quantity = getInt(getColumnIndexOrThrow(COLUMN_QUANTITY)),
            imageResName = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_RES_NAME)
            ).orEmpty(),
            imagePath = getString(
                getColumnIndexOrThrow(COLUMN_IMAGE_PATH)
            ).orEmpty()
        )
    }

    companion object {
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_ITEMS = "order_items"

        const val COLUMN_ID = "id"
        const val COLUMN_ORDER_NUMBER = "order_number"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_CUSTOMER_NAME = "customer_name"
        const val COLUMN_DELIVERY_ADDRESS = "delivery_address"
        const val COLUMN_DELIVERY_TYPE = "delivery_type"
        const val COLUMN_PAYMENT_METHOD = "payment_method"
        const val COLUMN_SUBTOTAL = "subtotal"
        const val COLUMN_DISCOUNT = "discount"
        const val COLUMN_SHIPPING = "shipping"
        const val COLUMN_TOTAL = "total"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CUSTOMER_NOTE = "customer_note"
        const val COLUMN_CREATED_AT = "created_at"

        const val COLUMN_ORDER_ID = "order_id"
        const val COLUMN_ORDER_PRODUCT_ID = "product_id"
        const val COLUMN_PRODUCT_NAME = "name"
        const val COLUMN_PRODUCT_DESCRIPTION = "description"
        const val COLUMN_UNIT_PRICE = "unit_price"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_IMAGE_RES_NAME = "image_res_name"
        const val COLUMN_IMAGE_PATH = "image_path"

        const val STATUS_RECEIVED = "RECEIVED"

        private const val ORDER_NUMBER_BASE = 1000

        private val ALL_ORDER_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_ORDER_NUMBER,
            COLUMN_USER_ID,
            COLUMN_CUSTOMER_NAME,
            COLUMN_DELIVERY_ADDRESS,
            COLUMN_DELIVERY_TYPE,
            COLUMN_PAYMENT_METHOD,
            COLUMN_SUBTOTAL,
            COLUMN_DISCOUNT,
            COLUMN_SHIPPING,
            COLUMN_TOTAL,
            COLUMN_STATUS,
            COLUMN_CUSTOMER_NOTE,
            COLUMN_CREATED_AT
        )

        private val ALL_ITEM_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_ORDER_ID,
            COLUMN_ORDER_PRODUCT_ID,
            COLUMN_PRODUCT_NAME,
            COLUMN_PRODUCT_DESCRIPTION,
            COLUMN_UNIT_PRICE,
            COLUMN_QUANTITY,
            COLUMN_IMAGE_RES_NAME,
            COLUMN_IMAGE_PATH
        )
    }
}
