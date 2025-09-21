package com.example.ritmofit.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String
)