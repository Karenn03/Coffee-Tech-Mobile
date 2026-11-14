package com.example.coffeetechmobile.ui.manager.products

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.Product
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation

/**
 * Gestión de productos del administrador
 *
 * Lista el menú real de SQLite, calcula las tarjetas por categoría y permite
 * crear, editar y eliminar productos. Las imágenes de los productos semilla
 * vienen de `drawable`; las nuevas se guardan en el almacenamiento interno
 */
class ProductManagementFragment :
    Fragment(R.layout.fragment_product_management) {

    private val viewModel: ProductManagementViewModel by viewModels()

    private lateinit var adapter: ProductAdapter

    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null
    private var searchQuery = ""
    private var currentPage = 1

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.product_bottom_navigation
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.PRODUCTS
        )

        // Se muestran ceros mientras llega la primera consulta a SQLite.
        renderStats(emptyList())

        val recycler = view.findViewById<RecyclerView>(
            R.id.products_recycler_view
        )

        adapter = ProductAdapter(
            products = mutableListOf(),
            onEdit = ::editProduct,
            onDelete = ::confirmDelete
        )

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        setupSearch(view)
        setupFilters(view)
        setupPagination(view)

        view.findViewById<View>(R.id.create_product_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_productManagementFragment_to_createProductFragment
                )
            }

        applyFilters()
        observeProducts()
    }

    override fun onResume() {
        super.onResume()

        // Al volver del formulario se recarga el menú desde SQLite.
        viewModel.loadProducts()
    }

    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            renderStats(products)
            updateFilterCounts()
            applyFilters()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()
            }
        }
    }

    private fun setupSearch(root: View) {
        root.findViewById<EditText>(R.id.product_search_input)
            .doAfterTextChanged { editable ->
                searchQuery = editable?.toString().orEmpty().trim()
                currentPage = 1
                applyFilters()
            }
    }

    private fun setupFilters(root: View) {
        bindCategoryFilter(
            root,
            R.id.product_filter_all,
            null
        )

        bindCategoryFilter(
            root,
            R.id.product_filter_hot,
            CATEGORY_HOT
        )

        bindCategoryFilter(
            root,
            R.id.product_filter_cold,
            CATEGORY_COLD
        )

        bindCategoryFilter(
            root,
            R.id.product_filter_bakery,
            CATEGORY_BAKERY
        )

        bindCategoryFilter(
            root,
            R.id.product_filter_breakfast,
            CATEGORY_BREAKFAST
        )
    }

    private fun bindCategoryFilter(
        root: View,
        containerId: Int,
        category: String?
    ) {
        root.findViewById<View>(containerId).setOnClickListener {
            selectedCategory = category
            currentPage = 1
            applyFilters()
        }
    }

    // Conecta los botones de página anterior y siguiente
    private fun setupPagination(root: View) {
        root.findViewById<View>(R.id.product_previous_page_button)
            .setOnClickListener {
                if (currentPage > 1) {
                    currentPage--
                    applyFilters()
                }
            }

        root.findViewById<View>(R.id.product_next_page_button)
            .setOnClickListener {
                currentPage++
                applyFilters()
            }
    }

    // Aplica búsqueda y categoría sobre la lista que vino de SQLite
    private fun applyFilters() {
        val query = searchQuery.trim()

        val filtered = allProducts.filter { product ->
            val matchesCategory = selectedCategory == null ||
                product.category == selectedCategory

            val matchesSearch = query.isEmpty() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)

            matchesCategory && matchesSearch
        }

        val totalPages = if (filtered.isEmpty()) {
            1
        } else {
            (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE
        }

        currentPage = currentPage.coerceIn(1, totalPages)

        val start = (currentPage - 1) * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, filtered.size)

        val pageProducts = if (filtered.isEmpty()) {
            emptyList()
        } else {
            filtered.subList(start, end)
        }

        adapter.submitList(
            pageProducts.map {
                ProductItem.fromProduct(requireContext(), it)
            }
        )

        updateFilterSelection()
        updateFilterCounts()
        updatePagination(currentPage, totalPages)
    }

    // Actualiza los contadores de categorías con los datos reales
    private fun updateFilterCounts() {
        val total = allProducts.size

        val hot = allProducts.count {
            it.category == CATEGORY_HOT
        }

        val cold = allProducts.count {
            it.category == CATEGORY_COLD
        }

        val bakery = allProducts.count {
            it.category == CATEGORY_BAKERY
        }

        val breakfast = allProducts.count {
            it.category == CATEGORY_BREAKFAST
        }

        setFilterText(
            R.id.product_filter_all_text,
            "Todos ($total)"
        )

        setFilterText(
            R.id.product_filter_hot_text,
            "$CATEGORY_HOT ($hot)"
        )

        setFilterText(
            R.id.product_filter_cold_text,
            "$CATEGORY_COLD ($cold)"
        )

        setFilterText(
            R.id.product_filter_bakery_text,
            "$CATEGORY_BAKERY ($bakery)"
        )

        setFilterText(
            R.id.product_filter_breakfast_text,
            "$CATEGORY_BREAKFAST ($breakfast)"
        )
    }

    private fun setFilterText(textId: Int, text: String) {
        requireView().findViewById<TextView>(textId).text = text
    }

    // Actualiza el texto y los botones de paginación
    private fun updatePagination(
        currentPage: Int,
        totalPages: Int
    ) {
        requireView().findViewById<TextView>(
            R.id.product_current_page_text
        ).text = "Página $currentPage de $totalPages"

        requireView().findViewById<View>(
            R.id.product_previous_page_button
        ).apply {
            isEnabled = currentPage > 1
            alpha = if (currentPage > 1) 1f else 0.4f
        }

        requireView().findViewById<View>(
            R.id.product_next_page_button
        ).apply {
            isEnabled = currentPage < totalPages
            alpha = if (currentPage < totalPages) 1f else 0.4f
        }
    }

    private fun updateFilterSelection() {
        bindFilterChip(
            R.id.product_filter_all,
            R.id.product_filter_all_icon,
            R.id.product_filter_all_text,
            selectedCategory == null
        )

        bindFilterChip(
            R.id.product_filter_hot,
            R.id.product_filter_hot_icon,
            R.id.product_filter_hot_text,
            selectedCategory == CATEGORY_HOT
        )

        bindFilterChip(
            R.id.product_filter_cold,
            R.id.product_filter_cold_icon,
            R.id.product_filter_cold_text,
            selectedCategory == CATEGORY_COLD
        )

        bindFilterChip(
            R.id.product_filter_bakery,
            R.id.product_filter_bakery_icon,
            R.id.product_filter_bakery_text,
            selectedCategory == CATEGORY_BAKERY
        )

        bindFilterChip(
            R.id.product_filter_breakfast,
            R.id.product_filter_breakfast_icon,
            R.id.product_filter_breakfast_text,
            selectedCategory == CATEGORY_BREAKFAST
        )
    }

    private fun bindFilterChip(
        containerId: Int,
        iconId: Int,
        textId: Int,
        selected: Boolean
    ) {
        val container = requireView().findViewById<View>(containerId)

        container.setBackgroundResource(
            if (selected) {
                R.drawable.bg_filter_selected
            } else {
                R.drawable.bg_filter_unselected
            }
        )

        val icon = requireView().findViewById<ImageView>(iconId)
        val text = requireView().findViewById<TextView>(textId)

        val iconColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_secondary
        )

        val textColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_primary
        )

        icon.imageTintList = ColorStateList.valueOf(iconColor)
        text.setTextColor(textColor)
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
            .setMessage(
                getString(
                    R.string.delete_product_confirmation,
                    product.name
                )
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_product) { _, _ ->
                viewModel.deleteProduct(product.productId)
            }
            .show()
    }

    // Actualiza las tarjetas superiores con los conteos por categoría
    private fun renderStats(products: List<Product>) {
        val hot = products.count { it.category == CATEGORY_HOT }
        val cold = products.count { it.category == CATEGORY_COLD }
        val bakery = products.count { it.category == CATEGORY_BAKERY }
        val breakfast = products.count {
            it.category == CATEGORY_BREAKFAST
        }

        bindStat(
            requireView(),
            R.id.product_stat_hot,
            R.drawable.ic_coffee,
            R.drawable.bg_stat_total,
            hot.toString(),
            CATEGORY_HOT,
            "#916A45",
            "#D9C8B8"
        )

        bindStat(
            requireView(),
            R.id.product_stat_cold,
            R.drawable.ic_snowflake,
            R.drawable.bg_stat_clients,
            cold.toString(),
            CATEGORY_COLD,
            "#80572E",
            "#D9C8B8"
        )

        bindStat(
            requireView(),
            R.id.product_stat_bakery,
            R.drawable.ic_croissant,
            R.drawable.bg_stat_sales,
            bakery.toString(),
            CATEGORY_BAKERY,
            "#F3DDD5",
            "#85675F"
        )

        bindStat(
            requireView(),
            R.id.product_stat_breakfast,
            R.drawable.ic_sandwich,
            R.drawable.bg_stat_employees,
            breakfast.toString(),
            CATEGORY_BREAKFAST,
            "#B77738",
            "#E1BFA1"
        )
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

    private companion object {
        const val PAGE_SIZE = 6
        const val CATEGORY_HOT = "Bebidas calientes"
        const val CATEGORY_COLD = "Bebidas frías"
        const val CATEGORY_BAKERY = "Panadería y pastelería"
        const val CATEGORY_BREAKFAST = "Desayunos y brunch"
    }
}
