package com.example.coffeetechmobile.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val profiles = mutableMapOf(
        UserRole.BUYER to UserProfile(
            name = "Karen Sophia Molina Díaz",
            email = "cliente@coffeetech.com",
            phone = "300 123 4567",
            role = UserRole.BUYER,
            document = "1020304050",
            location = "Bogotá, Colombia"
        ),
        UserRole.SELLER to UserProfile(
            name = "Karen Sophia Molina Díaz",
            email = "vendedor@coffeetech.com",
            phone = "310 987 6543",
            role = UserRole.SELLER,
            document = "1098765432",
            location = "Sede Chapinero Alto"
        )
    )

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    fun loadProfile(role: UserRole) {
        _profile.value = profiles.getValue(role)
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String
    ): Boolean {
        val current = _profile.value ?: return false

        if (
            name.isBlank() ||
            email.isBlank() ||
            phone.isBlank()
        ) {
            return false
        }

        val updated = current.copy(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim()
        )

        profiles[current.role] = updated
        _profile.value = updated

        return true
    }
}
