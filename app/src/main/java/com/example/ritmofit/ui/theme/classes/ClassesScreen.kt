// Archivo: ClassesScreen.kt (Versión Final)
package com.example.ritmofit.ui.theme.classes

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    classesViewModel: ClassesViewModel = viewModel()
) {
    // Observar el estado de las clases desde el ClassesViewModel
    val classesState by classesViewModel.classesState.collectAsState()

    // Llamar a la función para cargar las clases cuando el Composable se inicie
    LaunchedEffect(Unit) {
        classesViewModel.fetchClasses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = classesState) {
                is ClassesViewModel.ClassesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ClassesViewModel.ClassesUiState.Success -> {
                    if (state.classes.isEmpty()) {
                        Text(text = "No hay clases disponibles.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
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
                is ClassesViewModel.ClassesUiState.Error -> {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }
}