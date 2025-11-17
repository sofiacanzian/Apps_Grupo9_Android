package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class OtpConfirmationRequest(val email: String, val otp: String)