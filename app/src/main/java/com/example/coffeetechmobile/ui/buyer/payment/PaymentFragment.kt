package com.example.coffeetechmobile.ui.buyer.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.ui.buyer.cart.CartViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private val cartViewModel: CartViewModel by activityViewModels()

    private val priceFormatter = NumberFormat.getIntegerInstance(
        Locale("es", "CO")
    )

    private var selectedMethod = PaymentMethodType.PSE

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Regresar al carrito
        view.findViewById<View>(
            R.id.payment_back_button
        ).setOnClickListener {
            findNavController().navigateUp()
        }

        setupPaymentMethods(view)
        setupOrderSummary(view)
        setupPaymentConfirmation(view)
    }

    private fun setupPaymentMethods(root: View) {

        val container = root.findViewById<LinearLayout>(
            R.id.payment_methods_container
        )

        val methods = listOf(
            PaymentMethod(
                type = PaymentMethodType.PSE,
                name = getString(R.string.payment_pse_name),
                description = getString(R.string.payment_pse_description),
                iconRes = R.drawable.ic_wallet,
                badge = getString(R.string.payment_popular),
                details = getString(R.string.payment_pse_details)
            ),
            PaymentMethod(
                type = PaymentMethodType.DIGITAL_WALLET,
                name = getString(R.string.payment_wallet_name),
                description = getString(R.string.payment_wallet_description),
                iconRes = R.drawable.ic_phone,
                details = getString(R.string.payment_wallet_details)
            ),
            PaymentMethod(
                type = PaymentMethodType.CARD,
                name = getString(R.string.payment_card_name),
                description = getString(R.string.payment_card_description),
                iconRes = R.drawable.ic_credit_card,
                details = getString(R.string.payment_card_details)
            ),
            PaymentMethod(
                type = PaymentMethodType.CASH_ON_DELIVERY,
                name = getString(R.string.payment_cash_name),
                description = getString(R.string.payment_cash_description),
                iconRes = R.drawable.ic_hand_coins,
                details = getString(R.string.payment_cash_details)
            )
        )

        val methodCards =
            mutableMapOf<PaymentMethodType, MaterialCardView>()

        val coffeeColor = ContextCompat.getColor(
            requireContext(),
            R.color.coffee_primary
        )

        val borderColor = android.graphics.Color.parseColor(
            "#EAD9CA"
        )

        val density = resources.displayMetrics.density

        fun updateSelection() {

            methods.forEach { method ->

                val card = methodCards[method.type]
                    ?: return@forEach

                val isSelected = method.type == selectedMethod

                card.isSelected = isSelected

                card.strokeColor = if (isSelected) {
                    coffeeColor
                } else {
                    borderColor
                }

                card.strokeWidth = (
                        (if (isSelected) 2 else 1) * density
                        ).toInt()

                card.findViewById<RadioButton>(
                    R.id.payment_method_radio
                ).isChecked = isSelected

                val details = card.findViewById<LinearLayout>(
                    R.id.payment_method_details
                )

                details.visibility =
                    if (isSelected && method.details != null) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }

        methods.forEach { method ->

            val card = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.item_payment_method,
                    container,
                    false
                ) as MaterialCardView

            card.findViewById<TextView>(
                R.id.payment_method_name
            ).text = method.name

            card.findViewById<TextView>(
                R.id.payment_method_description
            ).text = method.description

            card.findViewById<ImageView>(
                R.id.payment_method_icon
            ).setImageResource(method.iconRes)

            val badge = card.findViewById<TextView>(
                R.id.payment_method_badge
            )

            if (method.badge != null) {
                badge.text = method.badge
                badge.visibility = View.VISIBLE
            } else {
                badge.visibility = View.GONE
            }

            card.findViewById<TextView>(
                R.id.payment_method_details_description
            ).text = method.details.orEmpty()

            card.setOnClickListener {
                selectedMethod = method.type
                updateSelection()
            }

            methodCards[method.type] = card
            container.addView(card)
        }

        updateSelection()
    }

    private fun setupOrderSummary(root: View) {

        val subtotalText = root.findViewById<TextView>(
            R.id.cart_summary_subtotal
        )

        val discountText = root.findViewById<TextView>(
            R.id.cart_summary_discount
        )

        val shippingText = root.findViewById<TextView>(
            R.id.cart_summary_shipping
        )

        val totalText = root.findViewById<TextView>(
            R.id.cart_summary_total
        )

        val bottomTotal = root.findViewById<TextView>(
            R.id.payment_bottom_total
        )

        val confirmButton = root.findViewById<View>(
            R.id.payment_confirm_button
        )

        cartViewModel.items.observe(viewLifecycleOwner) { items ->

            val hasProducts = items.isNotEmpty()

            subtotalText.text = formatPrice(
                cartViewModel.getSubtotal()
            )

            discountText.text = formatPrice(
                CartViewModel.DISCOUNT
            )

            shippingText.text = formatPrice(
                if (hasProducts) CartViewModel.SHIPPING_COST else 0
            )

            val total = formatPrice(
                cartViewModel.getTotal()
            )

            totalText.text = total
            bottomTotal.text = total

            confirmButton.isEnabled = hasProducts
            confirmButton.alpha = if (hasProducts) 1f else 0.5f
        }
    }

    private fun setupPaymentConfirmation(root: View) {

        val termsCheckbox = root.findViewById<CheckBox>(
            R.id.payment_terms_checkbox
        )

        val confirmButton = root.findViewById<View>(
            R.id.payment_confirm_button
        )

        confirmButton.setOnClickListener {

            if (cartViewModel.items.value.orEmpty().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.payment_empty_cart,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!termsCheckbox.isChecked) {
                Toast.makeText(
                    requireContext(),
                    R.string.payment_accept_terms,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val methodName = when (selectedMethod) {
                PaymentMethodType.PSE ->
                    getString(R.string.payment_pse_name)

                PaymentMethodType.DIGITAL_WALLET ->
                    getString(R.string.payment_wallet_name)

                PaymentMethodType.CARD ->
                    getString(R.string.payment_card_name)

                PaymentMethodType.CASH_ON_DELIVERY ->
                    getString(R.string.payment_cash_name)
            }

            val message = getString(
                R.string.payment_confirmation_message,
                methodName,
                formatPrice(cartViewModel.getTotal())
            )

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.payment_confirmation_title)
                .setMessage(message)
                .setNegativeButton(
                    R.string.payment_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.payment_confirm_simulation
                ) { _, _ ->
                    Toast.makeText(
                        requireContext(),
                        R.string.payment_simulation_success,
                        Toast.LENGTH_LONG
                    ).show()
                }
                .show()
        }
    }

    private fun formatPrice(value: Int): String {
        return getString(
            R.string.cart_price_format,
            "\$${priceFormatter.format(value)}"
        )
    }
}
