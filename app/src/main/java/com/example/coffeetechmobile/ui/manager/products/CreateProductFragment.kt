package com.example.coffeetechmobile.ui.manager.products

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R

class CreateProductFragment : Fragment(R.layout.fragment_create_product) {

    private var productToEdit: ProductItem? = null
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult

        selectedImageUri = uri

        view?.let { root ->
            root.findViewById<ImageView>(
                R.id.product_form_image
            ).apply {
                setImageURI(uri)
                contentDescription = getString(
                    R.string.selected_product_image
                )
            }

            showProductImage(root)
        }
    }

    private fun showProductImage(root: View) {
        root.findViewById<ImageView>(
            R.id.product_form_image
        ).visibility = View.VISIBLE

        root.findViewById<View>(
            R.id.product_image_placeholder
        ).visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        styleTaxOption(
            view,
            R.id.tax_exempt,
            "Exento 0%",
            "Café en grano o tostado natural sin transformar"
        )

        styleTaxOption(
            view,
            R.id.tax_reduced,
            "Tarifa reducida 5%",
            "Insumos de maquinaria agrícola y empaques bio"
        )

        styleTaxOption(
            view,
            R.id.tax_general,
            "Tarifa general 19%",
            "Bebidas de servicio en mesa y barismo premium"
        )

        productToEdit = arguments?.getSerializable("product_to_edit") as? ProductItem
        productToEdit?.let { loadProduct(view, it) }

        view.findViewById<View>(R.id.product_form_image_picker).setOnClickListener {
            imagePicker.launch("image/*")
        }
        view.findViewById<View>(R.id.product_back_button).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.cancel_product_button).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.save_product_button).setOnClickListener {
            saveProduct(view)
        }
    }

    private fun saveProduct(root: View) {
        val name = root.findViewById<EditText>(R.id.product_name_input).text.toString().trim()
        val description = root.findViewById<EditText>(R.id.product_description_input)
            .text.toString().trim()
        val category = root.findViewById<Spinner>(R.id.product_category_spinner)
        val salePrice = parseAmount(root, R.id.product_sale_price_input)
        val preparationCost = parseAmount(root, R.id.product_preparation_cost_input)
        val stock = parseAmount(root, R.id.product_initial_stock_input)
        val minimumStock = parseAmount(root, R.id.product_minimum_stock_input)
        val taxRate = when (root.findViewById<RadioGroup>(R.id.product_tax_group).checkedRadioButtonId) {
            R.id.tax_exempt -> 0
            R.id.tax_reduced -> 5
            R.id.tax_general -> 19
            else -> null
        }

        when {
            name.isBlank() -> showError(R.string.enter_product_name)
            category.selectedItemPosition == 0 -> showError(R.string.select_product_category)
            description.isBlank() -> showError(R.string.enter_product_description)
            salePrice == null || salePrice <= 0 -> showError(R.string.enter_sale_price)
            preparationCost == null || preparationCost < 0 -> showError(R.string.enter_preparation_cost)
            taxRate == null -> showError(R.string.select_tax_rate)
            stock == null || stock < 0 -> showError(R.string.enter_initial_stock)
            minimumStock == null || minimumStock < 0 -> showError(R.string.enter_minimum_stock)
            stock != null && minimumStock != null && minimumStock > stock ->
                showError(R.string.minimum_stock_error)
            else -> {
                val product = ProductItem(
                    id = productToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                    name = name,
                    category = category.selectedItem.toString(),
                    description = description,
                    salePrice = salePrice!!,
                    preparationCost = preparationCost!!,
                    taxRate = taxRate,
                    initialStock = stock!!,
                    minimumStock = minimumStock!!,
                    imageRes = productToEdit?.imageRes,
                    imageUri = selectedImageUri?.toString() ?: productToEdit?.imageUri
                )
                val key = if (productToEdit == null) "created_product" else "updated_product"
                findNavController().previousBackStackEntry?.savedStateHandle?.set(key, product)
                Toast.makeText(
                    requireContext(),
                    if (productToEdit == null) R.string.product_created else R.string.product_updated,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun parseAmount(root: View, id: Int): Int? =
        root.findViewById<EditText>(id).text.toString().trim().toIntOrNull()

    private fun showError(message: Int) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun loadProduct(root: View, product: ProductItem) {
        root.findViewById<EditText>(R.id.product_name_input)
            .setText(product.name)

        root.findViewById<EditText>(R.id.product_description_input)
            .setText(product.description)

        root.findViewById<EditText>(R.id.product_sale_price_input)
            .setText(product.salePrice.toString())

        root.findViewById<EditText>(R.id.product_preparation_cost_input)
            .setText(product.preparationCost.toString())

        root.findViewById<EditText>(R.id.product_initial_stock_input)
            .setText(product.initialStock.toString())

        root.findViewById<EditText>(R.id.product_minimum_stock_input)
            .setText(product.minimumStock.toString())

        selectSpinner(root.findViewById(R.id.product_category_spinner), product.category)
        root.findViewById<RadioGroup>(R.id.product_tax_group).check(
            when (product.taxRate) {
                0 -> R.id.tax_exempt
                5 -> R.id.tax_reduced
                else -> R.id.tax_general
            }
        )

        val image = root.findViewById<ImageView>(
            R.id.product_form_image
        )

        when {
            product.imageUri != null -> {
                image.setImageURI(Uri.parse(product.imageUri))
                showProductImage(root)
            }

            product.imageRes != null -> {
                image.setImageResource(product.imageRes)
                showProductImage(root)
            }
        }

        root.findViewById<android.widget.TextView>(R.id.product_form_title)
            .setText(R.string.edit_product_title)

        root.findViewById<android.widget.TextView>(R.id.product_form_subtitle)
            .setText(R.string.edit_product_subtitle)

        root.findViewById<android.widget.TextView>(R.id.product_action_text)
            .setText(R.string.save_product_changes)

    }

    private fun selectSpinner(spinner: Spinner, value: String) {
        for (position in 0 until spinner.adapter.count) {
            if (spinner.adapter.getItem(position).toString() == value) {
                spinner.setSelection(position)
                return
            }
        }
    }

    private fun styleTaxOption(
        root: View,
        radioId: Int,
        title: String,
        description: String
    ) {
        val radio = root.findViewById<RadioButton>(radioId)

        val content = "$title\n$description"
        val spannable = SpannableString(content)

        // Título en negrita
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Descripción en gris
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            ),
            title.length + 1,
            content.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        radio.text = spannable
    }

}
