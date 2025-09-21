// Archivo: app/src/main/java/com/example/ritmofit/data/models/Location.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: String,
    val name: String,
    val address: String
)