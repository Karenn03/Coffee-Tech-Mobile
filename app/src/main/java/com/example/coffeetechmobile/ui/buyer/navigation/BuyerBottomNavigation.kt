package com.example.coffeetechmobile.ui.buyer.navigation

import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import com.example.coffeetechmobile.R

enum class BuyerTab {
    HOME,
    CART
}

fun setupBuyerBottomNavigation(
    root: View,
    navController: NavController,
    selectedTab: BuyerTab
) {
    val navigation = root as ConstraintLayout

    val background = navigation.findViewById<ImageView>(
        R.id.bottom_navigation_background
    )

    background.setImageResource(
        if (selectedTab == BuyerTab.HOME) {
            R.drawable.bg_bottom_navigation
        } else {
            R.drawable.bg_bottom_navigation_cart
        }
    )

    val homeButton = navigation.findViewById<ImageButton>(
        R.id.home_navigation_button
    )

    val cartButton = navigation.findViewById<ImageButton>(
        R.id.cart_navigation_button
    )

    fun dp(value: Int): Int {
        return (value * navigation.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun configureButton(
        button: ImageButton,
        selected: Boolean,
        selectedBias: Float,
        normalBias: Float,
        normalPadding: Int
    ) {
        val params = button.layoutParams as ConstraintLayout.LayoutParams

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

            button.setBackgroundResource(value.resourceId)
        }

        val padding = dp(
            if (selected) 12 else normalPadding
        )

        button.setPadding(padding, padding, padding, padding)
        button.elevation = dp(if (selected) 6 else 0).toFloat()
    }

    configureButton(
        homeButton,
        selectedTab == BuyerTab.HOME,
        0.212f,
        0.212f,
        9
    )

    configureButton(
        cartButton,
        selectedTab == BuyerTab.CART,
        0.79f,
        0.75f,
        5
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
        navigateTo(R.id.homeFragment)
    }

    cartButton.setOnClickListener {
        navigateTo(R.id.cartFragment)
    }
}
