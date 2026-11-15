package com.example.coffeetechmobile.data.local

import android.content.ContentValues
import android.database.Cursor
import com.example.coffeetechmobile.data.model.DeliveryAddress

/**
 * Acceso a la tabla `addresses`
 *
 * Cada comprador puede guardar varias direcciones (Casa, Trabajo, etc.)
 * y seleccionar una como su dirección de entrega activa
 */
class AddressDao(private val database: CoffeeTechDatabase) {

    fun findAll(userId: Long): List<DeliveryAddress> {
        val addresses = mutableListOf<DeliveryAddress>()

        database.readableDatabase.query(
            TABLE_ADDRESSES,
            ALL_COLUMNS,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                addresses.add(cursor.toDeliveryAddress())
            }
        }

        return addresses
    }

    fun findById(
        userId: Long,
        addressId: Long
    ): DeliveryAddress? {
        database.readableDatabase.query(
            TABLE_ADDRESSES,
            ALL_COLUMNS,
            "$COLUMN_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(addressId.toString(), userId.toString()),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.toDeliveryAddress()
            } else {
                null
            }
        }
    }

    fun insert(
        userId: Long,
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        createdAt: Long
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_LABEL, label)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
            put(COLUMN_CREATED_AT, createdAt)
            put(COLUMN_UPDATED_AT, createdAt)
        }

        return database.writableDatabase.insertOrThrow(
            TABLE_ADDRESSES,
            null,
            values
        )
    }

    fun delete(userId: Long, addressId: Long): Int {
        return database.writableDatabase.delete(
            TABLE_ADDRESSES,
            "$COLUMN_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(addressId.toString(), userId.toString())
        )
    }

    fun count(userId: Long): Int {
        database.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_ADDRESSES " +
                "WHERE $COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun Cursor.toDeliveryAddress(): DeliveryAddress {
        return DeliveryAddress(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            label = getString(getColumnIndexOrThrow(COLUMN_LABEL)),
            address = getString(getColumnIndexOrThrow(COLUMN_ADDRESS)),
            latitude = getDouble(getColumnIndexOrThrow(COLUMN_LATITUDE)),
            longitude = getDouble(getColumnIndexOrThrow(COLUMN_LONGITUDE))
        )
    }

    companion object {
        const val TABLE_ADDRESSES = "addresses"

        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_LABEL = "label"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_USER_ID,
            COLUMN_LABEL,
            COLUMN_ADDRESS,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE,
            COLUMN_CREATED_AT
        )
    }
}
