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
    // Añadimos 'email' y 'nextAction' para la acción de verificación final
    data class OtpVerification(val email: String, val nextAction: String) : AuthState()
    data class ResetPassword(val email: String) : AuthState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    // ----------------------------------------------------------------------
    // CAMBIO CLAVE: Se elimina 'onLoginSuccess: () -> Unit'
    // La navegación ahora es manejada por Navigation.kt observando
    // authViewModel.isAuthenticated (SessionManager.isLoggedIn).
    // ----------------------------------------------------------------------
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

    // Sincronizar el estado local del OTP con el ViewModel
    LaunchedEffect(otpCode) {
        authViewModel.setOtp(otpCode)
    }

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
                // Contraseña restablecida
                msg.contains("Contraseña restablecida con éxito") -> {
                    authState = AuthState.Login // Vuelve al login tras restablecer
                    authViewModel.clearMessages()
                }
                // Verificación de login exitosa: El Navigation.kt se encargará de la navegación
                msg.contains("Sesión iniciada con éxito") || msg.contains("Usuario registrado con éxito") -> {
                    // El ViewModel ya guardó el token. NO se necesita llamar a onLoginSuccess() aquí.
                    // El Navigation.kt verá el cambio en authViewModel.isAuthenticated y navegará.
                    // Solo limpiamos el mensaje para que no se muestre doble.
                    authViewModel.clearMessages()
                }
                // Manejo de otros éxitos si es necesario
                else -> authViewModel.clearMessages()
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
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = authState) {
                AuthState.Login -> {
                    AuthForm(
                        title = "Iniciar Sesión",
                        email = email, onEmailChange = { email = it },
                        password = password, onPasswordChange = { password = it },
                        isLoading = isLoading,
                        buttonText = "Solicitar Acceso (Enviar OTP)",
                        onSubmit = {
                            authViewModel.loginAndSendOtp(email, password) {
                                // El éxito de esta función solo lleva a la siguiente pantalla
                                authState = AuthState.OtpVerification(email, "LOGIN")
                            }
                        }
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
                                authViewModel.registerAndSendOtp(email, password) {
                                    // El éxito de esta función solo lleva a la siguiente pantalla
                                    authState = AuthState.OtpVerification(email, "REGISTER")
                                }
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
                        onSubmit = {
                            authViewModel.requestPasswordResetOtp(email) {
                                authState = AuthState.ResetPassword(email)
                                authViewModel.clearMessages()
                            }
                        }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("Volver al inicio de sesión")
                        }
                    }
                }

                is AuthState.OtpVerification -> {
                    OtpVerificationForm(
                        email = state.email,
                        otpCode = otpCode, onOtpChange = { otpCode = it },
                        isLoading = isLoading,
                        onSubmit = {
                            // La lógica de verificación y guardado de token está en el ViewModel
                            authViewModel.confirmOtp(state.email, state.nextAction)
                        },
                        onRequestNewOtp = {
                            when (state.nextAction) {
                                // Asegúrate de que, al reenviar, la contraseña original sea la correcta (si es Login o Register).
                                "REGISTER" -> authViewModel.registerAndSendOtp(state.email, password) { /* Se queda en la misma pantalla */ }
                                "LOGIN" -> authViewModel.loginAndSendOtp(state.email, password) { /* Se queda en la misma pantalla */ }
                                else -> authViewModel.requestPasswordResetOtp(state.email) { /* Se queda en la misma pantalla */ }
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
                                authViewModel.resetPassword(state.email, password) {
                                    authState = AuthState.Login
                                }
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
// Componentes Composable Reutilizables (SIN CAMBIOS)
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
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
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
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
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
            onValueChange = onEmailChange,
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
            onValueChange = onOtpChange,
            label = { Text("Código OTP") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit, enabled = !isLoading && otpCode.length >= 6, // Ajustado a 6 dígitos
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
            onValueChange = onPasswordChange,
            label = { Text("Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
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