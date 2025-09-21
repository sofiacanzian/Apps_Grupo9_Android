package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val otp: String? = null
)