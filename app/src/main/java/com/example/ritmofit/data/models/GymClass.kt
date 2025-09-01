// Archivo: GymClass.kt
package com.example.ritmofit.data.models

/**
 * Modelo para las clases del gimnasio
 */
data class GymClass(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val instructor: Instructor = Instructor(),
    val location: GymLocation = GymLocation(),
    val schedule: ClassSchedule = ClassSchedule(),
    val capacity: Int = 0,
    val availableSpots: Int = 0,
    val duration: Int = 60, // duración en minutos
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val imageUrl: String? = null
)

/**
 * Instructor de la clase
 */
data class Instructor(
    val id: String = "",
    val name: String = "",
    val profilePicture: String? = null,
    val specialties: List<String> = emptyList(),
    val rating: Float = 0f
)

/**
 * Sede/ubicación del gimnasio
 */
data class GymLocation(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = ""
)

/**
 * Horario de la clase
 */
data class ClassSchedule(
    val dayOfWeek: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val date: String = ""
)

/**
 * Niveles de dificultad
 */
enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Principiante"),
    INTERMEDIATE("Intermedio"),
    ADVANCED("Avanzado")
}

/**
 * Filtros para buscar clases
 */
data class ClassFilters(
    val locationId: String? = null,
    val discipline: String? = null,
    val date: String? = null,
    val timeRange: TimeRange? = null
)

/**
 * Rango de tiempo para filtros
 */
data class TimeRange(
    val startTime: String,
    val endTime: String
)