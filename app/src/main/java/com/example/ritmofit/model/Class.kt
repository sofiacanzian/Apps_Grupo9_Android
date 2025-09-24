package com.example.ritmofit.model

import com.google.gson.annotations.SerializedName

data class Class(
    val id: String,
    val name: String,
    @SerializedName("sede")
    val location: String,
    val instructor: String,
    val duration: Int,
    val availableSpots: Int,
    val totalSpots: Int,
    val discipline: String,
    val datetime: String
)
