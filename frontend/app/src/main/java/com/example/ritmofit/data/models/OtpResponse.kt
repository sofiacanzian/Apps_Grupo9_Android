// Archivo: OtpResponse.kt
package com.example.ritmofit.data.models

import com.google.gson.annotations.SerializedName

data class OtpResponse(
    @SerializedName("message")
    val message: String
)