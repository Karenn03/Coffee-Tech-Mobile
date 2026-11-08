package com.example.coffeetechmobile.ui.buyer.cart

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.navigation.BuyerTab
import com.example.coffeetechmobile.ui.buyer.navigation.setupBuyerBottomNavigation

class CartFragment : Fragment(R.layout.fragment_cart) {

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupBuyerBottomNavigation(
            root = view.findViewById(R.id.cart_bottom_navigation),
            navController = findNavController(),
            selectedTab = BuyerTab.CART
        )

        val productsCount = view.findViewById<TextView>(
            R.id.cart_products_count
        )

        cartViewModel.items.observe(viewLifecycleOwner) { items ->

            val totalQuantity = items.sumOf {
                it.quantity
            }

            productsCount.text = if (totalQuantity == 1) {
                "1 producto"
            } else {
                "$totalQuantity productos"
            }
        }
    }
}
