package com.example.ritmofit.network

import kotlinx.serialization.Serializable

@Serializable
data class FilterResponse(
    val locations: List<String>,
    val disciplines: List<String>
)