package com.example.coffeetechmobile.ui.seller.orders

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R

class SellerOrdersFragment :
    Fragment(R.layout.fragment_seller_orders) {

    private val ordersViewModel: SellerOrdersViewModel by viewModels()

    private lateinit var ordersAdapter: SellerOrdersAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(
            R.id.seller_orders_back_button
        ).setOnClickListener {
            findNavController().navigateUp()
        }

        ordersAdapter = SellerOrdersAdapter()

        val recyclerView = view.findViewById<RecyclerView>(
            R.id.seller_orders_recycler_view
        )

        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )
        recyclerView.adapter = ordersAdapter
        recyclerView.isNestedScrollingEnabled = false

        view.findViewById<TextView>(
            R.id.seller_orders_active_count
        ).text = ordersViewModel.activeCount.toString()

        view.findViewById<TextView>(
            R.id.seller_orders_delivered_count
        ).text = ordersViewModel.deliveredCount.toString()

        view.findViewById<TextView>(
            R.id.seller_stat_received
        ).text = ordersViewModel.countByStatus(
            SellerOrderStatus.RECEIVED
        ).toString()

        view.findViewById<TextView>(
            R.id.seller_stat_preparing
        ).text = ordersViewModel.countByStatus(
            SellerOrderStatus.PREPARING
        ).toString()

        view.findViewById<TextView>(
            R.id.seller_stat_ready
        ).text = ordersViewModel.countByStatus(
            SellerOrderStatus.READY
        ).toString()

        setupFilters(view)
        observeOrders(view, recyclerView)
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
