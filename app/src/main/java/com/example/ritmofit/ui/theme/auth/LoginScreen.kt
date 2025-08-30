package com.example.ritmofit.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.AuthState
import com.example.ritmofit.ui.auth.AuthViewModel
import com.example.ritmofit.ui.theme.theme.RitmoFitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState
    val email by viewModel.email
    val otpCode by viewModel.otpCode

    // Manejar estado de éxito
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo y título
            Text(
                text = "RitmoFit",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu gimnasio, tu ritmo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Contenido según estado
            when (authState) {
                is AuthState.Idle, is AuthState.Loading, is AuthState.Error -> {
                    EmailStep(
                        email = email,
                        onEmailChange = viewModel::updateEmail,
                        onSendOtp = viewModel::sendOtp,
                        isLoading = authState is AuthState.Loading,
                        error = if (authState is AuthState.Error) (authState as AuthState.Error).message else null,
                        onErrorDismiss = viewModel::clearError
                    )
                }

                is AuthState.OtpSent -> {
                    OtpStep(
                        email = (authState as AuthState.OtpSent).email,
                        otpCode = otpCode,
                        onOtpChange = viewModel::updateOtpCode,
                        onVerifyOtp = viewModel::verifyOtp,
                        onResendOtp = viewModel::resendOtp,
                        onBackToEmail = viewModel::backToEmail,
                        isLoading = false
                    )
                }

                is AuthState.Success -> {
                    // Este caso se maneja con LaunchedEffect arriba
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onErrorDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ingresa tu email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Te enviaremos un código de verificación",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendOtp,
            enabled = !isLoading && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Enviar código")
        }

        // Mostrar error si existe
        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun OtpStep(
    email: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onBackToEmail: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Código enviado",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa el código de 6 dígitos que enviamos a:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            label = { Text("Código de verificación") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtp,
            enabled = !isLoading && otpCode.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Verificar código")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onResendOtp) {
                Text("Reenviar código")
            }

            TextButton(onClick = onBackToEmail) {
                Text("Cambiar email")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "💡 Código de prueba: 123456",
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    RitmoFitTheme {
        LoginScreen(onLoginSuccess = {})
    }
}