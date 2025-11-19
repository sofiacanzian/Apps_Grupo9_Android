package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PreferredTimeRange(
    val label: String,
    val start: String,
    val end: String
)

@Serializable
data class TrainingPreferences(
    val favoriteDisciplines: List<String> = emptyList(),
    val preferredLocations: List<String> = emptyList(),
    val preferredTimeRanges: List<PreferredTimeRange> = emptyList()
) {
    fun isEmpty(): Boolean =
        favoriteDisciplines.isEmpty() && preferredLocations.isEmpty() && preferredTimeRanges.isEmpty()

    fun hasSelections(): Boolean = !isEmpty()

    companion object {
        val EMPTY = TrainingPreferences()
    }
}
