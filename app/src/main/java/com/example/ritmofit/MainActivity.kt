package com.example.ritmofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.ritmofit.utils.RitmoFitNavigation
import com.example.ritmofit.ui.theme.theme.RitmoFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RitmoFitTheme {
                // Usar el sistema de navegación
                val navController = rememberNavController()
                RitmoFitNavigation(navController = navController)
            }
        }
    }
}