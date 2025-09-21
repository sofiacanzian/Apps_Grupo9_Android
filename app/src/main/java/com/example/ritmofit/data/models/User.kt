// Archivo: app/src/main/java/com/example/ritmofit/data/models/User.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val profilePhotoUrl: String? = null
)