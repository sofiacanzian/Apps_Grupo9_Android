package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ritmofit.ui.theme.home.getMockClasses
import com.example.ritmofit.ui.theme.reservations.ReservationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classId: String,
    onNavigateBack: () -> Unit,
    onReservationSuccess: () -> Unit,
    reservationsViewModel: ReservationsViewModel
) {
    val gymClass = getMockClasses().find { it.id == classId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gymClass?.name ?: "Detalle de Clase") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        gymClass?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Horario: ${it.schedule.startTime} - ${it.schedule.endTime}")
                        Text("Duración: ${it.duration} minutos")
                        Text("Instructor: ${it.instructor.name}")
                        Text("Ubicación: ${it.location.name}")
                        Text("Dificultad: ${it.difficulty.displayName}")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        reservationsViewModel.createReservation(it)
                        onReservationSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = it.availableSpots > 0 && !reservationsViewModel.isBooking
                ) {
                    Text(if (reservationsViewModel.isBooking) "Reservando..." else "Reservar un cupo (${it.availableSpots} disponibles)")
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Clase no encontrada", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}