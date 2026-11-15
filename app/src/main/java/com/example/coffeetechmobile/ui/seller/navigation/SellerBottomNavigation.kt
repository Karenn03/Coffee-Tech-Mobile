package com.example.coffeetechmobile.ui.seller.navigation

import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import com.example.coffeetechmobile.R

enum class SellerTab {
    HOME,
    PRODUCTS,
    ORDERS
}

/**
 * Menú inferior del vendedor con Inicio, Productos e Historial de pedidos
 *
 * Usa tres posiciones equilibradas y cambia el fondo curveado según la tab
 * activa, manteniendo el mismo lenguaje visual del resto de la aplicación
 */
fun setupSellerBottomNavigation(
    root: View,
    navController: NavController,
    selectedTab: SellerTab
) {
    val navigation = root as ConstraintLayout

    val background = navigation.findViewById<ImageView>(
        R.id.seller_navigation_background
    )

    background.setImageResource(
        when (selectedTab) {
            SellerTab.HOME ->
                R.drawable.bg_seller_bottom_navigation_home

            SellerTab.PRODUCTS ->
                R.drawable.bg_seller_bottom_navigation_products

            SellerTab.ORDERS ->
                R.drawable.bg_seller_bottom_navigation_orders
        }
    )

    val homeButton = navigation.findViewById<ImageButton>(
        R.id.seller_home_navigation_button
    )

    val productsButton = navigation.findViewById<ImageButton>(
        R.id.seller_products_navigation_button
    )

    val ordersButton = navigation.findViewById<ImageButton>(
        R.id.seller_orders_navigation_button
    )

    fun dp(value: Int): Int {
        return (
            value * navigation.resources.displayMetrics.density + 0.5f
            ).toInt()
    }

    /**
     * Calcula el bias necesario para que el centro del botón coincida con
     * el centro del hueco de la barra, compensando el ancho del botón
     */
    fun biasFor(
        notchFraction: Float,
        buttonWidthDp: Int
    ): Float {
        val parentWidth = if (navigation.width > 0) {
            navigation.width.toFloat()
        } else {
            navigation.resources.displayMetrics.widthPixels.toFloat()
        }

        if (parentWidth <= 0f) return notchFraction

        val widthFraction = dp(buttonWidthDp) / parentWidth

        if (widthFraction >= 1f) return notchFraction

        return (
            notchFraction - widthFraction / 2f
            ) / (1f - widthFraction)
    }

    fun configureButton(
        button: ImageButton,
        selected: Boolean,
        selectedBias: Float,
        normalBias: Float,
        normalPadding: Int
    ) {
        val params = button.layoutParams as
                ConstraintLayout.LayoutParams

        params.width = dp(if (selected) 54 else 36)
        params.height = dp(if (selected) 54 else 36)

        params.topToTop = if (selected) {
            ConstraintLayout.LayoutParams.PARENT_ID
        } else {
            ConstraintLayout.LayoutParams.UNSET
        }

        params.bottomToBottom = if (selected) {
            ConstraintLayout.LayoutParams.UNSET
        } else {
            ConstraintLayout.LayoutParams.PARENT_ID
        }

        params.bottomMargin = if (selected) 0 else dp(8)

        params.horizontalBias = if (selected) {
            selectedBias
        } else {
            normalBias
        }

        button.layoutParams = params

        if (selected) {
            button.setBackgroundResource(
                R.drawable.bg_home_nav_button
            )
        } else {
            val value = TypedValue()

            button.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                value,
                true
            )

            if (value.resourceId != 0) {
                button.setBackgroundResource(value.resourceId)
            } else {
                button.background = null
            }
        }

        val padding = dp(if (selected) 12 else normalPadding)

        button.setPadding(padding, padding, padding, padding)
        button.elevation = dp(if (selected) 6 else 0).toFloat()
    }

    configureButton(
        button = homeButton,
        selected = selectedTab == SellerTab.HOME,
        selectedBias = biasFor(0.15f, 54),
        normalBias = biasFor(0.15f, 36),
        normalPadding = 9
    )

    configureButton(
        button = productsButton,
        selected = selectedTab == SellerTab.PRODUCTS,
        selectedBias = biasFor(0.5f, 54),
        normalBias = biasFor(0.5f, 36),
        normalPadding = 8
    )

    configureButton(
        button = ordersButton,
        selected = selectedTab == SellerTab.ORDERS,
        selectedBias = biasFor(0.85f, 54),
        normalBias = biasFor(0.85f, 36),
        normalPadding = 8
    )

    fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) {
            return
        }

        if (!navController.popBackStack(destinationId, false)) {
            navController.navigate(destinationId)
        }
    }

    homeButton.setOnClickListener {
        navigateTo(R.id.sellerHomeFragment)
    }

    productsButton.setOnClickListener {
        navigateTo(R.id.productManagementFragment)
    }

    ordersButton.setOnClickListener {
        navigateTo(R.id.sellerOrdersFragment)
    }
}
