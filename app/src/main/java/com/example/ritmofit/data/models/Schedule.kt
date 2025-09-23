package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val day: String,
    val startTime: String,
    val endTime: String
)