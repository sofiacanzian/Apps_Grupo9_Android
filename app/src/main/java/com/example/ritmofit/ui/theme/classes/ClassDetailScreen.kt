// Archivo: ClassDetailScreen.kt
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.ui.theme.reservation.ReservationsViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ClassDetailScreen(
    classId: String,
    onReservationSuccess: () -> Unit,
    classDetailViewModel: ClassDetailViewModel = viewModel(),
    reservationsViewModel: ReservationsViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val gymClass by classDetailViewModel.classState.collectAsState()
    val isBooking by reservationsViewModel.isBooking.collectAsState()

    LaunchedEffect(key1 = classId) {
        classDetailViewModel.fetchClassDetails(classId)
    }

    // Formateador de fecha
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale("es", "ES"))
    }

    // Lógica para parsear la fecha real
    val classDateText = remember(gymClass?.classDate) {
        val isoDate = gymClass?.classDate
        if (isoDate != null) {
            try {
                // Parseamos el string YYYY-MM-DD y lo formateamos
                val date = LocalDate.parse(isoDate)
                "Fecha: ${date.format(dateFormatter)}"
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
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
                .padding(paddingValues)
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
                    // --- CAMBIO CLAVE: MOSTRAR LA FECHA REAL ---
                    if (classDateText != null) {
                        Text(classDateText, fontWeight = FontWeight.SemiBold)
                    }
                    // ------------------------------------------
                    Text("Horario: ${gymClass!!.schedule.startTime} - ${gymClass!!.schedule.endTime}")
                    Text("Ubicación: ${gymClass!!.location.name}")
                    Text("Cupos: ${gymClass!!.currentCapacity} / ${gymClass!!.maxCapacity}")
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