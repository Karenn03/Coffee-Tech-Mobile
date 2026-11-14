package com.example.coffeetechmobile.data.repository

import android.content.Context
import com.example.coffeetechmobile.data.local.CoffeeTechDatabase
import com.example.coffeetechmobile.data.local.ProductDao
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.data.storage.ProductImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Datos que llegan desde el formulario de producto.
 *
 * [imagePath] corresponde a una foto interna elegida desde galería o cámara.
 * [imageResName] conserva el drawable cuando el producto semilla no cambia su
 * imagen.
 */
data class ProductInput(
    val name: String,
    val category: String,
    val description: String,
    val salePrice: Int,
    val preparationCost: Int,
    val taxRate: Int,
    val stock: Int,
    val minimumStock: Int,
    val imagePath: String? = null,
    val imageResName: String? = null
)

// Resultado de las operaciones del CRUD de productos
sealed interface ProductResult {

    data class Success(val product: Product) : ProductResult

    data class Error(val message: String) : ProductResult
}

/**
 * CRUD de productos del administrador sobre SQLite.
 *
 * También administra el ciclo de vida de las imágenes internas: al reemplazar
 * una foto o eliminar un producto se borra el archivo anterior.
 */
class ProductRepository(context: Context) {

    private val productDao = ProductDao(
        CoffeeTechDatabase.getInstance(context)
    )
    private val imageStorage = ProductImageStorage(context)

    // Lista los productos para el panel del administrador
    suspend fun getAllProducts(): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.findAll()
        }

    // Crea un producto nuevo
    suspend fun createProduct(input: ProductInput): ProductResult =
        withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()

                val productId = productDao.insert(
                    name = input.name.trim(),
                    category = input.category,
                    description = input.description.trim(),
                    salePrice = input.salePrice,
                    preparationCost = input.preparationCost,
                    taxRate = input.taxRate,
                    stock = input.stock,
                    minimumStock = input.minimumStock,
                    imagePath = input.imagePath.orEmpty(),
                    imageResName = input.imageResName.orEmpty(),
                    createdAt = now
                )

                ProductResult.Success(
                    Product(
                        id = productId,
                        name = input.name.trim(),
                        category = input.category,
                        description = input.description.trim(),
                        salePrice = input.salePrice,
                        preparationCost = input.preparationCost,
                        taxRate = input.taxRate,
                        stock = input.stock,
                        minimumStock = input.minimumStock,
                        imagePath = input.imagePath.orEmpty(),
                        imageResName = input.imageResName.orEmpty(),
                        active = true,
                        createdAt = now
                    )
                )
            } catch (exception: Exception) {
                ProductResult.Error(
                    "No se pudo guardar el producto. Intenta de nuevo."
                )
            }
        }

    // Actualiza un producto existente
    suspend fun updateProduct(
        productId: Long,
        input: ProductInput
    ): ProductResult = withContext(Dispatchers.IO) {
        try {
            val current = productDao.findById(productId)
                ?: return@withContext ProductResult.Error(
                    "El producto ya no existe."
                )

            val newImagePath = input.imagePath.orEmpty()

            val updatedRows = productDao.update(
                productId = productId,
                name = input.name.trim(),
                category = input.category,
                description = input.description.trim(),
                salePrice = input.salePrice,
                preparationCost = input.preparationCost,
                taxRate = input.taxRate,
                stock = input.stock,
                minimumStock = input.minimumStock,
                imagePath = newImagePath,
                imageResName = input.imageResName.orEmpty()
            )

            if (updatedRows <= 0) {
                return@withContext ProductResult.Error(
                    "No se pudo actualizar el producto."
                )
            }

            // Si se reemplazó una foto interna, se elimina la anterior.
            if (
                current.imagePath.isNotBlank() &&
                current.imagePath != newImagePath
            ) {
                imageStorage.delete(current.imagePath)
            }

            ProductResult.Success(
                current.copy(
                    name = input.name.trim(),
                    category = input.category,
                    description = input.description.trim(),
                    salePrice = input.salePrice,
                    preparationCost = input.preparationCost,
                    taxRate = input.taxRate,
                    stock = input.stock,
                    minimumStock = input.minimumStock,
                    imagePath = newImagePath,
                    imageResName = input.imageResName.orEmpty()
                )
            )
        } catch (exception: Exception) {
            ProductResult.Error(
                "No se pudo actualizar el producto. Intenta de nuevo."
            )
        }
    }

    // Elimina el producto y su archivo de imagen si existe
    suspend fun deleteProduct(productId: Long): Boolean =
        withContext(Dispatchers.IO) {
            val current = productDao.findById(productId) ?: return@withContext false

            val deleted = productDao.deleteById(productId) > 0

            if (deleted) {
                imageStorage.delete(current.imagePath)
            }

            deleted
        }
}
