package com.example.coffeetechmobile.ui.manager.dashboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.catalog.MenuCatalog
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardFragment :
    Fragment(R.layout.fragment_admin_dashboard) {

    private val formatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    private enum class Period {
        MONTH, TODAY, WEEK
    }

    private data class DashboardData(
        val title: String,
        val revenue: Int,
        val transactions: Int,
        val growth: String,
        val hourlySales: List<Int>,
        val productUnits: List<Int>
    )

    private val dataByPeriod = mapOf(
        Period.MONTH to DashboardData(
            title = "Este mes · Datos de ejemplo",
            revenue = 18_450_000,
            transactions = 354,
            growth = "+14,2 %",
            hourlySales = listOf(16, 68, 92, 54, 42, 76, 25),
            productUnits = listOf(142, 89, 74)
        ),
        Period.TODAY to DashboardData(
            title = "Hoy · Datos de ejemplo",
            revenue = 675_000,
            transactions = 13,
            growth = "+7,1 %",
            hourlySales = listOf(2, 8, 13, 6, 4, 10, 3),
            productUnits = listOf(6, 4, 3)
        ),
        Period.WEEK to DashboardData(
            title = "Esta semana · Datos de ejemplo",
            revenue = 4_560_000,
            transactions = 88,
            growth = "+9,8 %",
            hourlySales = listOf(8, 34, 51, 28, 24, 43, 12),
            productUnits = listOf(35, 22, 19)
        )
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.dashboard_bottom_navigation
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.DASHBOARD
        )

        setupPeriodFilters(view)

        view.findViewById<View>(
            R.id.dashboard_export_button
        ).setOnClickListener {
            Toast.makeText(
                requireContext(),
                R.string.dashboard_export_pending,
                Toast.LENGTH_SHORT
            ).show()
        }

        showDashboard(view, Period.MONTH)
    }

    private fun setupPeriodFilters(root: View) {
        root.findViewById<View>(
            R.id.dashboard_filter_month
        ).setOnClickListener {
            showDashboard(root, Period.MONTH)
        }

        root.findViewById<View>(
            R.id.dashboard_filter_today
        ).setOnClickListener {
            showDashboard(root, Period.TODAY)
        }

        root.findViewById<View>(
            R.id.dashboard_filter_week
        ).setOnClickListener {
            showDashboard(root, Period.WEEK)
        }
    }

    private fun showDashboard(
        root: View,
        period: Period
    ) {
        val data = dataByPeriod[period] ?: return

        updateFilters(root, period)

        root.findViewById<TextView>(
            R.id.dashboard_revenue
        ).text = formatMoney(data.revenue)

        root.findViewById<TextView>(
            R.id.dashboard_growth
        ).text = data.growth

        root.findViewById<TextView>(
            R.id.dashboard_period_label
        ).text = data.title

        val average = if (data.transactions > 0) {
            data.revenue / data.transactions
        } else {
            0
        }

        root.findViewById<TextView>(
            R.id.dashboard_average
        ).text = formatMoney(average)

        root.findViewById<TextView>(
            R.id.dashboard_transactions_value
        ).text = formatter.format(data.transactions)

        renderSalesChart(root, data.hourlySales)
        renderTopProducts(root, data.productUnits)
        renderPaymentMethods(root, data.revenue)
    }

    private fun updateFilters(
        root: View,
        selectedPeriod: Period
    ) {
        val filters = listOf(
            Period.MONTH to R.id.dashboard_filter_month,
            Period.TODAY to R.id.dashboard_filter_today,
            Period.WEEK to R.id.dashboard_filter_week
        )

        val activeTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.background_app
        )

        val inactiveTextColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        filters.forEach { (period, viewId) ->
            val button = root.findViewById<TextView>(viewId)
            val isSelected = period == selectedPeriod

            button.setBackgroundResource(
                if (isSelected) {
                    R.drawable.bg_filter_selected
                } else {
                    R.drawable.bg_dashboard_filter_unselected
                }
            )

            button.setTextColor(
                if (isSelected) activeTextColor
                else inactiveTextColor
            )

            button.compoundDrawableTintList =
                android.content.res.ColorStateList.valueOf(
                    if (isSelected) activeTextColor
                    else inactiveTextColor
                )

            button.isSelected = isSelected
        }
    }

    private fun renderSalesChart(
        root: View,
        values: List<Int>
    ) {
        val container = root.findViewById<LinearLayout>(
            R.id.dashboard_chart_container
        )

        container.removeAllViews()

        val labels = listOf(
            "6–8", "8–10", "10–12", "12–14",
            "14–16", "16–18", "18–20"
        )

        val maximum = values.maxOrNull()?.coerceAtLeast(1) ?: 1

        val peakIndex = values.indexOf(maximum)

        val primaryColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        val secondaryColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_secondary
        )

        values.forEachIndexed { index, value ->

            val column = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val columnParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            columnParams.marginEnd = dp(4)

            val barArea = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val barAreaParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            val barHeight = dp(
                12 + (value * 108 / maximum)
            )

            val bar = View(requireContext()).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = floatArrayOf(
                        dp(7).toFloat(),
                        dp(7).toFloat(),
                        dp(7).toFloat(),
                        dp(7).toFloat(),
                        0f, 0f, 0f, 0f
                    )
                    setColor(
                        if (index == peakIndex) primaryColor
                        else secondaryColor
                    )
                }
            }

            barArea.addView(
                bar,
                LinearLayout.LayoutParams(
                    dp(23),
                    barHeight
                )
            )

            val label = TextView(requireContext()).apply {
                text = labels.getOrElse(index) { "" }
                textSize = 9f
                gravity = Gravity.CENTER
                setTextColor(secondaryColor)
            }

            val labelParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(26)
            )

            column.addView(barArea, barAreaParams)
            column.addView(label, labelParams)

            container.addView(column, columnParams)
        }

        root.findViewById<TextView>(
            R.id.dashboard_peak_label
        ).text = if (peakIndex >= 0) {
            "Mayor actividad: ${labels[peakIndex]} h · Datos de ejemplo"
        } else {
            "Sin actividad"
        }
    }

    private fun renderTopProducts(
        root: View,
        units: List<Int>
    ) {
        val container = root.findViewById<LinearLayout>(
            R.id.dashboard_top_products_container
        )

        container.removeAllViews()

        val productIds = listOf(
            R.id.product_iced_caramel,
            R.id.product_frappe_oreo,
            R.id.product_muffin_red_velvet
        )

        val maxUnits = units.maxOrNull()?.coerceAtLeast(1) ?: 1

        productIds.forEachIndexed { index, productId ->

            val product = MenuCatalog.findById(productId)
                ?: return@forEachIndexed

            val soldUnits = units.getOrElse(index) { 0 }

            val item = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.item_dashboard_top_product,
                    container,
                    false
                )

            item.findViewById<ImageView>(
                R.id.dashboard_product_image
            ).setImageResource(product.imageRes)

            item.findViewById<TextView>(
                R.id.dashboard_product_name
            ).text = product.name

            item.findViewById<TextView>(
                R.id.dashboard_product_category
            ).text = product.category

            item.findViewById<TextView>(
                R.id.dashboard_product_units
            ).text = "$soldUnits uds."

            item.findViewById<TextView>(
                R.id.dashboard_product_revenue
            ).text = formatMoney(
                soldUnits * product.price
            )

            item.findViewById<ProgressBar>(
                R.id.dashboard_product_progress
            ).progress = soldUnits * 100 / maxUnits

            container.addView(item)
        }
    }

    private fun renderPaymentMethods(
        root: View,
        revenue: Int
    ) {
        val distribution = root.findViewById<LinearLayout>(
            R.id.dashboard_payment_distribution
        )

        val rows = root.findViewById<LinearLayout>(
            R.id.dashboard_payment_rows
        )

        distribution.removeAllViews()
        rows.removeAllViews()

        val methods = listOf(
            Triple("PSE / Débito bancario", 45, "#3E2723"),
            Triple("Nequi / Daviplata", 35, "#79512F"),
            Triple("Tarjetas", 20, "#F0BC8F")
        )

        methods.forEach { (name, percentage, hexColor) ->

            val color = Color.parseColor(hexColor)

            val segment = View(requireContext()).apply {
                setBackgroundColor(color)
            }

            val segmentParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                percentage.toFloat()
            )

            distribution.addView(segment, segmentParams)

            val amount = revenue.toLong() * percentage / 100L

            addPaymentRow(
                container = rows,
                name = name,
                percentage = percentage,
                amount = amount,
                color = color
            )
        }
    }

    private fun addPaymentRow(
        container: LinearLayout,
        name: String,
        percentage: Int,
        amount: Long,
        color: Int
    ) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(2), dp(12), dp(2), dp(12)
            )
        }

        val marker = View(requireContext()).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
        }

        row.addView(
            marker,
            LinearLayout.LayoutParams(
                dp(14),
                dp(14)
            )
        )

        val nameText = TextView(requireContext()).apply {
            text = name
            textSize = 13f
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_dark
                )
            )
        }

        val nameParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        nameParams.marginStart = dp(10)
        row.addView(nameText, nameParams)

        val valueText = TextView(requireContext()).apply {
            text = "${formatMoney(amount)}\n$percentage %"
            textSize = 12f
            gravity = Gravity.END
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.coffee_primary
                )
            )
        }

        row.addView(valueText)
        container.addView(row)
    }

    private fun formatMoney(value: Int): String {
        return formatMoney(value.toLong())
    }

    private fun formatMoney(value: Long): String {
        return "\$${formatter.format(value)} COP"
    }

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density + 0.5f
                ).toInt()
    }
}
