package com.example.ritmofit.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState()
    val loginResult by authViewModel.loginResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifica tu email") },
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
            Text("Ingresa el código que enviamos a $email")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Código de acceso") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.confirmOtp(email, otpCode) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmar")
                }
            }
        }
    }
}