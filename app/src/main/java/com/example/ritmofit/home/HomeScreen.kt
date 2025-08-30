package com.example.ritmofit.ui.theme.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onClassClick: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Bienvenido a RitmoFit!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onNavigateToProfile) {
                Text("Ver Perfil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToReservations) {
                Text("Mis Reservas")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToQrScanner) {
                Text("Escanear QR")
            }
        }
    }
}