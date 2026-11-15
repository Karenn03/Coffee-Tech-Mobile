package com.example.coffeetechmobile.ui.seller.orders

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.session.SessionManager
import com.example.coffeetechmobile.ui.profile.ProfileFragment
import com.example.coffeetechmobile.ui.seller.navigation.SellerTab
import com.example.coffeetechmobile.ui.seller.navigation.setupSellerBottomNavigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SellerOrdersFragment :
    Fragment(R.layout.fragment_seller_orders) {

    private val ordersViewModel: SellerOrdersViewModel by viewModels()

    private lateinit var ordersAdapter: SellerOrdersAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupSellerBottomNavigation(
            root = view.findViewById(R.id.seller_bottom_navigation),
            navController = findNavController(),
            selectedTab = SellerTab.ORDERS
        )

        val session = SessionManager(requireContext()).current()

        view.findViewById<TextView>(R.id.seller_orders_header_name).text =
            session?.fullName ?: "Coffee Tech"

        view.findViewById<TextView>(R.id.seller_orders_header_role).text =
            "Vendedor"

        // Perfil compartido del vendedor, desde donde puede cerrar sesión.
        view.findViewById<View>(R.id.profile_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.profileFragment,
                    ProfileFragment.createArguments(UserRole.SELLER)
                )
            }

        ordersAdapter = SellerOrdersAdapter { order ->
            confirmStatusUpdate(order)
        }

        val recyclerView = view.findViewById<RecyclerView>(
            R.id.seller_orders_recycler_view
        )

        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )
        recyclerView.adapter = ordersAdapter
        recyclerView.isNestedScrollingEnabled = false

        setupFilters(view)
        observeOrders(view, recyclerView)
    }

    override fun onResume() {
        super.onResume()

        // Un pedido nuevo del comprador aparece al volver a esta pantalla
        ordersViewModel.refresh()
    }

    private fun setupFilters(root: View) {
        root.findViewById<View>(
            R.id.seller_orders_filter_all
        ).setOnClickListener {
            ordersViewModel.setFilter(SellerOrderFilter.ALL)
        }

        root.findViewById<View>(
            R.id.seller_orders_filter_active
        ).setOnClickListener {
            ordersViewModel.setFilter(SellerOrderFilter.ACTIVE)
        }

        root.findViewById<View>(
            R.id.seller_orders_filter_delivered
        ).setOnClickListener {
            ordersViewModel.setFilter(SellerOrderFilter.DELIVERED)
        }
    }

    // Pregunta antes de avanzar el estado y explica cuál será el nuevo estado
    private fun confirmStatusUpdate(order: SellerOrder) {
        val nextStatus = order.status.next()

        if (nextStatus == null) {
            Toast.makeText(
                requireContext(),
                R.string.seller_order_already_delivered,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nextStatusLabel = getString(nextStatus.labelRes)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.seller_order_update_title)
            .setMessage(
                getString(
                    R.string.seller_order_update_message,
                    order.id,
                    nextStatusLabel
                )
            )
            .setNegativeButton(
                R.string.seller_order_update_cancel,
                null
            )
            .setPositiveButton(
                R.string.seller_order_update_confirm
            ) { _, _ ->
                ordersViewModel.advanceStatus(order.orderId)

                Toast.makeText(
                    requireContext(),
                    R.string.seller_order_updated,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun observeOrders(
        root: View,
        recyclerView: RecyclerView
    ) {
        val count = root.findViewById<TextView>(
            R.id.seller_orders_results_count
        )

        val emptyState = root.findViewById<View>(
            R.id.seller_orders_empty
        )

        ordersViewModel.visibleOrders.observe(
            viewLifecycleOwner
        ) { orders ->

            ordersAdapter.submitList(orders)

            count.text = getString(
                R.string.seller_orders_count,
                orders.size
            )

            recyclerView.visibility = if (orders.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

            emptyState.visibility = if (orders.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        ordersViewModel.selectedFilter.observe(
            viewLifecycleOwner
        ) { filter ->
            updateFilters(root, filter)
        }

        ordersViewModel.counts.observe(viewLifecycleOwner) { counts ->
            root.findViewById<TextView>(
                R.id.seller_orders_active_count
            ).text = counts.active.toString()

            root.findViewById<TextView>(
                R.id.seller_orders_delivered_count
            ).text = counts.delivered.toString()

            root.findViewById<TextView>(
                R.id.seller_stat_received
            ).text = counts.received.toString()

            root.findViewById<TextView>(
                R.id.seller_stat_preparing
            ).text = counts.preparing.toString()

            root.findViewById<TextView>(
                R.id.seller_stat_ready
            ).text = counts.ready.toString()
        }
    }


    private fun updateFilters(
        root: View,
        selectedFilter: SellerOrderFilter
    ) {
        data class FilterView(
            val filter: SellerOrderFilter,
            val containerId: Int,
            val iconId: Int,
            val textId: Int
        )

        val filters = listOf(
            FilterView(
                SellerOrderFilter.ALL,
                R.id.seller_orders_filter_all,
                R.id.seller_filter_all_icon,
                R.id.seller_filter_all_text
            ),
            FilterView(
                SellerOrderFilter.ACTIVE,
                R.id.seller_orders_filter_active,
                R.id.seller_filter_active_icon,
                R.id.seller_filter_active_text
            ),
            FilterView(
                SellerOrderFilter.DELIVERED,
                R.id.seller_orders_filter_delivered,
                R.id.seller_filter_delivered_icon,
                R.id.seller_filter_delivered_text
            )
        )

        val selectedColor = ContextCompat.getColor(
            requireContext(),
            R.color.background_app
        )

        val normalTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        val normalIconColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_secondary
        )

        filters.forEach { filterView ->

            val container = root.findViewById<View>(
                filterView.containerId
            )

            val icon = root.findViewById<ImageView>(
                filterView.iconId
            )

            val text = root.findViewById<TextView>(
                filterView.textId
            )

            val isSelected = filterView.filter == selectedFilter

            container.setBackgroundResource(
                if (isSelected) {
                    R.drawable.bg_filter_selected
                } else {
                    R.drawable.bg_dashboard_filter_unselected
                }
            )

            text.setTextColor(
                if (isSelected) selectedColor
                else normalTextColor
            )

            icon.imageTintList = ColorStateList.valueOf(
                if (isSelected) selectedColor
                else normalIconColor
            )

            container.isSelected = isSelected
        }
    }
}
