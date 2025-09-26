// Archivo: ClassDetailScreen.kt (Corregido)
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
    classDetailViewModel: ClassDetailViewModel = viewModel(factory = ClassDetailViewModel.Factory),
    reservationsViewModel: ReservationsViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val gymClass by classDetailViewModel.classState.collectAsState()
    val isBooking by reservationsViewModel.isBooking.collectAsState()

    LaunchedEffect(key1 = classId) {
        classDetailViewModel.fetchClassDetails(classId)
    }

    // El error 'Unresolved reference 'classDate'' se resuelve si agregas el campo a GymClass.

    // Formato de fecha para detalle (dd/MM/yyyy)
    val dateFormatter = remember {
        // Usamos el locale "es" para el formato deseado
        // Importante: No es necesario usar Locale("es", "ES") en DateTimeFormatter
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    }

    // Lógica para parsear y formatear la fecha real
    val classDateText = remember(gymClass?.classDate) {
        val isoDate = gymClass?.classDate // <-- Ahora 'classDate' es reconocido
        if (isoDate != null) {
            try {
                // El backend devuelve YYYY-MM-DD. Parseamos y formateamos con ThreetenABP.
                val date = LocalDate.parse(isoDate)
                "Fecha: ${date.format(dateFormatter)}"
            } catch (e: Exception) {
                // Fallback a solo mostrar el día de la semana si la fecha no parsea
                "Día: ${gymClass?.schedule?.day?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/D"}"
            }
        } else {
            // Si el campo classDate es null, mostramos el día
            "Día: ${gymClass?.schedule?.day?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/D"}"
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
                text = gymClass!!.className,
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
                    // Muestra la fecha real calculada (dd/MM/yyyy) o el día
                    Text(classDateText, fontWeight = FontWeight.SemiBold)

                    Text("Horario: ${gymClass!!.schedule.startTime} - ${gymClass!!.schedule.endTime}")
                    Text("Ubicación: ${gymClass!!.location.name}")
                    Text("Cupos: ${gymClass!!.currentCapacity} / ${gymClass!!.maxCapacity}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // 1. Crear la reserva
                    reservationsViewModel.createReservation(gymClass!!._id)

                    // 2. Usar la función del ViewModel para actualizar el cupo localmente.
                    classDetailViewModel.incrementCapacity()

                    // 3. Notificar éxito (esto suele disparar una navegación o un toast/snackbar)
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