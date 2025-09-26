// Archivo: LoginScreen.kt (VERSIÓN FINAL CORREGIDA)
package com.example.ritmofit.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// Definimos los estados de la interfaz de autenticación
sealed class AuthState {
    object Login : AuthState()
    object Register : AuthState()
    object RequestPasswordReset : AuthState()
    data class OtpVerification(val email: String, val nextAction: String) : AuthState()
    data class ResetPassword(val email: String) : AuthState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    var authState by remember { mutableStateOf<AuthState>(AuthState.Login) }

    // Estados de entrada para todos los flujos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    // Estados del ViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Estado del Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Control de navegación y mensajes basado en estado
    LaunchedEffect(successMessage, errorMessage) {
        // Manejo de mensajes de éxito
        successMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }

            when {
                // Registro/Login -> OTP Verification
                msg.contains("OTP enviado para registro") || msg.contains("OTP enviado para login") -> {
                    val nextAction = if (authState is AuthState.Register) "REGISTER" else "LOGIN"
                    authState = AuthState.OtpVerification(email, nextAction)
                    authViewModel.clearMessages()
                }
                // Recuperación -> OTP Verification
                msg.contains("OTP enviado para recuperación") -> {
                    authState = AuthState.OtpVerification(email, "PASSWORD_RESET")
                    authViewModel.clearMessages()
                }
                // OTP verificado para Recuperación -> Resetear Password
                msg.contains("OTP verificado para recuperación") -> {
                    authState = AuthState.ResetPassword(email)
                    authViewModel.clearMessages()
                }
                // Finalización (Sesión iniciada o Password cambiado)
                msg.contains("Sesión iniciada con éxito") || msg.contains("Usuario registrado con éxito") || msg.contains("Contraseña restablecida con éxito") -> {
                    authViewModel.clearMessages()
                    // La navegación a "home" es manejada por Navigation.kt con isAuthenticated
                }
            }
        }

        // Manejo de mensajes de error
        errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Cerrar",
                    duration = SnackbarDuration.Long
                )
            }
            authViewModel.clearMessages()
        }
    }

    // Usamos Scaffold para alojar el SnackbarHost
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        // Lógica para mostrar la pantalla correcta
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = authState) {
                AuthState.Login -> {
                    AuthForm(
                        title = "Iniciar Sesión",
                        email = email, onEmailChange = { email = it },
                        password = password, onPasswordChange = { password = it },
                        isLoading = isLoading,
                        buttonText = "Solicitar Acceso (Enviar OTP)",
                        onSubmit = { authViewModel.loginAndSendOtp(email, password) }
                    ) {
                        TextButton(onClick = { authState = AuthState.Register }) {
                            Text("¿No tienes cuenta? Regístrate")
                        }
                        TextButton(onClick = { authState = AuthState.RequestPasswordReset }) {
                            Text("¿Olvidaste tu contraseña?")
                        }
                    }
                }

                AuthState.Register -> {
                    RegisterForm(
                        email = email, onEmailChange = { email = it },
                        password = password, onPasswordChange = { password = it },
                        confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it },
                        isLoading = isLoading,
                        onSubmit = {
                            if (password == confirmPassword) {
                                authViewModel.registerAndSendOtp(email, password)
                            } else {
                                authViewModel.setErrorMessage("Las contraseñas no coinciden.")
                            }
                        }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("¿Ya tienes cuenta? Inicia Sesión")
                        }
                    }
                }

                AuthState.RequestPasswordReset -> {
                    RequestResetForm(
                        email = email, onEmailChange = { email = it },
                        isLoading = isLoading,
                        onSubmit = { authViewModel.requestPasswordResetOtp(email) }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("Volver al inicio de sesión")
                        }
                    }
                }

                is AuthState.OtpVerification -> {
                    OtpVerificationForm(
                        email = state.email,
                        otpCode = otpCode, onOtpChange = {
                            otpCode = it // Actualizar el estado local para el TextField
                            authViewModel.setOtp(it) // Actualizar el StateFlow en el ViewModel
                        },
                        isLoading = isLoading,
                        onSubmit = {
                            authViewModel.confirmOtp(state.email, state.nextAction)
                        },
                        onRequestNewOtp = {
                            when (state.nextAction) {
                                "REGISTER" -> authViewModel.registerAndSendOtp(state.email, password)
                                "LOGIN" -> authViewModel.loginAndSendOtp(state.email, password)
                                "PASSWORD_RESET" -> authViewModel.requestPasswordResetOtp(state.email)
                            }
                        }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("Cancelar y volver")
                        }
                    }
                }

                is AuthState.ResetPassword -> {
                    ResetPasswordForm(
                        email = state.email,
                        password = password, onPasswordChange = { password = it },
                        confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it },
                        isLoading = isLoading,
                        onSubmit = {
                            if (password == confirmPassword) {
                                authViewModel.resetPassword(state.email, password)
                            } else {
                                authViewModel.setErrorMessage("Las contraseñas no coinciden.")
                            }
                        }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Componentes Composable Reutilizables (CORREGIDOS)
// ----------------------------------------------------

@Composable
fun AuthForm(
    title: String, email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    isLoading: Boolean, buttonText: String, onSubmit: () -> Unit,
    footer: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange, // CORREGIDO
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange, // CORREGIDO
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(buttonText)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
    }
}

@Composable
fun RegisterForm(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit,
    footer: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Usuario", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange, // CORREGIDO
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange, // CORREGIDO
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange, // CORREGIDO
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Registrarse y Enviar OTP")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
    }
}

@Composable
fun RequestResetForm(
    email: String, onEmailChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit,
    footer: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange, // CORREGIDO
            label = { Text("Email para recuperación") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && email.isNotBlank(), modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Enviar Código de Recuperación (OTP)")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
    }
}

@Composable
fun OtpVerificationForm(
    email: String, otpCode: String, onOtpChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit, onRequestNewOtp: () -> Unit,
    footer: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verificación OTP", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Ingresa el código enviado a $email", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = otpCode,
            onValueChange = onOtpChange, // CORREGIDO
            label = { Text("Código OTP") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && otpCode.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Verificar y Continuar")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRequestNewOtp) {
            Text("Reenviar Código")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
    }
}

@Composable
fun ResetPasswordForm(
    email: String, password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit,
    footer: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Restablecer Contraseña", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Para: $email", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange, // CORREGIDO
            label = { Text("Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange, // CORREGIDO
            label = { Text("Confirmar Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Cambiar Contraseña")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { footer() }
    }
}