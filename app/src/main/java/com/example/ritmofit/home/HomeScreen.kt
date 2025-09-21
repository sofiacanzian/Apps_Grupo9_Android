// Archivo: HomeScreen.kt
package com.example.ritmofit.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ritmofit.home.HomeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.FitnessCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val classesState by homeViewModel.classesState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.fetchClasses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Disponibles") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* No action needed, already in Home */ },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Clases") },
                    label = { Text("Clases") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToReservations,
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Reservas") },
                    label = { Text("Reservas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToQrScanner,
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escaner QR") },
                    label = { Text("Escaner") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = classesState) {
                is HomeViewModel.ClassesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HomeViewModel.ClassesUiState.Success -> {
                    if (state.classes.isEmpty()) {
                        Text(text = "No hay clases disponibles en este momento.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.classes) { gymClass ->
                                GymClassCard(
                                    gymClass = gymClass,
                                    onClassClick = { onClassClick(gymClass) }
                                )
                            }
                        }
                    }
                }
                is HomeViewModel.ClassesUiState.Error -> {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymClassCard(
    gymClass: GymClass,
    onClassClick: (GymClass) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onClassClick(gymClass) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = gymClass.className,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Horario: ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}