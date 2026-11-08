package com.example.coffeetechmobile.ui.buyer.cart

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
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onIncrease: (Int) -> Unit,
    private val onDecrease: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    private val priceFormatter =
        NumberFormat.getIntegerInstance(Locale("es", "CO"))

    inner class CartViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView =
            itemView.findViewById(R.id.cart_item_image)

        private val name: TextView =
            itemView.findViewById(R.id.cart_item_name)

        private val description: TextView =
            itemView.findViewById(R.id.cart_item_description)

        private val unitPrice: TextView =
            itemView.findViewById(R.id.cart_item_unit_price)

        private val quantity: TextView =
            itemView.findViewById(R.id.cart_item_quantity)

        private val totalPrice: TextView =
            itemView.findViewById(R.id.cart_item_total_price)

        private val increaseButton: TextView =
            itemView.findViewById(R.id.cart_item_increase_button)

        private val decreaseButton: TextView =
            itemView.findViewById(R.id.cart_item_decrease_button)

        private val deleteButton: ImageButton =
            itemView.findViewById(R.id.cart_item_delete_button)

        fun bind(item: CartItem) {

            image.setImageResource(item.imageRes)
            image.contentDescription = item.name

            name.text = item.name
            description.text = item.description

            unitPrice.text =
                "$${priceFormatter.format(item.price)} COP / unidad"

            quantity.text = item.quantity.toString()

            val total = item.price * item.quantity

            totalPrice.text =
                "$${priceFormatter.format(total)} COP"

            increaseButton.setOnClickListener {
                onIncrease(item.id)
            }

            decreaseButton.setOnClickListener {
                onDecrease(item.id)
            }

            deleteButton.setOnClickListener {
                onDelete(item.id)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)

        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {

        override fun areItemsTheSame(
            oldItem: CartItem,
            newItem: CartItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CartItem,
            newItem: CartItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
