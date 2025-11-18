package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReservationRequest(val userId: String, val gymClassId: String)