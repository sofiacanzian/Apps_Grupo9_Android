package com.example.ritmofit.ui.theme.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.data.models.ReservationStatus
import com.example.ritmofit.ui.theme.home.getMockClasses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    val mockHistory = listOf(
        Reservation(
            id = "h1",
            userId = "u1",
            gymClass = getMockClasses()[2],
            status = ReservationStatus.COMPLETED,
            checkedInAt = System.currentTimeMillis()
        ),
        Reservation(
            id = "h2",
            userId = "u1",
            gymClass = getMockClasses()[3],
            status = ReservationStatus.COMPLETED,
            checkedInAt = System.currentTimeMillis()
        )
    )

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockHistory) { attendance ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(attendance.gymClass.name, style = MaterialTheme.typography.titleMedium)
                        Text("Sede: ${attendance.gymClass.location.name}")
                        Text("Fecha: ${attendance.gymClass.schedule.date}")
                    }
                }
            }
        }
    }
}