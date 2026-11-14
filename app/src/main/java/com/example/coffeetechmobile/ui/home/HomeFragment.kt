package com.example.coffeetechmobile.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.cart.CartItem
import com.example.coffeetechmobile.ui.buyer.cart.CartViewModel
import com.example.coffeetechmobile.ui.buyer.catalog.MenuCatalog
import com.example.coffeetechmobile.ui.buyer.catalog.MenuProduct
import com.example.coffeetechmobile.ui.buyer.navigation.BuyerTab
import com.example.coffeetechmobile.ui.buyer.navigation.setupBuyerBottomNavigation
import com.example.coffeetechmobile.ui.profile.ProfileFragment
import com.example.coffeetechmobile.data.model.UserRole
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupBuyerBottomNavigation(
            root = view.findViewById(
                R.id.bottom_navigation_container
            ),
            navController = findNavController(),
            selectedTab = BuyerTab.HOME
        )

        view.findViewById<View>(
            R.id.profile_button
        ).setOnClickListener {
            findNavController().navigate(
                R.id.profileFragment,
                ProfileFragment.createArguments(UserRole.BUYER)
            )
        }

        MenuCatalog.products.forEach { product ->
            bindProduct(
                root = view,
                product = product
            )
        }
    }

    private fun bindProduct(
        root: View,
        product: MenuProduct
    ) {
        val productCard = root.findViewById<View>(
            product.id
        )

        productCard.findViewById<ImageView>(
            R.id.product_image
        ).apply {
            setImageResource(product.imageRes)
            contentDescription = product.name
        }

        productCard.findViewById<TextView>(
            R.id.product_name
        ).text = product.name

        productCard.findViewById<TextView>(
            R.id.product_description
        ).text = product.description

        val formattedPrice = NumberFormat
            .getIntegerInstance(Locale("es", "CO"))
            .format(product.price)

        productCard.findViewById<TextView>(
            R.id.product_price
        ).text = "\$$formattedPrice"

        // Abrir detalle al tocar la tarjeta
        productCard.setOnClickListener {

            val args = bundleOf(
                "product_id" to product.id
            )

            findNavController().navigate(
                R.id.action_homeFragment_to_productDetailFragment,
                args
            )
        }

        // Agregar directamente al carrito
        productCard.findViewById<ImageButton>(
            R.id.add_product_button
        ).setOnClickListener {

            cartViewModel.addProduct(
                CartItem(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    imageRes = product.imageRes
                )
            )

            Toast.makeText(
                requireContext(),
                "${product.name} agregado al carrito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
