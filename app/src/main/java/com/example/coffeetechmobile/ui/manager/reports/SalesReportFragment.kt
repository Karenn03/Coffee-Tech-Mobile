package com.example.coffeetechmobile.ui.manager.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation
import java.text.NumberFormat
import java.util.Locale

class SalesReportFragment :
    Fragment(R.layout.fragment_sales_report) {

    private val reportViewModel: SalesReportViewModel by viewModels()

    private val formatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.report_bottom_navigation
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.REPORTS
        )

        setupFilters(view)
        observeReport(view)

        view.findViewById<View>(
            R.id.report_export_button
        ).setOnClickListener {
            Toast.makeText(
                requireContext(),
                R.string.report_export_pending,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupFilters(root: View) {
        root.findViewById<View>(
            R.id.report_filter_all
        ).setOnClickListener {
            reportViewModel.setFilter(SaleFilter.ALL)
        }

        root.findViewById<View>(
            R.id.report_filter_completed
        ).setOnClickListener {
            reportViewModel.setFilter(SaleFilter.COMPLETED)
        }

        root.findViewById<View>(
            R.id.report_filter_pending
        ).setOnClickListener {
            reportViewModel.setFilter(SaleFilter.PENDING)
        }
    }

    private fun observeReport(root: View) {

        reportViewModel.selectedFilter.observe(
            viewLifecycleOwner
        ) { filter ->
            updateFilterAppearance(root, filter)
        }

        reportViewModel.visibleSales.observe(
            viewLifecycleOwner
        ) { sales ->
            renderSales(root, sales)
            updateStatistics(root, sales)
        }
    }

    private fun updateFilterAppearance(
        root: View,
        selectedFilter: SaleFilter
    ) {
        val filters = listOf(
            SaleFilter.ALL to R.id.report_filter_all,
            SaleFilter.COMPLETED to R.id.report_filter_completed,
            SaleFilter.PENDING to R.id.report_filter_pending
        )

        val activeColor = ContextCompat.getColor(
            requireContext(),
            R.color.background_app
        )

        val inactiveColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        filters.forEach { (filter, id) ->
            val chip = root.findViewById<TextView>(id)
            val selected = filter == selectedFilter

            chip.setBackgroundResource(
                if (selected) {
                    R.drawable.bg_filter_selected
                } else {
                    R.drawable.bg_dashboard_filter_unselected
                }
            )

            chip.setTextColor(
                if (selected) activeColor else inactiveColor
            )

            chip.isSelected = selected
        }
    }

    private fun updateStatistics(
        root: View,
        sales: List<SalesReport>
    ) {
        root.findViewById<TextView>(
            R.id.report_total_amount
        ).text = formatMoney(
            reportViewModel.getTotalAmount()
        )

        root.findViewById<TextView>(
            R.id.report_completed_count
        ).text = reportViewModel.getCompletedCount().toString()

        root.findViewById<TextView>(
            R.id.report_pending_count
        ).text = reportViewModel.getPendingCount().toString()

        root.findViewById<TextView>(
            R.id.report_results_count
        ).text = getString(
            R.string.report_results_format,
            sales.size
        )
    }

    private fun renderSales(
        root: View,
        sales: List<SalesReport>
    ) {
        val container = root.findViewById<LinearLayout>(
            R.id.report_transactions_container
        )

        container.removeAllViews()

        sales.forEach { sale ->

            val card = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.item_sales_transaction,
                    container,
                    false
                )

            card.findViewById<TextView>(
                R.id.sale_id
            ).text = sale.id

            card.findViewById<TextView>(
                R.id.sale_date
            ).text = sale.date

            card.findViewById<TextView>(
                R.id.sale_customer
            ).text = sale.customer

            card.findViewById<TextView>(
                R.id.sale_products
            ).text = sale.products

            card.findViewById<TextView>(
                R.id.sale_payment_method
            ).text = sale.paymentMethod

            card.findViewById<TextView>(
                R.id.sale_amount
            ).text = formatMoney(sale.amount)

            val statusText = card.findViewById<TextView>(
                R.id.sale_status
            )

            when (sale.status) {

                SaleStatus.COMPLETED -> {
                    statusText.text = "Completada"

                    statusText.setBackgroundResource(
                        R.drawable.bg_sale_completed
                    )

                    statusText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.sale_completed_text
                        )
                    )
                }

                SaleStatus.PENDING -> {
                    statusText.text = "Pendiente"

                    statusText.setBackgroundResource(
                        R.drawable.bg_sale_pending
                    )

                    statusText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.sale_pending_text
                        )
                    )
                }
            }

            container.addView(card)
        }
    }

    private fun formatMoney(value: Int): String {
        return "\$${formatter.format(value)} COP"
    }
}
