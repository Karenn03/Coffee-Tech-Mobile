package com.example.coffeetechmobile.ui.manager.navigation

import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import com.example.coffeetechmobile.R

enum class ManagerTab {
    DASHBOARD,
    USERS,
    PRODUCTS,
    REPORTS
}

fun setupManagerBottomNavigation(
    root: View,
    navController: NavController,
    selectedTab: ManagerTab
) {
    // El include puede reemplazar el ID de la vista raíz
    val navigation = root as ConstraintLayout

    val background = navigation.findViewById<ImageView>(
        R.id.manager_navigation_background
    )

    val homeButton = navigation.findViewById<ImageButton>(
        R.id.manager_home_button
    )

    val usersButton = navigation.findViewById<ImageButton>(
        R.id.manager_users_button
    )

    val productsButton = navigation.findViewById<ImageButton>(
        R.id.manager_sales_button
    )

    val reportsButton = navigation.findViewById<ImageButton>(
        R.id.manager_stats_button
    )

    background.setImageResource(
        when (selectedTab) {
            ManagerTab.DASHBOARD ->
                R.drawable.bg_manager_bottom_navigation_home

            ManagerTab.USERS ->
                R.drawable.bg_manager_bottom_navigation

            ManagerTab.PRODUCTS ->
                R.drawable.bg_manager_bottom_navigation_products

            ManagerTab.REPORTS ->
                R.drawable.bg_manager_bottom_navigation_reports
        }
    )

    fun dp(value: Int): Int {
        return (
                value * navigation.resources.displayMetrics.density + 0.5f
                ).toInt()
    }

    fun configureButton(
        button: ImageButton,
        selected: Boolean,
        selectedBias: Float,
        normalBias: Float
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

        val padding = dp(if (selected) 12 else 6)

        button.setPadding(
            padding,
            padding,
            padding,
            padding
        )

        button.elevation = if (selected) {
            dp(6).toFloat()
        } else {
            0f
        }

        button.isSelected = selected
    }

    configureButton(
        button = homeButton,
        selected = selectedTab == ManagerTab.DASHBOARD,
        selectedBias = 0.10f,
        normalBias = 0.10f
    )

    configureButton(
        button = usersButton,
        selected = selectedTab == ManagerTab.USERS,
        selectedBias = 0.385f,
        normalBias = 0.385f
    )

    configureButton(
        button = productsButton,
        selected = selectedTab == ManagerTab.PRODUCTS,
        selectedBias = 0.60f,
        normalBias = 0.68f
    )

    configureButton(
        button = reportsButton,
        selected = selectedTab == ManagerTab.REPORTS,
        selectedBias = 0.90f,
        normalBias = 0.90f
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
        navigateTo(R.id.adminDashboardFragment)
    }

    usersButton.setOnClickListener {
        navigateTo(R.id.userManagementFragment)
    }

    productsButton.setOnClickListener {
        navigateTo(R.id.productManagementFragment)
    }

    reportsButton.setOnClickListener {
        navigateTo(R.id.salesReportFragment)
    }
}
