package com.example.coffeetechmobile.data.model

/**
 * Producto del menú almacenado en SQLite.
 *
 * [imageResName] guarda el nombre del drawable para los productos semilla;
 * [imagePath] guarda la ruta interna cuando el administrador sube una foto
 * desde galería o cámara.
 */
data class Product(
    val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val salePrice: Int,
    val preparationCost: Int,
    val taxRate: Int,
    val stock: Int,
    val minimumStock: Int,
    val imagePath: String = "",
    val imageResName: String = "",
    val active: Boolean = true,
    val createdAt: Long = 0L
)
