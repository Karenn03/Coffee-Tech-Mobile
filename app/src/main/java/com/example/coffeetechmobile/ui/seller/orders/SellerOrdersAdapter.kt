package com.example.coffeetechmobile.ui.seller.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
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
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SellerOrder,
                    newItem: SellerOrder
                ) = oldItem == newItem
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

            val productsContainer =
                card.findViewById<LinearLayout>(
                    R.id.seller_order_products_container
                )

            productsContainer.removeAllViews()

            order.products.forEach { productName ->
                val productText = TextView(context).apply {
                    text = productName
                    textSize = 13f

                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.coffee_secondary
                        )
                    )
                }

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                params.bottomMargin = (
                        5 * context.resources.displayMetrics.density
                        ).toInt()

                productsContainer.addView(
                    productText,
                    params
                )
            }

            configureStatus(
                card.findViewById(R.id.seller_order_status),
                order.status
            )
        }

        private fun configureStatus(
            label: TextView,
            status: SellerOrderStatus
        ) {
            val context = label.context

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
                ContextCompat.getColor(context, colorRes)
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
