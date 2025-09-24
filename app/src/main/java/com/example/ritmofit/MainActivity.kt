// Archivo: MainActivity.kt (Corregido)
package com.example.ritmofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.ritmofit.utils.RitmoFitNavigation
import com.example.ritmofit.ui.theme.theme.RitmoFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RitmoFitApp()
        }
    }
}

@Composable
fun RitmoFitApp() {
    RitmoFitTheme {
        // La variable navController no se utiliza aquí, por lo que se puede eliminar.
        // val navController = rememberNavController()

        // Llama a la función RitmoFitNavigation
        RitmoFitNavigation()
    }
}