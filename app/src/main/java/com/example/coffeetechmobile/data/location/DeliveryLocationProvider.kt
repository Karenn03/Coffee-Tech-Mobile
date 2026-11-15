package com.example.coffeetechmobile.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

// Ubicación de entrega capturada con el GPS del dispositivo
data class DeliveryLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Obtiene la ubicación actual y la convierte en una dirección legible
 *
 * Usa LocationManager (GPS / red) y el Geocoder del sistema, de modo que no
 * requiere una clave de Google Maps para funcionar. Las coordenadas se
 * conservan junto con la dirección para asociarlas a la orden de entrega
 */
class DeliveryLocationProvider(private val context: Context) {

    suspend fun findCurrentLocation(): DeliveryLocation? =
        withContext(Dispatchers.IO) {
            if (!hasLocationPermission()) {
                return@withContext null
            }

            val manager = context.getSystemService(
                Context.LOCATION_SERVICE
            ) as? LocationManager ?: return@withContext null

            val lastKnown = lastKnownLocation(manager)

            // Se prefiere una posición reciente; si el GPS tarda, se usa la
            // última conocida como respaldo para no bloquear la entrega.
            val freshLocation = lastKnown?.takeIf { isFresh(it) }

            val location = freshLocation
                ?: currentLocation(manager)
                ?: lastKnown
                ?: return@withContext null

            DeliveryLocation(
                address = resolveAddress(
                    location.latitude,
                    location.longitude
                ),
                latitude = location.latitude,
                longitude = location.longitude
            )
        }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
    }

    // Solo se reutiliza una posición reciente; si es vieja se pide una nueva
    private fun isFresh(location: Location): Boolean {
        return System.currentTimeMillis() - location.time <=
            MAX_LAST_KNOWN_AGE_MS
    }

    // Revisa la última posición conocida por GPS, red o pasiva
    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(
        manager: LocationManager
    ): Location? {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        return providers
            .filter { manager.isProviderEnabled(it) }
            .mapNotNull { provider ->
                runCatching {
                    manager.getLastKnownLocation(provider)
                }.getOrNull()
            }
            .maxByOrNull { it.time }
    }

    // Pide una ubicación puntual con un tiempo máximo de espera
    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(
        manager: LocationManager
    ): Location? {
        val provider = when {
            manager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            ) -> LocationManager.GPS_PROVIDER

            manager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            ) -> LocationManager.NETWORK_PROVIDER

            else -> return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                suspendCancellableCoroutine { continuation ->
                    val cancellation = CancellationSignal()

                    continuation.invokeOnCancellation {
                        cancellation.cancel()
                    }

                    manager.getCurrentLocation(
                        provider,
                        cancellation,
                        ContextCompat.getMainExecutor(context)
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }
            } else {
                requestSingleUpdate(manager, provider)
            }
        }
    }

    // Compatibilidad con Android 8 a 10
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        val listener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                manager.removeUpdates(this)

                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }

            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?
            ) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }

        manager.requestSingleUpdate(
            provider,
            listener,
            Looper.getMainLooper()
        )

        continuation.invokeOnCancellation {
            manager.removeUpdates(listener)
        }
    }

    /**
     * Convierte las coordenadas en una dirección
     *
     * Si el geocodificador no está disponible o no hay red, se muestran
     * las coordenadas para no perder la ubicación capturada
     */
    suspend fun resolveAddress(
        latitude: Double,
        longitude: Double
    ): String = withContext(Dispatchers.IO) {
        reverseGeocode(latitude, longitude)
    }

    // Busca coordenadas a partir de un texto escrito por el comprador
    suspend fun findByAddress(query: String): DeliveryLocation? =
        withContext(Dispatchers.IO) {
            val text = query.trim()

            if (text.isBlank() || !Geocoder.isPresent()) {
                return@withContext null
            }

            try {
                val address = Geocoder(
                    context,
                    Locale("es", "CO")
                ).getFromLocationName(text, 1)
                    .orEmpty()
                    .firstOrNull()
                    ?: return@withContext null

                DeliveryLocation(
                    address = address
                        .getAddressLine(0)
                        ?.takeIf { it.isNotBlank() }
                        ?: text,
                    latitude = address.latitude,
                    longitude = address.longitude
                )
            } catch (exception: Exception) {
                null
            }
        }

    private fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): String {
        val fallback = String.format(
            Locale.US,
            "Lat %.5f, Lng %.5f",
            latitude,
            longitude
        )

        if (!Geocoder.isPresent()) {
            return fallback
        }

        return try {
            val geocoder = Geocoder(
                context,
                Locale("es", "CO")
            )

            val address = geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ).orEmpty().firstOrNull() ?: return fallback

            val fullAddress = address
                .getAddressLine(0)
                ?.trim()
                .orEmpty()

            if (fullAddress.isNotBlank()) {
                fullAddress
            } else {
                buildAddress(address)
            }
        } catch (exception: Exception) {
            fallback
        }
    }

    private fun buildAddress(address: Address): String {
        val street = listOfNotNull(
            address.thoroughfare,
            address.subThoroughfare?.let { "#$it" }
        ).joinToString(" ").trim()

        val city = address.locality ?: address.subAdminArea

        val parts = listOfNotNull(
            street.takeIf { it.isNotBlank() },
            city?.takeIf { it.isNotBlank() }
        )

        return if (parts.isEmpty()) {
            "Ubicación capturada"
        } else {
            parts.joinToString(", ")
        }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MS = 15_000L
        const val MAX_LAST_KNOWN_AGE_MS = 5L * 60L * 1000L
    }
}
