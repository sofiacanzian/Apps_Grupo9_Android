package com.example.ritmofit.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FitnessGoal(
    val id: String,
    val userId: String,
    val title: String,
    val targetCount: Int,
    val periodType: GoalPeriodType,
    val discipline: String? = null,
    val currentProgress: Int = 0,
    val remaining: Int = 0,
    val status: GoalStatus = GoalStatus.IN_PROGRESS,
    val progressPercentage: Double = 0.0,
    val periodLabel: String = "",
    val reminderThreshold: Int = 1,
    val updatedAt: String? = null
)

@Serializable
enum class GoalPeriodType {
    @SerialName("WEEK")
    WEEK,

    @SerialName("MONTH")
    MONTH
}

@Serializable
enum class GoalStatus {
    @SerialName("IN_PROGRESS")
    IN_PROGRESS,

    @SerialName("NEAR_COMPLETION")
    NEAR_COMPLETION,

    @SerialName("COMPLETED")
    COMPLETED
}
