// Archivo: SessionManager.kt (ACTUALIZADO)
package com.example.ritmofit.data.models

// Nota: Puedes eliminar el import de mutableStateOf si no lo estás usando para estados globales.
// import androidx.compose.runtime.mutableStateOf

object SessionManager {
    // Almacenamiento del ID del usuario
    var userId: String? = null
        set(value) {
            field = value
            // (Aquí va la lógica para almacenar en SharedPreferences/DataStore)
        }

    // 🚀 Campo para el token de sesión (JWT)
    var token: String? = null
        set(value) {
            field = value
            // (Aquí va la lógica para almacenar en SharedPreferences/DataStore)
        }

    // Estado derivado para saber si el usuario está logueado
    val isLoggedIn: Boolean
        get() = userId != null && token != null
}