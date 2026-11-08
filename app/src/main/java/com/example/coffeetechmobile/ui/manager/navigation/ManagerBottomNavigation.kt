
package com.example.coffeetechmobile.ui.manager.navigation

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import com.example.coffeetechmobile.R

enum class ManagerTab {
    USERS,
    PRODUCTS
}

fun setupManagerBottomNavigation(
    root: View,
    navController: NavController,
    selectedTab: ManagerTab
) {
    val navigation = root as ConstraintLayout

    val background = navigation.findViewById<ImageView>(
        R.id.manager_navigation_background
    )

    background.setImageResource(
        if (selectedTab == ManagerTab.USERS) {
            R.drawable.bg_manager_bottom_navigation
        } else {
            R.drawable.bg_manager_bottom_navigation_products
        }
    )

    val userButton = navigation.findViewById<ImageButton>(
        R.id.manager_users_button
    )

    val productButton = navigation.findViewById<ImageButton>(
        R.id.manager_sales_button
    )

    fun configureButton(
        button: ImageButton,
        selected: Boolean,
        selectedBias: Float,
        normalBias: Float,
        normalPadding: Int
    ) {
        val density = navigation.resources.displayMetrics.density

        fun dp(value: Int): Int = (value * density + 0.5f).toInt()

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
        params.horizontalBias = if (selected) selectedBias else normalBias

        button.layoutParams = params

        if (selected) {
            button.setBackgroundResource(R.drawable.bg_home_nav_button)
        } else {
            val value = TypedValue()
            button.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                value,
                true
            )
            button.setBackgroundResource(value.resourceId)
        }

        val padding = dp(if (selected) 12 else normalPadding)
        button.setPadding(padding, padding, padding, padding)
        button.elevation = dp(if (selected) 6 else 0).toFloat()
    }

    configureButton(
        userButton,
        selectedTab == ManagerTab.USERS,
        0.385f,
        0.385f,
        9
    )

    configureButton(
        productButton,
        selectedTab == ManagerTab.PRODUCTS,
        0.59f,
        0.68f,
        8
    )

    fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        if (!navController.popBackStack(destinationId, false)) {
            navController.navigate(destinationId)
        }
    }

    userButton.setOnClickListener {
        navigateTo(R.id.userManagementFragment)
    }

    productButton.setOnClickListener {
        navigateTo(R.id.productManagementFragment)
    }
}
