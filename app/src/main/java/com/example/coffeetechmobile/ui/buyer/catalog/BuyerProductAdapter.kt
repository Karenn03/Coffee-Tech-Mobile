package com.example.coffeetechmobile.ui.buyer.catalog

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter del menú del comprador
 *
 * Reutiliza la tarjeta `item_product.xml`, abre el detalle al tocar la
 * tarjeta y agrega el producto al carrito desde el botón de suma
 */
class BuyerProductAdapter(
    private val onProductClick: (MenuProduct) -> Unit,
    private val onAddToCart: (MenuProduct) -> Unit
) : ListAdapter<MenuProduct, BuyerProductAdapter.ProductViewHolder>(
    MenuProductDiffCallback()
) {

    private val priceFormatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    inner class ProductViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView =
            itemView.findViewById(R.id.product_image)

        private val name: TextView =
            itemView.findViewById(R.id.product_name)

        private val description: TextView =
            itemView.findViewById(R.id.product_description)

        private val price: TextView =
            itemView.findViewById(R.id.product_price)

        private val addButton: ImageButton =
            itemView.findViewById(R.id.add_product_button)

        fun bind(product: MenuProduct) {
            if (!product.imagePath.isNullOrBlank()) {
                image.setImageURI(
                    Uri.fromFile(File(product.imagePath))
                )
            } else {
                image.setImageResource(product.imageRes)
            }

            image.contentDescription = product.name
            name.text = product.name
            description.text = product.description
            price.text = "\$${priceFormatter.format(product.price)}"

            addButton.setOnClickListener {
                onAddToCart(product)
            }

            itemView.setOnClickListener {
                onProductClick(product)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    private class MenuProductDiffCallback :
        DiffUtil.ItemCallback<MenuProduct>() {

        override fun areItemsTheSame(
            oldItem: MenuProduct,
            newItem: MenuProduct
        ): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(
            oldItem: MenuProduct,
            newItem: MenuProduct
        ): Boolean {
            return oldItem == newItem
        }
    }
}
