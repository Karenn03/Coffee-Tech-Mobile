package com.example.coffeetechmobile.ui.manager.products

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.repository.ProductInput
import com.example.coffeetechmobile.data.storage.ProductImageStorage
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation
import java.io.File

/**
 * Formulario de creación y edición de productos
 *
 * Mantiene los campos del diseño original, agrega la selección de imagen
 * desde galería o cámara y persiste el producto en SQLite
 */
class CreateProductFragment : Fragment(R.layout.fragment_create_product) {

    private val formViewModel: ProductFormViewModel by viewModels()

    private var productToEdit: ProductItem? = null
    private var selectedImagePath: String? = null
    private var pendingCameraPath: String? = null
    private var isSaving = false

    private val galleryPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult

        val path = ProductImageStorage(requireContext())
            .copyFromUri(uri)

        if (path == null) {
            Toast.makeText(
                requireContext(),
                R.string.product_image_load_error,
                Toast.LENGTH_SHORT
            ).show()
            return@registerForActivityResult
        }

        selectedImagePath = path
        showImageFromPath(path)
    }

    private val cameraPicker = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingCameraPath
        pendingCameraPath = null

        if (success && path != null) {
            selectedImagePath = path
            showImageFromPath(path)
        } else {
            path?.let {
                ProductImageStorage(requireContext()).delete(it)
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // El menú inferior ya existe en el diseño; se activa también aquí
        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.product_form_bottom_navigation
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.PRODUCTS
        )

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

        productToEdit = arguments
            ?.getSerializable("product_to_edit") as? ProductItem

        productToEdit?.let { loadProduct(view, it) }

        view.findViewById<View>(R.id.product_form_image_picker)
            .setOnClickListener {
                showImageSourceDialog()
            }

        view.findViewById<View>(R.id.product_back_button)
            .setOnClickListener {
                findNavController().navigateUp()
            }

        view.findViewById<View>(R.id.cancel_product_button)
            .setOnClickListener {
                findNavController().navigateUp()
            }

        view.findViewById<View>(R.id.save_product_button)
            .setOnClickListener {
                saveProduct(view)
            }

        observeFormState()
    }

    // Ofrece elegir una imagen desde la galería o tomar una foto
    private fun showImageSourceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.product_image_source_title)
            .setItems(
                arrayOf(
                    getString(R.string.product_image_source_gallery),
                    getString(R.string.product_image_source_camera)
                )
            ) { _, which ->
                when (which) {
                    0 -> galleryPicker.launch("image/*")
                    1 -> launchCamera()
                }
            }
            .show()
    }

    private fun launchCamera() {
        val storage = ProductImageStorage(requireContext())

        try {
            val file = storage.createCameraFile()
            pendingCameraPath = file.absolutePath

            cameraPicker.launch(storage.createCameraUri(file))
        } catch (exception: Exception) {
            pendingCameraPath?.let { storage.delete(it) }
            pendingCameraPath = null

            Toast.makeText(
                requireContext(),
                R.string.product_image_camera_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showImageFromPath(path: String) {
        view?.let { root ->
            root.findViewById<ImageView>(R.id.product_form_image)
                .setImageURI(Uri.fromFile(File(path)))

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

    private fun saveProduct(root: View) {
        if (isSaving) return

        val name = root.findViewById<EditText>(
            R.id.product_name_input
        ).text.toString().trim()

        val description = root.findViewById<EditText>(
            R.id.product_description_input
        ).text.toString().trim()

        val category = root.findViewById<Spinner>(
            R.id.product_category_spinner
        )

        val salePrice = parseAmount(root, R.id.product_sale_price_input)
        val preparationCost = parseAmount(
            root,
            R.id.product_preparation_cost_input
        )
        val stock = parseAmount(root, R.id.product_initial_stock_input)
        val minimumStock = parseAmount(
            root,
            R.id.product_minimum_stock_input
        )

        val taxRate = when (
            root.findViewById<RadioGroup>(
                R.id.product_tax_group
            ).checkedRadioButtonId
        ) {
            R.id.tax_exempt -> 0
            R.id.tax_reduced -> 5
            R.id.tax_general -> 19
            else -> null
        }

        when {
            name.isBlank() -> showError(R.string.enter_product_name)

            category.selectedItemPosition == 0 ->
                showError(R.string.select_product_category)

            description.isBlank() ->
                showError(R.string.enter_product_description)

            salePrice == null || salePrice <= 0 ->
                showError(R.string.enter_sale_price)

            preparationCost == null || preparationCost < 0 ->
                showError(R.string.enter_preparation_cost)

            taxRate == null -> showError(R.string.select_tax_rate)

            stock == null || stock < 0 ->
                showError(R.string.enter_initial_stock)

            minimumStock == null || minimumStock < 0 ->
                showError(R.string.enter_minimum_stock)

            minimumStock > stock ->
                showError(R.string.minimum_stock_error)

            else -> {
                val imagePath = selectedImagePath

                // Si se conserva la imagen semilla se guarda su drawable.
                val imageResName = if (imagePath.isNullOrBlank()) {
                    productToEdit?.imageResName
                } else {
                    null
                }

                val input = ProductInput(
                    name = name,
                    category = category.selectedItem.toString(),
                    description = description,
                    salePrice = salePrice,
                    preparationCost = preparationCost,
                    taxRate = taxRate,
                    stock = stock,
                    minimumStock = minimumStock,
                    imagePath = imagePath,
                    imageResName = imageResName
                )

                isSaving = true

                if (productToEdit == null) {
                    formViewModel.createProduct(input)
                } else {
                    formViewModel.updateProduct(
                        productToEdit!!.productId,
                        input
                    )
                }
            }
        }
    }

    private fun observeFormState() {
        formViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ProductFormState.Idle -> Unit

                ProductFormState.Loading -> setLoading(true)

                is ProductFormState.Success -> {
                    setLoading(false)
                    isSaving = false
                    formViewModel.consumeState()

                    Toast.makeText(
                        requireContext(),
                        if (productToEdit == null) {
                            R.string.product_created
                        } else {
                            R.string.product_updated
                        },
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigateUp()
                }

                is ProductFormState.Error -> {
                    setLoading(false)
                    isSaving = false

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()

                    formViewModel.consumeState()
                }
            }
        }
    }

    // Carga los datos del producto que se va a editar
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

        selectSpinner(
            root.findViewById(R.id.product_category_spinner),
            product.category
        )

        root.findViewById<RadioGroup>(R.id.product_tax_group).check(
            when (product.taxRate) {
                0 -> R.id.tax_exempt
                5 -> R.id.tax_reduced
                else -> R.id.tax_general
            }
        )

        selectedImagePath = product.imagePath

        val image = root.findViewById<ImageView>(
            R.id.product_form_image
        )

        when {
            !product.imagePath.isNullOrBlank() -> {
                image.setImageURI(
                    Uri.fromFile(File(product.imagePath))
                )
                showProductImage(root)
            }

            product.imageRes != null -> {
                image.setImageResource(product.imageRes)
                showProductImage(root)
            }
        }

        root.findViewById<TextView>(R.id.product_form_title)
            .setText(R.string.edit_product_title)

        root.findViewById<TextView>(R.id.product_form_subtitle)
            .setText(R.string.edit_product_subtitle)

        root.findViewById<TextView>(R.id.product_action_text)
            .setText(R.string.save_product_changes)

        // En edición el botón debe decir "Cancelar", no "Cancelar registro".
        root.findViewById<TextView>(R.id.cancel_product_text)
            .setText(R.string.cancel)
    }

    private fun parseAmount(root: View, id: Int): Int? {
        return root.findViewById<EditText>(id)
            .text.toString().trim().toIntOrNull()
    }

    private fun showError(message: Int) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
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

    // Bloquea los botones mientras se escribe en SQLite
    private fun setLoading(loading: Boolean) {
        val saveButton = requireView()
            .findViewById<View>(R.id.save_product_button)

        val cancelButton = requireView()
            .findViewById<View>(R.id.cancel_product_button)

        saveButton.isEnabled = !loading
        saveButton.alpha = if (loading) 0.7f else 1f

        cancelButton.isEnabled = !loading
    }
}
