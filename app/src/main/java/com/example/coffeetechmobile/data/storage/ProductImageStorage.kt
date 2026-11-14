package com.example.coffeetechmobile.data.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Guarda las imágenes de productos en el almacenamiento interno de la app
 *
 * No se usan rutas de galería de forma permanente porque el permiso sobre el
 * URI original puede perderse. Al elegir una imagen se copia al directorio
 * `files/product_images` y en SQLite solo se guarda la ruta interna
 */
class ProductImageStorage(context: Context) {

    private val appContext = context.applicationContext

    // Crea el archivo vacío que recibirá la foto de la cámara
    fun createCameraFile(): File {
        val directory = imageDirectory()
        return File(
            directory,
            "product_${System.currentTimeMillis()}.jpg"
        )
    }

    // Genera el URI seguro que se entrega a la aplicación de cámara
    fun createCameraUri(file: File): Uri {
        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
    }

    /**
     * Copia una imagen elegida desde la galería al almacenamiento interno.
     * Devuelve la ruta absoluta o null si no se pudo copiar.
     */
    fun copyFromUri(uri: Uri): String? {
        return try {
            val directory = imageDirectory()
            val destination = File(
                directory,
                "product_${System.currentTimeMillis()}.jpg"
            )

            appContext.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            destination.absolutePath
        } catch (exception: Exception) {
            null
        }
    }

    // Elimina un archivo interno cuando el producto se borra o cambia de foto
    fun delete(path: String?) {
        if (path.isNullOrBlank()) return

        val file = File(path)

        if (file.exists()) {
            file.delete()
        }
    }

    private fun imageDirectory(): File {
        val directory = File(appContext.filesDir, "product_images")
        directory.mkdirs()
        return directory
    }
}
