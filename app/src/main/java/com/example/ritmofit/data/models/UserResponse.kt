// Archivo: app/src/main/java/com/example/ritmofit/data/models/UserResponse.kt
package com.example.ritmofit.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerializedName("user")
    val user: User
)