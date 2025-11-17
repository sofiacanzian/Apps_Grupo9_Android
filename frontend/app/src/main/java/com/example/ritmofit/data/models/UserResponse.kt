// Archivo: UserResponse.kt (COMPLETO)
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// Modelo que mapea el objeto 'user'
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,
    val email: String,
    val name: String? = null,
    val lastName: String? = null,
    val memberId: String? = null,
    val birthDate: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profilePhotoUrl: String? = null
)

@Serializable
data class UserResponse(
    // CRÍTICO: El token es opcional, ya que puede fallar el login/registro
    val token: String? = null,
    val user: UserProfile,
    val message: String? = null
)