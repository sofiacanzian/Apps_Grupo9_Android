// Archivo: ClassDetailScreen.kt
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.ui.theme.reservation.ReservationsViewModel

@Composable
fun ClassDetailScreen(
    classId: String,
    onReservationSuccess: () -> Unit,
    classDetailViewModel: ClassDetailViewModel = viewModel(),
    reservationsViewModel: ReservationsViewModel = viewModel(),
    paddingValues: PaddingValues // <-- Parámetro agregado
) {
    val gymClass by classDetailViewModel.classState.collectAsState()
    val isBooking by reservationsViewModel.isBooking.collectAsState()

    LaunchedEffect(key1 = classId) {
        classDetailViewModel.fetchClassDetails(classId)
    }

    if (gymClass == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // <-- Se aplica el padding aquí
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = gymClass!!.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
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
                    Text("Horario: ${gymClass!!.schedule.startTime}")
                    Text("Ubicación: ${gymClass!!.location.name}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    reservationsViewModel.createReservation(gymClass!!.id)
                    onReservationSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBooking
            ) {
                Text(if (isBooking) "Reservando..." else "Reservar un cupo")
            }
        }
    }
}