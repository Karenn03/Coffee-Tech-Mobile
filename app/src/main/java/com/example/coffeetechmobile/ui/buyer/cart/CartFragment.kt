package com.example.coffeetechmobile.ui.buyer.cart

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.navigation.BuyerTab
import com.example.coffeetechmobile.ui.buyer.navigation.setupBuyerBottomNavigation
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment(R.layout.fragment_cart) {

    private val cartViewModel: CartViewModel by activityViewModels()

    private lateinit var cartAdapter: CartAdapter

    private val priceFormatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Navegación del comprador
        setupBuyerBottomNavigation(
            root = view.findViewById(R.id.cart_bottom_navigation),
            navController = findNavController(),
            selectedTab = BuyerTab.CART
        )

        // Adaptador
        cartAdapter = CartAdapter(
            onIncrease = { productId ->
                cartViewModel.increaseQuantity(productId)
            },
            onDecrease = { productId ->
                cartViewModel.decreaseQuantity(productId)
            },
            onDelete = { productId ->
                cartViewModel.removeProduct(productId)
            }
        )

        // Listado
        val recyclerView = view.findViewById<RecyclerView>(
            R.id.cart_products_recycler_view
        )

        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )
        recyclerView.adapter = cartAdapter
        recyclerView.isNestedScrollingEnabled = false

        // Contador
        val productsCount = view.findViewById<TextView>(
            R.id.cart_products_count
        )

        // Resumen
        val summary = view.findViewById<View>(
            R.id.cart_order_summary
        )

        val subtotalText = view.findViewById<TextView>(
            R.id.cart_summary_subtotal
        )

        val discountText = view.findViewById<TextView>(
            R.id.cart_summary_discount
        )

        val shippingText = view.findViewById<TextView>(
            R.id.cart_summary_shipping
        )

        val totalText = view.findViewById<TextView>(
            R.id.cart_summary_total
        )

        val checkoutButton = view.findViewById<View>(
            R.id.cart_checkout_button
        )

        // Datos reactivos
        cartViewModel.items.observe(viewLifecycleOwner) { items ->

            cartAdapter.submitList(items)

            val totalQuantity = items.sumOf { it.quantity }

            productsCount.text = if (totalQuantity == 1) {
                "1 producto"
            } else {
                "$totalQuantity productos"
            }

            val hasProducts = items.isNotEmpty()

            summary.visibility = if (hasProducts) {
                View.VISIBLE
            } else {
                View.GONE
            }

            checkoutButton.visibility = if (hasProducts) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (hasProducts) {
                subtotalText.text = formatPrice(
                    cartViewModel.getSubtotal()
                )

                discountText.text = formatPrice(
                    CartViewModel.DISCOUNT
                )

                shippingText.text = formatPrice(
                    CartViewModel.SHIPPING_COST
                )

                totalText.text = formatPrice(
                    cartViewModel.getTotal()
                )
            }
        }

        checkoutButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                R.string.cart_payment_pending,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatPrice(value: Int): String {
        return getString(
            R.string.cart_price_format,
            "\$${priceFormatter.format(value)}"
        )
    }
}
