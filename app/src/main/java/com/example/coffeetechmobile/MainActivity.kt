package com.example.coffeetechmobile

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.example.coffeetechmobile.data.session.SessionManager
import com.example.coffeetechmobile.ui.navigation.RoleNavigator

/**
 * Activity única de la aplicación.
 *
 * Contiene el NavHostFragment y decide si debe restaurar una sesión guardada
 * antes de mostrar el login.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.nav_host_fragment)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // El NavHostFragment termina de configurar el grafo después de inflar
        // la vista; se espera al siguiente ciclo para poder navegar.
        findViewById<View>(R.id.nav_host_fragment).post {
            restoreSessionIfNeeded()
        }
    }

    /**
     * Si el usuario cerró la aplicación sin cerrar sesión, lo lleva
     * directamente al panel de su rol.
     */
    private fun restoreSessionIfNeeded() {
        val session = SessionManager(this).current() ?: return
        val navController = findNavController(R.id.nav_host_fragment)

        // Solo se restaura si el grafo aún está en el login.
        if (navController.currentDestination?.id == R.id.sign_in_fragment) {
            RoleNavigator.navigateToRoleHome(
                navController = navController,
                role = session.role
            )
        }
    }
}
