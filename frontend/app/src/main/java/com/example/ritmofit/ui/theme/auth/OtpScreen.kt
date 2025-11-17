// Archivo: OtpScreen.kt (MANTENIDO CON PEQUEÑOS AJUSTES)
package com.example.ritmofit.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight // Añadido para mejor UI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    // AÑADIDO: Recibimos la acción siguiente para el ViewModel
    nextAction: String,
    authViewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    // 1. Coleccionar los estados del ViewModel
    val otp by authViewModel.otp.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // 2. Efecto para navegar al éxito
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onVerificationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar OTP") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Verificación OTP", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) // Agregado título
            Spacer(modifier = Modifier.height(8.dp))
            Text("Se ha enviado un código a $email", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = otp,
                // onValueChange llama a setOtp en el ViewModel, actualizando el StateFlow
                onValueChange = { authViewModel.setOtp(it) },
                label = { Text("Código OTP (6 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                // CORREGIDO: Pasar 'nextAction' al ViewModel
                onClick = { authViewModel.confirmOtp(email, nextAction) },
                enabled = !isLoading && otp.length == 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verificar y Continuar")
                }
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                // Pequeño ajuste para mostrar el error en color
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}