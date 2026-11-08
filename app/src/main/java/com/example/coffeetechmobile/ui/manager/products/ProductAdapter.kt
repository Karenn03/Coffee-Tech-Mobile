package com.example.coffeetechmobile.ui.manager.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import java.io.Serializable
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val description: String,
    val salePrice: Int,
    val preparationCost: Int,
    val taxRate: Int,
    val initialStock: Int,
    val minimumStock: Int,
    val imageRes: Int? = null,
    val imageUri: String? = null
) : Serializable

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

    class ProductViewHolder(
        itemView: View,
        private val onEdit: (ProductItem) -> Unit,
        private val onDelete: (ProductItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(product: ProductItem) {
            val image = itemView.findViewById<ImageView>(R.id.admin_product_image)
            if (product.imageUri != null) {
                image.setImageURI(android.net.Uri.parse(product.imageUri))
            } else if (product.imageRes != null) {
                image.setImageResource(product.imageRes)
            } else {
                image.setImageResource(R.drawable.ic_coffee)
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
