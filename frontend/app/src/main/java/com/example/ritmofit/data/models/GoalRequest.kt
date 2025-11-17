package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GoalRequest(
    val title: String,
    val targetCount: Int,
    val periodType: GoalPeriodType,
    val discipline: String? = null,
    val reminderThreshold: Int = 1
)
