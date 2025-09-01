// Archivo: User.kt
package com.example.ritmofit.data.models

/**
 * Modelo de datos para el usuario de RitmoFit
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val birthDate: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val partnerNumber: String? = null,
    val profilePicture: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Request para autenticación con email
 */
data class AuthRequest(
    val email: String
)

/**
 * Request para verificar código OTP
 */
data class OtpRequest(
    val email: String,
    val code: String
)

/**
 * Response de autenticación exitosa
 */
data class AuthResponse(
    val user: User,
    val token: String,
    val success: Boolean = true,
    val message: String = ""
)

/**
 * Estados de autenticación
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data class OtpSent(val email: String) : AuthState()
}