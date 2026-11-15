package com.example.coffeetechmobile.ui.buyer.address

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.coffeetechmobile.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Pide un nombre (Casa, Trabajo, etc.) antes de guardar una dirección nueva
fun Fragment.showAddressLabelDialog(
    address: String,
    onSave: (String) -> Unit
) {
    val content = layoutInflater.inflate(
        R.layout.dialog_address_label,
        null,
        false
    )

    val labelLayout = content.findViewById<TextInputLayout>(
        R.id.address_label_layout
    )

    val labelInput = content.findViewById<TextInputEditText>(
        R.id.address_label_input
    )

    val dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.address_new_title)
        .setMessage(address)
        .setView(content)
        .setNegativeButton(R.string.address_cancel, null)
        .setPositiveButton(R.string.address_save, null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {
                val label = labelInput.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()

                if (label.isBlank()) {
                    labelLayout.error = getString(
                        R.string.address_label_error
                    )
                    return@setOnClickListener
                }

                labelLayout.error = null
                dialog.dismiss()
                onSave(label)
            }
    }

    dialog.show()
}
