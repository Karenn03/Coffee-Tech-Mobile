package com.example.coffeetechmobile.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coffeetechmobile.data.model.UserRole

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
        ),
        UserRole.ADMIN to UserProfile(
            name = "Administrador Coffee Tech",
            email = "admin@coffeetech.com",
            phone = "300 000 0000",
            role = UserRole.ADMIN,
            document = "1000000000",
            location = "Sede principal"
        )
    )

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    fun loadProfile(role: UserRole) {
        // Si llega un rol sin datos de demostración, se usa el comprador
        // para evitar que la pantalla se cierre por una excepción
        _profile.value = profiles[role] ?: profiles.getValue(UserRole.BUYER)
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
