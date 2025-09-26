// Archivo: com/example/ritmofit/network/FilterResponse.kt
package com.example.ritmofit.network

import kotlinx.serialization.Serializable

@Serializable
data class FilterResponse(
    val locations: List<String>,
    val disciplines: List<String>
)