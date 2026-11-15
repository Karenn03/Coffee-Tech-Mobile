package com.example.coffeetechmobile.ui.manager.dashboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.StoredOrder
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.ui.common.bindHeaderProfileButton
import com.example.coffeetechmobile.ui.common.bindSessionHeader
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation
import com.example.coffeetechmobile.ui.profile.ProfileFragment
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AdminDashboardFragment :
    Fragment(R.layout.fragment_admin_dashboard) {

    private val formatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    private val dashboardViewModel: AdminDashboardViewModel by viewModels()

    private var currentPeriod = Period.MONTH

    private enum class Period {
        MONTH, TODAY, WEEK
    }

    private data class TopProduct(
        val name: String,
        val units: Int,
        val revenue: Int,
        val imageRes: Int,
        val imagePath: String?
    )

    private data class PaymentShare(
        val name: String,
        val amount: Int
    )

    private data class DashboardData(
        val title: String,
        val revenue: Int,
        val transactions: Int,
        val growth: String,
        val hourlySales: List<Int>,
        val topProducts: List<TopProduct>,
        val paymentShares: List<PaymentShare>
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

        // El icono de perfil abre el perfil compartido para
        // que el administrador también pueda cerrar sesión
        view.findViewById<View>(R.id.profile_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.profileFragment,
                    ProfileFragment.createArguments(UserRole.ADMIN)
                )
            }

        view.findViewById<View>(
            R.id.dashboard_export_button
        ).setOnClickListener {
            Toast.makeText(
                requireContext(),
                R.string.dashboard_export_pending,
                Toast.LENGTH_SHORT
            ).show()
        }

        dashboardViewModel.orders.observe(viewLifecycleOwner) {
            showDashboard(view, currentPeriod)
        }

        showDashboard(view, currentPeriod)
        dashboardViewModel.refresh()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refresh()
        bindSessionHeader(requireView())
        bindHeaderProfileButton(requireView(), findNavController())
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
        currentPeriod = period

        val orders = dashboardViewModel.orders.value.orEmpty()
        val data = buildDashboardData(orders, period)

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
        renderTopProducts(root, data.topProducts)
        renderPaymentMethods(
            root,
            data.paymentShares,
            data.revenue
        )
    }

    private fun buildDashboardData(
        orders: List<StoredOrder>,
        period: Period
    ): DashboardData {
        val start = periodStart(period)
        val previousStart = previousPeriodStart(period, start)
        val now = System.currentTimeMillis()

        val periodOrders = orders.filter {
            it.createdAt in start..now
        }

        val previousOrders = orders.filter {
            it.createdAt >= previousStart && it.createdAt < start
        }

        val revenue = periodOrders.sumOf { it.total }
        val previousRevenue = previousOrders.sumOf { it.total }

        val title = when (period) {
            Period.MONTH -> getString(R.string.dashboard_month)
            Period.TODAY -> getString(R.string.dashboard_today)
            Period.WEEK -> getString(R.string.dashboard_week)
        }

        return DashboardData(
            title = title,
            revenue = revenue,
            transactions = periodOrders.size,
            growth = calculateGrowth(revenue, previousRevenue),
            hourlySales = buildHourlySales(periodOrders),
            topProducts = buildTopProducts(periodOrders),
            paymentShares = buildPaymentShares(periodOrders)
        )
    }

    private fun periodStart(period: Period): Long {
        val calendar = Calendar.getInstance()

        when (period) {
            Period.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }

            Period.WEEK -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, -6)
            }

            Period.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }

        return calendar.timeInMillis
    }

    private fun previousPeriodStart(
        period: Period,
        currentStart: Long
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentStart
        }

        when (period) {
            Period.TODAY -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            Period.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            Period.MONTH -> calendar.add(Calendar.MONTH, -1)
        }

        return calendar.timeInMillis
    }

    private fun calculateGrowth(
        currentRevenue: Int,
        previousRevenue: Int
    ): String {
        if (previousRevenue <= 0) {
            return "—"
        }

        val growth = (
            (currentRevenue - previousRevenue) * 100.0 /
                previousRevenue
            )

        return String.format(
            Locale("es", "CO"),
            "%+.1f %%",
            growth
        )
    }

    private fun buildHourlySales(
        orders: List<StoredOrder>
    ): List<Int> {
        val buckets = MutableList(7) { 0 }
        val calendar = Calendar.getInstance()

        orders.forEach { order ->
            calendar.timeInMillis = order.createdAt
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            if (hour in 6..19) {
                val index = (hour - 6) / 2
                buckets[index] = buckets[index] + 1
            }
        }

        return buckets
    }

    private fun buildTopProducts(
        orders: List<StoredOrder>
    ): List<TopProduct> {
        data class Accumulator(
            var units: Int,
            var revenue: Int,
            val imageRes: Int,
            val imagePath: String?
        )

        val products = mutableMapOf<String, Accumulator>()

        orders.forEach { order ->
            order.items.forEach { item ->
                val current = products[item.name]

                if (current == null) {
                    val drawableId = if (item.imageResName.isNotBlank()) {
                        resources.getIdentifier(
                            item.imageResName,
                            "drawable",
                            requireContext().packageName
                        )
                    } else {
                        0
                    }

                    products[item.name] = Accumulator(
                        units = item.quantity,
                        revenue = item.unitPrice * item.quantity,
                        imageRes = if (drawableId != 0) {
                            drawableId
                        } else {
                            R.drawable.ic_coffee
                        },
                        imagePath = item.imagePath.ifBlank { null }
                    )
                } else {
                    current.units += item.quantity
                    current.revenue += item.unitPrice * item.quantity
                }
            }
        }

        return products.map { (name, accumulator) ->
            TopProduct(
                name = name,
                units = accumulator.units,
                revenue = accumulator.revenue,
                imageRes = accumulator.imageRes,
                imagePath = accumulator.imagePath
            )
        }.sortedByDescending { it.units }
            .take(3)
    }

    private fun buildPaymentShares(
        orders: List<StoredOrder>
    ): List<PaymentShare> {
        return orders
            .groupBy { it.paymentMethod.ifBlank { "Sin método" } }
            .map { (name, methodOrders) ->
                PaymentShare(
                    name = name,
                    amount = methodOrders.sumOf { it.total }
                )
            }
            .sortedByDescending { it.amount }
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
            "Mayor actividad: ${labels[peakIndex]} h"
        } else {
            "Sin actividad"
        }
    }

    private fun renderTopProducts(
        root: View,
        products: List<TopProduct>
    ) {
        val container = root.findViewById<LinearLayout>(
            R.id.dashboard_top_products_container
        )

        container.removeAllViews()

        if (products.isEmpty()) {
            return
        }

        val maxUnits = products.maxOf { it.units }
            .coerceAtLeast(1)

        products.forEach { product ->
            val item = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.item_dashboard_top_product,
                    container,
                    false
                )

            val image = item.findViewById<ImageView>(
                R.id.dashboard_product_image
            )

            if (!product.imagePath.isNullOrBlank()) {
                image.setImageURI(
                    Uri.fromFile(File(product.imagePath))
                )
            } else {
                image.setImageResource(product.imageRes)
            }

            image.contentDescription = product.name

            item.findViewById<TextView>(
                R.id.dashboard_product_name
            ).text = product.name

            item.findViewById<TextView>(
                R.id.dashboard_product_category
            ).text = "Más vendido"

            item.findViewById<TextView>(
                R.id.dashboard_product_units
            ).text = "${product.units} uds."

            item.findViewById<TextView>(
                R.id.dashboard_product_revenue
            ).text = formatMoney(product.revenue)

            item.findViewById<ProgressBar>(
                R.id.dashboard_product_progress
            ).progress = product.units * 100 / maxUnits

            container.addView(item)
        }
    }

    private fun renderPaymentMethods(
        root: View,
        shares: List<PaymentShare>,
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

        if (shares.isEmpty()) {
            rows.addView(
                TextView(requireContext()).apply {
                    text = "Sin ventas registradas en este período."
                    textSize = 13f
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.coffee_secondary
                        )
                    )
                    setPadding(0, dp(12), 0, dp(12))
                }
            )
            return
        }

        val total = if (revenue > 0) {
            revenue
        } else {
            shares.sumOf { it.amount }
        }

        val colors = listOf(
            "#3E2723",
            "#79512F",
            "#F0BC8F",
            "#B08968"
        )

        shares.forEachIndexed { index, share ->
            if (share.amount <= 0) {
                return@forEachIndexed
            }

            val color = Color.parseColor(
                colors[index % colors.size]
            )

            val percentage = if (total > 0) {
                (
                    share.amount.toLong() * 100L / total
                    ).toInt().coerceAtLeast(1)
            } else {
                0
            }

            val segment = View(requireContext()).apply {
                setBackgroundColor(color)
            }

            val segmentParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                share.amount.toFloat()
            )

            distribution.addView(segment, segmentParams)

            addPaymentRow(
                container = rows,
                name = share.name,
                percentage = percentage,
                amount = share.amount.toLong(),
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
