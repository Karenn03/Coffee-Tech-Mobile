package com.example.coffeetechmobile.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.coffeetechmobile.R
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Primera fila
        bindProduct(
            root = view,
            containerId = R.id.product_iced_caramel,
            imageRes = R.drawable.img_iced_caramel_latte,
            name = "Iced Caramel Latte",
            description = "Café espresso mezclado con leche fría y un toque de caramelo, servido sobre hielo. Dulce, cremoso y refrescante.",
            price = 7000
        )

        bindProduct(
            root = view,
            containerId = R.id.product_frappe_oreo,
            imageRes = R.drawable.img_frappe_oreo,
            name = "Frappé Oreo",
            description = "Una deliciosa combinación de espresso, chocolate caliente y leche vaporizada, coronada con crema batida. Perfecta para los amantes del chocolate y el café.",
            price = 8000
        )

        // Segunda fila
        bindProduct(
            root = view,
            containerId = R.id.product_muffin_red_velvet,
            imageRes = R.drawable.img_muffin_red_velvet,
            name = "Muffin de Red Velvet",
            description = "Muffin suave y esponjoso, con el clásico color rojo y un toque de sabor a chocolate, coronado con un glaseado de queso crema.",
            price = 5000
        )

        bindProduct(
            root = view,
            containerId = R.id.product_iced_macchiato,
            imageRes = R.drawable.img_iced_macchiato,
            name = "Iced Macchiato",
            description = "Espresso manchado con leche fría y servido con hielo. Una opción ligera y refrescante para los amantes del café.",
            price = 6500
        )
    }

    private fun bindProduct(
        root: View,
        containerId: Int,
        imageRes: Int,
        name: String,
        description: String,
        price: Int
    ) {
        val productCard = root.findViewById<View>(containerId)

        productCard.findViewById<ImageView>(R.id.product_image).apply {
            setImageResource(imageRes)
            contentDescription = name
        }

        productCard.findViewById<TextView>(R.id.product_name).text = name

        productCard.findViewById<TextView>(R.id.product_description).text =
            description

        val formattedPrice = NumberFormat
            .getIntegerInstance(Locale("es", "CO"))
            .format(price)

        productCard.findViewById<TextView>(R.id.product_price).text =
            "\$$formattedPrice"
    }
}
