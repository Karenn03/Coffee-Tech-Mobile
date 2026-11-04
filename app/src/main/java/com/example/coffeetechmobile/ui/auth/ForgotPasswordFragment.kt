package com.example.coffeetechmobile.ui.auth

import androidx.fragment.app.Fragment
import com.example.coffeetechmobile.R
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.back_to_sign_in_container)
            .setOnClickListener {

                findNavController().popBackStack(
                    R.id.sign_in_fragment,
                    false
                )
            }
    }
}
