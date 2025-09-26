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
    // El callback es importante para que la vista anterior se actualice
    onReservationSuccess: () -> Unit,
    classDetailViewModel: ClassDetailViewModel = viewModel(factory = ClassDetailViewModel.Factory), // Asegúrate de usar el factory aquí
    reservationsViewModel: ReservationsViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val gymClass by classDetailViewModel.classState.collectAsState()
    val isBooking by reservationsViewModel.isBooking.collectAsState()

    LaunchedEffect(key1 = classId) {
        classDetailViewModel.fetchClassDetails(classId)
    }

    // CAMBIO CLAVE: Formato de fecha para detalle (dd/MM/yyyy)
    val dateFormatter = remember {
        // Usamos "dd/MM/yyyy" para el formato deseado
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    }

    // Lógica para parsear y formatear la fecha real
    val classDateText = remember(gymClass?.classDate) {
        val isoDate = gymClass?.classDate
        if (isoDate != null) {
            try {
                // El backend devuelve YYYY-MM-DD. Parseamos y formateamos.
                val date = LocalDate.parse(isoDate)
                "Fecha: ${date.format(dateFormatter)}"
            } catch (e: Exception) {
                // En caso de error de parseo, mostramos el día de la semana original como fallback
                "Fecha: ${gymClass?.schedule?.day?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/D"}"
            }
        } else {
            "Fecha: N/D"
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
                    // Muestra la fecha real calculada (dd/MM/yyyy)
                    Text(classDateText, fontWeight = FontWeight.SemiBold)

                    Text("Horario: ${gymClass!!.schedule.startTime} - ${gymClass!!.schedule.endTime}")
                    Text("Ubicación: ${gymClass!!.location.name}")

                    // Aseguramos que la capacidad refleje el estado actual de gymClass
                    Text("Cupos: ${gymClass!!.currentCapacity} / ${gymClass!!.maxCapacity}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // 1. Crear la reserva
                    reservationsViewModel.createReservation(gymClass!!.id)

                    // 2. 🚀 LLAMADA CORREGIDA: Usamos la función del ViewModel para actualizar el cupo.
                    classDetailViewModel.incrementCapacity()

                    // 3. Notificar éxito
                    onReservationSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                // Se deshabilita si está reservando o no hay cupos
                enabled = !isBooking && gymClass!!.currentCapacity < gymClass!!.maxCapacity
            ) {
                Text(
                    when {
                        isBooking -> "Reservando..."
                        gymClass!!.currentCapacity >= gymClass!!.maxCapacity -> "Sin cupos disponibles"
                        else -> "Reservar un cupo"
                    }
                )
            }
        }
    }
}