// Archivo: HomeScreen.kt (Corregido)
package com.example.ritmofit.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.ui.theme.classes.ClassesScreen
import com.example.ritmofit.ui.theme.classes.ClassesViewModel

@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    classesViewModel: ClassesViewModel = viewModel()
) {
    ClassesScreen(
        onClassClick = onClassClick
    )
}