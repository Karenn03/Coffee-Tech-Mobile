package com.example.coffeetechmobile.ui.seller.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.catalog.MenuCatalog
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class SellerOrdersAdapter :
    ListAdapter<SellerOrder, SellerOrdersAdapter.OrderViewHolder>(
        DIFF_CALLBACK
    ) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<SellerOrder>() {

                override fun areItemsTheSame(
                    oldItem: SellerOrder,
                    newItem: SellerOrder
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: SellerOrder,
                    newItem: SellerOrder
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    inner class OrderViewHolder(
        private val card: MaterialCardView
    ) : RecyclerView.ViewHolder(card) {

        fun bind(order: SellerOrder) {
            val context = card.context

            card.findViewById<TextView>(
                R.id.seller_order_id
            ).text = "Pedido #${order.id}"

            card.findViewById<TextView>(
                R.id.seller_order_date
            ).text = order.date

            card.findViewById<TextView>(
                R.id.seller_order_customer
            ).text = order.customer

            card.findViewById<TextView>(
                R.id.seller_order_delivery
            ).text = order.deliveryType

            card.findViewById<TextView>(
                R.id.seller_order_payment
            ).text = order.paymentMethod

            val formattedAmount = NumberFormat
                .getIntegerInstance(Locale("es", "CO"))
                .format(order.total)

            card.findViewById<TextView>(
                R.id.seller_order_total
            ).text = context.getString(
                R.string.seller_orders_amount_format,
                formattedAmount
            )

            bindProducts(order)
            bindCustomerNote(order)

            configureStatus(
                card.findViewById(R.id.seller_order_status),
                order.status
            )
        }

        private fun bindProducts(order: SellerOrder) {
            val container = card.findViewById<LinearLayout>(
                R.id.seller_order_products_container
            )

            container.removeAllViews()

            val inflater = LayoutInflater.from(card.context)

            order.products.forEach { orderProduct ->
                val product = MenuCatalog.findById(
                    orderProduct.productId
                )

                val itemView = inflater.inflate(
                    R.layout.item_seller_order_product,
                    container,
                    false
                )

                val image = itemView.findViewById<ImageView>(
                    R.id.order_product_image
                )

                val name = itemView.findViewById<TextView>(
                    R.id.order_product_name
                )

                val quantity = itemView.findViewById<TextView>(
                    R.id.order_product_quantity
                )

                if (product != null) {
                    image.setImageResource(product.imageRes)
                    image.contentDescription = product.name
                    image.visibility = View.VISIBLE
                    name.text = product.name
                } else {
                    image.visibility = View.GONE
                    name.setText(
                        R.string.seller_order_missing_product
                    )
                }

                quantity.text = if (orderProduct.quantity == 1) {
                    card.context.getString(
                        R.string.seller_order_single_unit
                    )
                } else {
                    card.context.getString(
                        R.string.seller_order_product_quantity,
                        orderProduct.quantity
                    )
                }

                container.addView(itemView)
            }
        }

        private fun bindCustomerNote(order: SellerOrder) {
            val noteContainer = card.findViewById<View>(
                R.id.seller_order_note_container
            )

            val noteText = card.findViewById<TextView>(
                R.id.seller_order_note
            )

            val note = order.customerNote
                ?.trim()
                .orEmpty()

            if (note.isNotEmpty()) {
                noteText.text = note
                noteContainer.visibility = View.VISIBLE
            } else {
                noteText.text = ""
                noteContainer.visibility = View.GONE
            }
        }

        private fun configureStatus(
            label: TextView,
            status: SellerOrderStatus
        ) {
            val (textRes, backgroundRes, colorRes) =
                when (status) {

                    SellerOrderStatus.RECEIVED ->
                        Triple(
                            R.string.seller_orders_status_received,
                            R.drawable.bg_user_count_badge,
                            R.color.coffee_primary
                        )

                    SellerOrderStatus.PREPARING ->
                        Triple(
                            R.string.seller_orders_status_preparing,
                            R.drawable.bg_sale_pending,
                            R.color.sale_pending_text
                        )

                    SellerOrderStatus.READY ->
                        Triple(
                            R.string.seller_orders_status_ready,
                            R.drawable.bg_sale_completed,
                            R.color.sale_completed_text
                        )

                    SellerOrderStatus.DELIVERED ->
                        Triple(
                            R.string.seller_orders_status_delivered,
                            R.drawable.bg_sale_completed,
                            R.color.sale_completed_text
                        )
                }

            label.setText(textRes)
            label.setBackgroundResource(backgroundRes)

            label.setTextColor(
                ContextCompat.getColor(
                    label.context,
                    colorRes
                )
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_seller_order,
                parent,
                false
            ) as MaterialCardView

        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OrderViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}
