// Archivo: HistoryScreen.kt
package com.example.ritmofit.ui.theme.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.Location
import com.example.ritmofit.data.models.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Asistencias") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Aquí debes usar el ViewModel para obtener los datos reales.
        // Por ahora, usamos datos de prueba para que compile.
        val mockHistory = listOf(
            GymClass(
                id = "clase1",
                name = "Yoga Matutino",
                description = "Clase de yoga para empezar el día con energía.",
                schedule = Schedule(id = "sch1", startTime = "09:00", endTime = "10:00", day = "Lunes", gymClassId = "clase1"),
                duration = 60,
                instructor = "Laura",
                location = Location(id = "loc1", name = "Salón de Yoga"),
                difficulty = "Baja",
                availableSpots = 5,
                imageUrl = "https://example.com/yoga.jpg",
                maxCapacity = 15
            ),
            GymClass(
                id = "clase2",
                name = "Cardio Intenso",
                description = "Entrenamiento cardiovascular de alta intensidad.",
                schedule = Schedule(id = "sch2", startTime = "18:00", endTime = "18:45", day = "Miércoles", gymClassId = "clase2"),
                duration = 45,
                instructor = "Carlos",
                location = Location(id = "loc2", name = "Salón Principal"),
                difficulty = "Alta",
                availableSpots = 10,
                imageUrl = "https://example.com/cardio.jpg",
                maxCapacity = 20
            )
        )
        if (mockHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes historial de asistencias.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(mockHistory) { gymClass ->
                    Text(text = gymClass.name)
                }
            }
        }
    }
}