package com.example.coffeetechmobile.ui.buyer.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utilidades de permiso de ubicación compartidas por las
 * pantallas de direcciones y el selector del mapa
 */
fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val coarse = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return fine == PackageManager.PERMISSION_GRANTED ||
        coarse == PackageManager.PERMISSION_GRANTED
}

fun locationPermissions(): Array<String> {
    return arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}
