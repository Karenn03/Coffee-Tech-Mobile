package com.example.coffeetechmobile.data.repository

import android.content.Context
import com.example.coffeetechmobile.data.local.AddressDao
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.local.UserDao
import com.example.coffeetechmobile.data.model.DeliveryAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Libreta de direcciones del comprador
 *
 * La dirección seleccionada se sincroniza con `users.location` y sus
 * coordenadas para que el checkout y la orden la usen sin pasos extra
 */
class AddressRepository(context: Context) {

    private val database = CoffeeTechDatabase.getInstance(context)
    private val addressDao = AddressDao(database)
    private val userDao = UserDao(database)

    suspend fun getAddresses(userId: Long): List<DeliveryAddress> =
        withContext(Dispatchers.IO) {
            ensureInitialAddress(userId)
            addressDao.findAll(userId)
        }

    suspend fun getSelectedAddress(userId: Long): DeliveryAddress? =
        withContext(Dispatchers.IO) {
            ensureInitialAddress(userId)

            val user = userDao.findById(userId)?.user ?: return@withContext null
            val selectedId = user.selectedAddressId
                ?: return@withContext null

            addressDao.findById(userId, selectedId)
        }

    suspend fun getSelectedAddressId(userId: Long): Long? =
        withContext(Dispatchers.IO) {
            ensureInitialAddress(userId)
            userDao.findById(userId)?.user?.selectedAddressId
        }

    suspend fun saveAddress(
        userId: Long,
        label: String,
        address: String,
        latitude: Double,
        longitude: Double
    ): DeliveryAddress? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        val addressId = addressDao.insert(
            userId = userId,
            label = label.trim(),
            address = address.trim(),
            latitude = latitude,
            longitude = longitude,
            createdAt = now
        )

        val selected = userDao.selectDeliveryAddress(
            userId = userId,
            addressId = addressId,
            address = address.trim(),
            latitude = latitude,
            longitude = longitude
        ) > 0

        if (!selected) {
            return@withContext null
        }

        DeliveryAddress(
            id = addressId,
            label = label.trim(),
            address = address.trim(),
            latitude = latitude,
            longitude = longitude
        )
    }

    suspend fun selectAddress(
        userId: Long,
        addressId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val address = addressDao.findById(userId, addressId)
            ?: return@withContext false

        userDao.selectDeliveryAddress(
            userId = userId,
            addressId = address.id,
            address = address.address,
            latitude = address.latitude,
            longitude = address.longitude
        ) > 0
    }

    suspend fun deleteAddress(
        userId: Long,
        addressId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val deleted = addressDao.delete(userId, addressId) > 0

        if (!deleted) {
            return@withContext false
        }

        val user = userDao.findById(userId)?.user
            ?: return@withContext true

        if (user.selectedAddressId != addressId) {
            return@withContext true
        }

        // Si se eliminó la dirección activa, se elige la más reciente
        val remaining = addressDao.findAll(userId)
        val next = remaining.firstOrNull()

        if (next != null) {
            userDao.selectDeliveryAddress(
                userId = userId,
                addressId = next.id,
                address = next.address,
                latitude = next.latitude,
                longitude = next.longitude
            )
        } else {
            userDao.selectDeliveryAddress(
                userId = userId,
                addressId = null,
                address = null,
                latitude = null,
                longitude = null
            )
        }

        true
    }

    /**
     * Convierte la ubicación capturada antes de existir la libreta en una
     * dirección guardada, para no perderla al actualizar la aplicación
     */
    private fun ensureInitialAddress(userId: Long) {
        val user = userDao.findById(userId)?.user ?: return
        val addresses = addressDao.findAll(userId)

        if (
            addresses.isEmpty() &&
            user.location.isNotBlank() &&
            user.latitude != null &&
            user.longitude != null
        ) {
            val addressId = addressDao.insert(
                userId = userId,
                label = INITIAL_ADDRESS_LABEL,
                address = user.location,
                latitude = user.latitude,
                longitude = user.longitude,
                createdAt = System.currentTimeMillis()
            )

            userDao.selectDeliveryAddress(
                userId = userId,
                addressId = addressId,
                address = user.location,
                latitude = user.latitude,
                longitude = user.longitude
            )
        } else if (
            addresses.isNotEmpty() &&
            user.selectedAddressId == null
        ) {
            val first = addresses.first()

            userDao.selectDeliveryAddress(
                userId = userId,
                addressId = first.id,
                address = first.address,
                latitude = first.latitude,
                longitude = first.longitude
            )
        }
    }

    private companion object {
        const val INITIAL_ADDRESS_LABEL = "Dirección actual"
    }
}
