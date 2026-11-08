package com.example.coffeetechmobile.ui.buyer.catalog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.cart.CartItem
import com.example.coffeetechmobile.ui.buyer.cart.CartViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment :
    Fragment(R.layout.fragment_product_detail) {

    private val cartViewModel: CartViewModel by activityViewModels()

    private val formatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    private var quantity = 1
    private var size = "Mediano"
    private var milk = "Tradicional"
    private var sweetness = "Normal"
    private var ice = "Con hielo"
    private var extraShot = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val productId = requireArguments().getInt("product_id")
        val product = MenuCatalog.findById(productId)

        if (product == null) {
            Toast.makeText(
                requireContext(),
                R.string.product_detail_not_found,
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
            return
        }

        view.findViewById<View>(
            R.id.detail_back_button
        ).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(
            R.id.detail_cart_button
        ).setOnClickListener {
            findNavController().navigate(R.id.cartFragment)
        }

        bindProduct(view, product)
        setupOptions(view, product)
        setupQuantity(view, product)

        view.findViewById<View>(
            R.id.detail_add_button
        ).setOnClickListener {
            addToCart(product)

            Toast.makeText(
                requireContext(),
                R.string.product_detail_added,
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }

        updatePrice(view, product)
    }

    private fun bindProduct(
        root: View,
        product: MenuProduct
    ) {
        root.findViewById<ImageView>(
            R.id.detail_product_image
        ).apply {
            setImageResource(product.imageRes)
            contentDescription = product.name
        }

        root.findViewById<TextView>(
            R.id.detail_category
        ).text = product.category

        root.findViewById<TextView>(
            R.id.detail_product_name
        ).text = product.name

        root.findViewById<TextView>(
            R.id.detail_product_description
        ).text = product.description

        root.findViewById<View>(
            R.id.detail_drink_options
        ).visibility = if (product.isDrink) {
            View.VISIBLE
        } else {
            View.GONE
        }

        root.findViewById<View>(
            R.id.detail_ice_section
        ).visibility = if (product.isIced) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setupOptions(
        root: View,
        product: MenuProduct
    ) {
        if (!product.isDrink) return

        setupChoiceGroup(
            root = root,
            ids = listOf(
                R.id.detail_size_small,
                R.id.detail_size_medium,
                R.id.detail_size_large
            ),
            selectedIndex = 1
        ) { index ->
            size = when (index) {
                0 -> "Pequeño"
                2 -> "Grande"
                else -> "Mediano"
            }

            updatePrice(root, product)
        }

        setupChoiceGroup(
            root = root,
            ids = listOf(
                R.id.detail_milk_regular,
                R.id.detail_milk_almond
            ),
            selectedIndex = 0
        ) { index ->
            milk = if (index == 0) {
                "Tradicional"
            } else {
                "Almendras"
            }

            updatePrice(root, product)
        }

        setupChoiceGroup(
            root = root,
            ids = listOf(
                R.id.detail_sweet_regular,
                R.id.detail_sweet_half,
                R.id.detail_sweet_none
            ),
            selectedIndex = 0
        ) { index ->
            sweetness = when (index) {
                1 -> "Medio"
                2 -> "Sin azúcar"
                else -> "Normal"
            }
        }

        if (product.isIced) {
            setupChoiceGroup(
                root = root,
                ids = listOf(
                    R.id.detail_ice_regular,
                    R.id.detail_ice_none
                ),
                selectedIndex = 0
            ) { index ->
                ice = if (index == 0) {
                    "Con hielo"
                } else {
                    "Sin hielo"
                }
            }
        }

        root.findViewById<MaterialCheckBox>(
            R.id.detail_extra_shot
        ).setOnCheckedChangeListener { _, checked ->
            extraShot = checked
            updatePrice(root, product)
        }
    }

    private fun setupChoiceGroup(
        root: View,
        ids: List<Int>,
        selectedIndex: Int,
        onSelected: (Int) -> Unit
    ) {
        val buttons = ids.map { id ->
            root.findViewById<TextView>(id)
        }

        val selectedColor = ContextCompat.getColor(
            requireContext(),
            R.color.background_app
        )

        val normalColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        fun select(index: Int) {
            buttons.forEachIndexed { position, button ->
                val isSelected = position == index

                button.setBackgroundResource(
                    if (isSelected) {
                        R.drawable.bg_detail_option_selected
                    } else {
                        R.drawable.bg_detail_option_unselected
                    }
                )

                button.setTextColor(
                    if (isSelected) selectedColor else normalColor
                )

                button.isSelected = isSelected
            }

            onSelected(index)
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                select(index)
            }
        }

        select(selectedIndex)
    }

    private fun setupQuantity(
        root: View,
        product: MenuProduct
    ) {
        val quantityText = root.findViewById<TextView>(
            R.id.detail_quantity
        )

        fun refresh() {
            quantityText.text = quantity.toString()
            updatePrice(root, product)
        }

        root.findViewById<View>(
            R.id.detail_decrease_button
        ).setOnClickListener {
            if (quantity > 1) {
                quantity--
                refresh()
            }
        }

        root.findViewById<View>(
            R.id.detail_increase_button
        ).setOnClickListener {
            if (quantity < 20) {
                quantity++
                refresh()
            }
        }

        refresh()
    }

    private fun getUnitPrice(product: MenuProduct): Int {
        if (!product.isDrink) return product.price

        val sizeAdjustment = when (size) {
            "Pequeño" -> -1000
            "Grande" -> 1500
            else -> 0
        }

        val milkAdjustment = if (milk == "Almendras") {
            2000
        } else {
            0
        }

        val extraAdjustment = if (extraShot) {
            2000
        } else {
            0
        }

        return product.price +
                sizeAdjustment +
                milkAdjustment +
                extraAdjustment
    }

    private fun updatePrice(
        root: View,
        product: MenuProduct
    ) {
        val unitPrice = getUnitPrice(product)
        val total = unitPrice * quantity

        root.findViewById<TextView>(
            R.id.detail_unit_price
        ).text = formatPrice(unitPrice)

        root.findViewById<TextView>(
            R.id.detail_button_price
        ).text = formatPrice(total)
    }

    private fun addToCart(product: MenuProduct) {
        val isDefaultSelection = !product.isDrink || (
                size == "Mediano" &&
                        milk == "Tradicional" &&
                        sweetness == "Normal" &&
                        ice == "Con hielo" &&
                        !extraShot
                )

        val variantKey = listOf(
            product.id,
            size,
            milk,
            sweetness,
            ice,
            extraShot
        ).joinToString("|")

        val cartId = if (isDefaultSelection) {
            product.id
        } else {
            variantKey.hashCode()
        }

        val finalDescription = if (
            !product.isDrink || isDefaultSelection
        ) {
            product.description
        } else {
            buildString {
                append(product.description)
                append("\n")
                append("Tamaño: $size")
                append(" · Leche: $milk")
                append(" · Dulzor: $sweetness")

                if (product.isIced) {
                    append(" · $ice")
                }

                if (extraShot) {
                    append(" · Shot extra")
                }
            }
        }

        cartViewModel.addProduct(
            CartItem(
                id = cartId,
                name = product.name,
                description = finalDescription,
                price = getUnitPrice(product),
                imageRes = product.imageRes,
                quantity = quantity
            )
        )
    }

    private fun formatPrice(price: Int): String {
        return "\$${formatter.format(price)} COP"
    }
}
