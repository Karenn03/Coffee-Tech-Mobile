package com.example.coffeetechmobile.ui.seller.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.session.SessionManager
import com.example.coffeetechmobile.ui.manager.products.ProductManagementViewModel
import com.example.coffeetechmobile.ui.profile.ProfileFragment
import com.example.coffeetechmobile.ui.seller.navigation.SellerTab
import com.example.coffeetechmobile.ui.seller.navigation.setupSellerBottomNavigation
import com.example.coffeetechmobile.ui.seller.orders.SellerOrdersViewModel

/**
 * Inicio del vendedor
 *
 * Muestra un resumen de pedidos y productos, además de accesos rápidos
 * al CRUD de productos y al historial de pedidos
 */
class SellerHomeFragment : Fragment(R.layout.fragment_seller_home) {

    private val ordersViewModel: SellerOrdersViewModel by viewModels()
    private val productsViewModel: ProductManagementViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupSellerBottomNavigation(
            root = view.findViewById(R.id.seller_home_bottom_navigation),
            navController = findNavController(),
            selectedTab = SellerTab.HOME
        )

        val session = SessionManager(requireContext()).current()

        view.findViewById<TextView>(R.id.seller_home_header_name).text =
            session?.fullName ?: "Coffee Tech"

        view.findViewById<TextView>(R.id.seller_home_header_role).text =
            "Vendedor"

        ordersViewModel.counts.observe(viewLifecycleOwner) { counts ->
            view.findViewById<TextView>(
                R.id.seller_home_active_count
            ).text = counts.active.toString()

            view.findViewById<TextView>(
                R.id.seller_home_delivered_count
            ).text = counts.delivered.toString()
        }

        view.findViewById<TextView>(R.id.seller_home_product_count).text =
            "0"

        productsViewModel.products.observe(viewLifecycleOwner) { products ->
            view.findViewById<TextView>(R.id.seller_home_product_count)
                .text = products.size.toString()
        }

        view.findViewById<View>(R.id.seller_home_products_action)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.productManagementFragment
                )
            }

        view.findViewById<View>(R.id.seller_home_orders_action)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.sellerOrdersFragment
                )
            }

        view.findViewById<View>(R.id.seller_home_profile_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.profileFragment,
                    ProfileFragment.createArguments(UserRole.SELLER)
                )
            }
    }

    override fun onResume() {
        super.onResume()

        productsViewModel.loadProducts()
        ordersViewModel.refresh()
    }
}
