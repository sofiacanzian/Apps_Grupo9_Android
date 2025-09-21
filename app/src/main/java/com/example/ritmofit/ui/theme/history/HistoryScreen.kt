// Archivo: HistoryScreen.kt (Corregido)
package com.example.ritmofit.ui.theme.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.home.GymClassCard
import com.example.ritmofit.ui.theme.history.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val historyState by historyViewModel.reservationsState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        historyViewModel.fetchUserReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Reservas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = historyState) {
                is HistoryViewModel.ReservationsUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HistoryViewModel.ReservationsUiState.Success -> {
                    if (state.reservations.isEmpty()) {
                        Text(text = "No tienes reservas en tu historial.")
                    } else {
                        LazyColumn {
                            items(state.reservations) { reservation ->
                                GymClassCard(
                                    gymClass = reservation.gymClass,
                                    onClassClick = { onClassClick(reservation.gymClass) }
                                )
                            }
                        }
                    }
                }
                is HistoryViewModel.ReservationsUiState.Error -> {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }
}