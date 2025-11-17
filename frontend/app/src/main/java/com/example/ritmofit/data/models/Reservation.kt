package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Reservation(
    val id: String,
    val classId: GymClass?,
    val userId: String,
    val reservationDate: String,
    val classDate: String,
    val status: String
)