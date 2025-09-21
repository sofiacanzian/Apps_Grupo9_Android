// Archivo: app/src/main/java/com/example/ritmofit/data/models/Schedule.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val id: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val gymClassId: String
)