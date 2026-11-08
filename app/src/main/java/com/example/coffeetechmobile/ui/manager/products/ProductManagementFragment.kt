package com.example.coffeetechmobile.ui.manager.products

import android.os.Bundle
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R

class ProductManagementFragment : Fragment(R.layout.fragment_product_management) {

    private lateinit var adapter: ProductAdapter
    private val products = getSampleProducts().toMutableList()
    private var selectedCategory: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.products_recycler_view)
        adapter = ProductAdapter(products, ::editProduct, ::confirmDelete)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        bindStat(
            view,
            R.id.product_stat_hot,
            R.drawable.ic_coffee,
            R.drawable.bg_stat_total,
            "14",
            "Bebidas calientes",
            "#916A45",
            "#D9C8B8"
        )

        bindStat(
            view,
            R.id.product_stat_cold,
            R.drawable.ic_shopping_bag,
            R.drawable.bg_stat_clients,
            "14",
            "Bebidas frías",
            "#80572E",
            "#D9C8B8"
        )

        bindStat(
            view,
            R.id.product_stat_bakery,
            R.drawable.ic_shopping_basket,
            R.drawable.bg_stat_sales,
            "24",
            "Panadería y pastelería",
            "#F3DDD5",
            "#85675F"
        )

        bindStat(
            view,
            R.id.product_stat_breakfast,
            R.drawable.ic_coffee_maker,
            R.drawable.bg_stat_employees,
            "25",
            "Desayunos y brunch",
            "#B77738",
            "#E1BFA1"
        )


        val result = findNavController().currentBackStackEntry?.savedStateHandle
        result?.getLiveData<ProductItem>("created_product")?.observe(viewLifecycleOwner) {
            adapter.addProduct(it)
            val query = view.findViewById<EditText>(R.id.product_search_input).text.toString()
            adapter.filter(query, selectedCategory)
            result.remove<ProductItem>("created_product")
        }
        result?.getLiveData<ProductItem>("updated_product")?.observe(viewLifecycleOwner) {
            adapter.updateProduct(it)
            result.remove<ProductItem>("updated_product")
        }

        view.findViewById<View>(R.id.create_product_button).setOnClickListener {
            findNavController().navigate(R.id.action_productManagementFragment_to_createProductFragment)
        }

        view.findViewById<EditText>(R.id.product_search_input).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter(s?.toString().orEmpty(), selectedCategory)
                }
                override fun afterTextChanged(s: Editable?) = Unit
            }
        )

        bindCategoryFilter(view, R.id.product_filter_all, null)
        bindCategoryFilter(view, R.id.product_filter_hot, "Bebidas calientes")
        bindCategoryFilter(view, R.id.product_filter_cold, "Bebidas frías")
        bindCategoryFilter(view, R.id.product_filter_bakery, "Pastelería")
    }

    private fun bindCategoryFilter(root: View, id: Int, category: String?) {
        root.findViewById<View>(id).setOnClickListener {
            selectedCategory = category
            val query = root.findViewById<EditText>(R.id.product_search_input).text.toString()
            adapter.filter(query, selectedCategory)
        }
    }

    private fun editProduct(product: ProductItem) {
        findNavController().navigate(
            R.id.action_productManagementFragment_to_createProductFragment,
            bundleOf("product_to_edit" to product)
        )
    }

    private fun confirmDelete(product: ProductItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_product)
            .setMessage(getString(R.string.delete_product_confirmation, product.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_product) { _, _ ->
                adapter.removeProduct(product)
                Toast.makeText(requireContext(), R.string.product_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun bindStat(
        root: View,
        cardId: Int,
        iconRes: Int,
        backgroundRes: Int,
        value: String,
        label: String,
        contentColor: String,
        badgeBackgroundColor: String
    ) {
        val card = root.findViewById<View>(cardId)

        card.setBackgroundResource(backgroundRes)

        val color = Color.parseColor(contentColor)

        val icon = card.findViewById<ImageView>(R.id.stat_icon)
        icon.setImageResource(iconRes)
        icon.imageTintList = ColorStateList.valueOf(color)

        val badge = card.findViewById<TextView>(R.id.stat_badge)
        badge.text = "Productos"
        badge.setTextColor(color)
        badge.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor(badgeBackgroundColor)
        )

        val statValue = card.findViewById<TextView>(R.id.stat_value)
        statValue.text = value
        statValue.setTextColor(color)

        val statLabel = card.findViewById<TextView>(R.id.stat_label)
        statLabel.text = label
        statLabel.setTextColor(color)
        statLabel.maxLines = 2
    }

    private fun getSampleProducts(): List<ProductItem> = listOf(
        ProductItem(
            name = "Iced caramel latte",
            category = "Bebidas frías",
            description = "Café espresso mezclado con leche fría y caramelo.",
            salePrice = 7000,
            preparationCost = 3500,
            taxRate = 19,
            initialStock = 45,
            minimumStock = 10,
            imageRes = R.drawable.img_iced_caramel_latte
        ),
        ProductItem(
            name = "Frappé de oreo",
            category = "Bebidas frías",
            description = "Combinación de espresso, chocolate y leche.",
            salePrice = 8000,
            preparationCost = 4000,
            taxRate = 19,
            initialStock = 30,
            minimumStock = 8,
            imageRes = R.drawable.img_frappe_oreo
        ),
        ProductItem(
            name = "Muffin de red velvet",
            category = "Pastelería",
            description = "Muffin suave y esponjoso con glaseado.",
            salePrice = 5000,
            preparationCost = 2200,
            taxRate = 5,
            initialStock = 24,
            minimumStock = 6,
            imageRes = R.drawable.img_muffin_red_velvet
        ),
        ProductItem(
            name = "Iced macchiato",
            category = "Bebidas frías",
            description = "Espresso manchado con leche fría y hielo.",
            salePrice = 6500,
            preparationCost = 3000,
            taxRate = 19,
            initialStock = 40,
            minimumStock = 10,
            imageRes = R.drawable.img_iced_macchiato
        )
    )
}
