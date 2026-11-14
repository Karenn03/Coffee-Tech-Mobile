package com.example.coffeetechmobile.ui.manager.products

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.Product
import java.io.File
import java.io.Serializable
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val productId: Long = 0L,
    val name: String,
    val category: String,
    val description: String,
    val salePrice: Int,
    val preparationCost: Int,
    val taxRate: Int,
    val initialStock: Int,
    val minimumStock: Int,
    val imageRes: Int? = null,
    val imageResName: String? = null,
    val imagePath: String? = null
) : Serializable {

    companion object {

        /**
         * Convierte un producto de SQLite al modelo de la tarjeta del panel
         *
         * Los productos semilla usan un drawable; los creados desde
         * el formulario usan una ruta interna de imagen
         */
        fun fromProduct(context: Context, product: Product): ProductItem {
            val imageRes = if (product.imageResName.isNotBlank()) {
                context.resources.getIdentifier(
                    product.imageResName,
                    "drawable",
                    context.packageName
                ).takeIf { it != 0 }
            } else {
                null
            }

            return ProductItem(
                id = product.id.toString(),
                productId = product.id,
                name = product.name,
                category = product.category,
                description = product.description,
                salePrice = product.salePrice,
                preparationCost = product.preparationCost,
                taxRate = product.taxRate,
                initialStock = product.stock,
                minimumStock = product.minimumStock,
                imageRes = imageRes,
                imageResName = product.imageResName.ifBlank { null },
                imagePath = product.imagePath.ifBlank { null }
            )
        }
    }
}

class ProductAdapter(
    private val products: MutableList<ProductItem>,
    private val onEdit: (ProductItem) -> Unit,
    private val onDelete: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val allProducts = products.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_admin, parent, false)
        return ProductViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun addProduct(product: ProductItem) {
        allProducts.add(0, product)
        products.add(0, product)
        notifyItemInserted(0)
    }

    fun updateProduct(product: ProductItem) {
        val sourcePosition = allProducts.indexOfFirst { it.id == product.id }
        if (sourcePosition == -1) return
        allProducts[sourcePosition] = product
        val visiblePosition = products.indexOfFirst { it.id == product.id }
        if (visiblePosition != -1) {
            products[visiblePosition] = product
            notifyItemChanged(visiblePosition)
        }
    }

    fun removeProduct(product: ProductItem) {
        allProducts.removeAll { it.id == product.id }
        val position = products.indexOfFirst { it.id == product.id }
        if (position == -1) return
        products.removeAt(position)
        notifyItemRemoved(position)
    }

    fun filter(query: String, category: String?) {
        val filtered = allProducts.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category == category
            matchesQuery && matchesCategory
        }
        products.clear()
        products.addAll(filtered)
        notifyDataSetChanged()
    }

    // Reemplaza la lista visible con el resultado de SQLite o de un filtro
    fun submitList(newProducts: List<ProductItem>) {
        allProducts.clear()
        allProducts.addAll(newProducts)
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    class ProductViewHolder(
        itemView: View,
        private val onEdit: (ProductItem) -> Unit,
        private val onDelete: (ProductItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(product: ProductItem) {
            val image = itemView.findViewById<ImageView>(R.id.admin_product_image)
            when {
                !product.imagePath.isNullOrBlank() -> {
                    image.setImageURI(
                        Uri.fromFile(File(product.imagePath))
                    )
                }

                product.imageRes != null -> {
                    image.setImageResource(product.imageRes)
                }

                else -> {
                    image.setImageResource(R.drawable.ic_coffee)
                }
            }
            image.contentDescription = product.name

            itemView.findViewById<TextView>(R.id.admin_product_name).text = product.name
            itemView.findViewById<TextView>(R.id.admin_product_description).text = product.description
            itemView.findViewById<TextView>(R.id.admin_product_price).text = formatPrice(product.salePrice)
            itemView.findViewById<TextView>(R.id.admin_product_category).text = product.category

            itemView.findViewById<ImageButton>(R.id.admin_product_options)
                .setOnClickListener { anchor ->
                    PopupMenu(itemView.context, anchor).apply {
                        menu.add(0, 1, 0, R.string.edit_product)
                        menu.add(0, 2, 1, R.string.delete_product)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                1 -> onEdit(product)
                                2 -> onDelete(product)
                                else -> return@setOnMenuItemClickListener false
                            }
                            true
                        }
                        show()
                    }
                }
        }

        private fun formatPrice(price: Int): String {
            val formattedPrice =
                NumberFormat.getIntegerInstance(
                    Locale.forLanguageTag("es-CO")
                ).format(price)

            return "$$formattedPrice COP"
        }
    }
}
