package com.example.coffeetechmobile.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.ui.buyer.cart.CartItem
import com.example.coffeetechmobile.ui.buyer.cart.CartViewModel
import com.example.coffeetechmobile.ui.buyer.catalog.BuyerMenuViewModel
import com.example.coffeetechmobile.ui.buyer.catalog.BuyerProductAdapter
import com.example.coffeetechmobile.ui.buyer.catalog.MenuProduct
import com.example.coffeetechmobile.ui.buyer.navigation.BuyerTab
import com.example.coffeetechmobile.ui.buyer.navigation.setupBuyerBottomNavigation
import com.example.coffeetechmobile.ui.profile.ProfileFragment

/**
 * Inicio del comprador
 *
 * Carga el menú real de SQLite, lo pagina de 6 productos por página
 * y permite abrir el detalle o agregar directamente al carrito
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val cartViewModel: CartViewModel by activityViewModels()
    private val menuViewModel: BuyerMenuViewModel by viewModels()

    private lateinit var productAdapter: BuyerProductAdapter
    private var allProducts: List<MenuProduct> = emptyList()
    private var currentPage = 1
    private var selectedCategory: String? = null

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

        productAdapter = BuyerProductAdapter(
            onProductClick = { product ->
                findNavController().navigate(
                    R.id.action_homeFragment_to_productDetailFragment,
                    bundleOf("product_id" to product.productId)
                )
            },
            onAddToCart = { product ->
                addToCart(product)
            }
        )

        val recycler = view.findViewById<RecyclerView>(
            R.id.home_products_recycler_view
        )

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = productAdapter
        recycler.isNestedScrollingEnabled = false

        setupPagination(view)
        setupCategoryFilters(view)

        menuViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products.map {
                MenuProduct.fromProduct(requireContext(), it)
            }

            applyPage()
        }
    }

    override fun onResume() {
        super.onResume()

        // Recarga el menú al volver desde el detalle o el carrito
        menuViewModel.loadProducts()
    }

    private fun setupPagination(root: View) {
        root.findViewById<View>(R.id.home_previous_page_button)
            .setOnClickListener {
                if (currentPage > 1) {
                    currentPage--
                    applyPage()
                }
            }

        root.findViewById<View>(R.id.home_next_page_button)
            .setOnClickListener {
                currentPage++
                applyPage()
            }
    }

    // Conecta los chips de categoría y refresca su estado visual
    private fun setupCategoryFilters(root: View) {
        root.findViewById<View>(R.id.category_all)
            .setOnClickListener {
                selectCategory(null)
            }

        root.findViewById<View>(R.id.category_hot)
            .setOnClickListener {
                selectCategory(CATEGORY_HOT)
            }

        root.findViewById<View>(R.id.category_cold)
            .setOnClickListener {
                selectCategory(CATEGORY_COLD)
            }

        root.findViewById<View>(R.id.category_bakery)
            .setOnClickListener {
                selectCategory(CATEGORY_BAKERY)
            }

        root.findViewById<View>(R.id.category_breakfast)
            .setOnClickListener {
                selectCategory(CATEGORY_BREAKFAST)
            }

        updateCategorySelection()
    }

    private fun selectCategory(category: String?) {
        selectedCategory = category
        currentPage = 1
        updateCategorySelection()
        applyPage()
    }

    private fun updateCategorySelection() {
        bindCategoryChip(
            R.id.category_all,
            selectedCategory == null
        )

        bindCategoryChip(
            R.id.category_hot,
            selectedCategory == CATEGORY_HOT
        )

        bindCategoryChip(
            R.id.category_cold,
            selectedCategory == CATEGORY_COLD
        )

        bindCategoryChip(
            R.id.category_bakery,
            selectedCategory == CATEGORY_BAKERY
        )

        bindCategoryChip(
            R.id.category_breakfast,
            selectedCategory == CATEGORY_BREAKFAST
        )
    }

    private fun bindCategoryChip(
        containerId: Int,
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

        val group = container as? ViewGroup

        val icon = group?.getChildAt(0) as? ImageView
        val text = group?.getChildAt(1) as? TextView

        val iconColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_secondary
        )

        val textColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_primary
        )

        icon?.imageTintList = ColorStateList.valueOf(iconColor)
        text?.setTextColor(textColor)
    }

    private fun updateCategoryChipTexts() {
        val hot = allProducts.count { it.category == CATEGORY_HOT }
        val cold = allProducts.count { it.category == CATEGORY_COLD }
        val bakery = allProducts.count { it.category == CATEGORY_BAKERY }
        val breakfast = allProducts.count {
            it.category == CATEGORY_BREAKFAST
        }

        setCategoryChipText(
            R.id.category_all,
            "Todos (${allProducts.size})"
        )

        setCategoryChipText(
            R.id.category_hot,
            "$CATEGORY_HOT ($hot)"
        )

        setCategoryChipText(
            R.id.category_cold,
            "$CATEGORY_COLD ($cold)"
        )

        setCategoryChipText(
            R.id.category_bakery,
            "$CATEGORY_BAKERY ($bakery)"
        )

        setCategoryChipText(
            R.id.category_breakfast,
            "$CATEGORY_BREAKFAST ($breakfast)"
        )
    }

    private fun setCategoryChipText(
        containerId: Int,
        text: String
    ) {
        val container = requireView().findViewById<View>(containerId)
        val group = container as? ViewGroup
        (group?.getChildAt(1) as? TextView)?.text = text
    }

    // Aplica la página actual sobre la lista filtrada por categoría
    private fun applyPage() {
        val filteredProducts = if (selectedCategory == null) {
            allProducts
        } else {
            allProducts.filter {
                it.category == selectedCategory
            }
        }

        val totalPages = if (filteredProducts.isEmpty()) {
            1
        } else {
            (filteredProducts.size + PAGE_SIZE - 1) / PAGE_SIZE
        }

        currentPage = currentPage.coerceIn(1, totalPages)

        val start = (currentPage - 1) * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, filteredProducts.size)

        val pageProducts = if (filteredProducts.isEmpty()) {
            emptyList()
        } else {
            filteredProducts.subList(start, end)
        }

        productAdapter.submitList(pageProducts)

        updateCategoryChipTexts()
        updatePagination(currentPage, totalPages)
    }

    private fun updatePagination(
        currentPage: Int,
        totalPages: Int
    ) {
        requireView().findViewById<TextView>(
            R.id.home_current_page_text
        ).text = "Página $currentPage de $totalPages"

        requireView().findViewById<View>(
            R.id.home_previous_page_button
        ).apply {
            isEnabled = currentPage > 1
            alpha = if (currentPage > 1) 1f else 0.4f
        }

        requireView().findViewById<View>(
            R.id.home_next_page_button
        ).apply {
            isEnabled = currentPage < totalPages
            alpha = if (currentPage < totalPages) 1f else 0.4f
        }
    }

    private fun addToCart(product: MenuProduct) {
        cartViewModel.addProduct(
            CartItem(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                imageRes = product.imageRes,
                imagePath = product.imagePath,
                quantity = 1
            )
        )

        Toast.makeText(
            requireContext(),
            "${product.name} agregado al carrito",
            Toast.LENGTH_SHORT
        ).show()
    }

    private companion object {
        const val PAGE_SIZE = 6
        const val CATEGORY_HOT = "Bebidas calientes"
        const val CATEGORY_COLD = "Bebidas frías"
        const val CATEGORY_BAKERY = "Panadería y pastelería"
        const val CATEGORY_BREAKFAST = "Desayunos y brunch"
    }
}
