// Archivo: ClassesScreen.kt
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.Location
import com.example.ritmofit.data.models.Schedule

@Composable
fun ClassesScreen(
    onClassClick: (GymClass) -> Unit,
    classesViewModel: ClassesViewModel = viewModel()
) {
    val classes by classesViewModel.classes.collectAsState()

    if (classes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(classes) { gymClass ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClassClick(gymClass) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = gymClass.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Horario: ${gymClass.schedule.startTime}")
                        Text(text = "Profesor: ${gymClass.instructor}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClassesScreenPreview() {
    val mockClasses = listOf(
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
        )
    )
    ClassesScreen(
        onClassClick = {},
        classesViewModel = ClassesViewModel()
    )
}