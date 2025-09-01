package com.example.ritmofit.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Ingresa a RitmoFit",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.showOtpField) {
                OutlinedTextField(
                    value = viewModel.otp,
                    onValueChange = viewModel::updateOtp,
                    label = { Text("Código OTP") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (viewModel.showOtpField) {
                            viewModel.verifyOtp()
                        } else {
                            viewModel.sendOtp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.email.isNotBlank() && (!viewModel.showOtpField || viewModel.otp.isNotBlank())
                ) {
                    Text(if (viewModel.showOtpField) "Verificar" else "Enviar Código")
                }
            }
        }
    }
}