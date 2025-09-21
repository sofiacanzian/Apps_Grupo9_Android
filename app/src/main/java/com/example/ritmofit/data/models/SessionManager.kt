// Archivo: SessionManager.kt
package com.example.ritmofit.data.models

object SessionManager {
    var userId: String? = null
    val isLoggedIn: Boolean
        get() = userId != null
}