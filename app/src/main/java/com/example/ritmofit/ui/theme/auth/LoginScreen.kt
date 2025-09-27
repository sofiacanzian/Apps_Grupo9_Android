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
import androidx.compose.ui.platform.LocalContext // Contexto para acceder a servicios de Android
import androidx.biometric.BiometricManager // Gestión de biometría
import androidx.biometric.BiometricPrompt // Diálogo biométrico
import androidx.fragment.app.FragmentActivity // Necesario para BiometricPrompt en Compose
import android.content.Context
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import androidx.compose.material.icons.Icons // Ícono de huella
import androidx.compose.material.icons.filled.Fingerprint


// Definimos los estados de la interfaz de autenticación
sealed class AuthState {
    object Login : AuthState()
    object Register : AuthState()
    object RequestPasswordReset : AuthState()
    // Añadimos 'email' y 'nextAction' para la acción de verificación final
    data class OtpVerification(val email: String, val nextAction: String) : AuthState()
    data class ResetPassword(val email: String) : AuthState()
}

// ====================================================================
// FUNCIÓN DE UTILIDAD PARA LA BIOMETRÍA
// ====================================================================

fun showBiometricPrompt(
    context: Context,
    onSuccess: () -> Unit,
    onError: (errString: CharSequence) -> Unit
) {
    // Es necesario que el contexto sea una FragmentActivity
    val activity = context as? FragmentActivity
    if (activity == null) {
        onError("Error: La Activity no soporta FragmentActivity.")
        return
    }

    val executor: Executor = Executors.newSingleThreadExecutor()

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            // Ignorar cancelación del usuario o botón negativo
            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                onError(errString)
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess() // ¡Éxito!
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // El diálogo del sistema gestiona el reintento por fallo de lectura
        }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso a RitmoFit")
        .setSubtitle("Autentícate con tu huella dactilar o credenciales")
        .setNegativeButtonText("Usar Contraseña de la App")
        // Permite Biometría Fuerte O Credenciales de Dispositivo (PIN/Patrón/Contraseña)
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    // Se elimina 'onLoginSuccess' ya que la navegación es reactiva al estado del ViewModel
) {
    // OBTENEMOS EL CONTEXTO para la biometría
    val context = LocalContext.current

    // Definición de la acción a realizar al iniciar sesión con biometría
    val onBiometricLogin: () -> Unit = {
        showBiometricPrompt(
            context = context,
            onSuccess = {
                // Llama a la función de éxito biométrico en el ViewModel
                authViewModel.handleBiometricSuccess() // ¡Debes añadir esta función en AuthViewModel!
            },
            onError = { errString ->
                // Mostrar error en el Snackbar
                authViewModel.setErrorMessage(errString.toString())
            }
        )
    }
    var authState by remember { mutableStateOf<AuthState>(AuthState.Login) }

    // Estados de entrada para todos los flujos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                // Contraseña restablecida
                msg.contains("Contraseña restablecida con éxito") || msg.contains("Registro verificado") -> {
                    authState = AuthState.Login // Vuelve al login tras restablecer o verificar registro
                    authViewModel.clearMessages()
                }
                // Login exitoso (Sesión iniciada con éxito)
                msg.contains("Sesión iniciada con éxito") -> {
                    // La navegación a la pantalla principal ocurre automáticamente si isAuthenticated cambia a true
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
                        // ✅ CAMBIADO: Botón de Login directo
                        buttonText = "Iniciar Sesión",
                        onSubmit = {
                            // 🔑 Llama directamente al nuevo endpoint /api/auth/login
                            authViewModel.login(email, password)
                            // La navegación ocurre reactivamente si el login es exitoso
                        },
                        onBiometricLogin = onBiometricLogin // <-- AÑADE ESTA LÍNEA
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
                                    // El éxito de esta función lleva a la verificación de registro
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
                            // Después de solicitar el OTP, navega a la verificación.
                            authViewModel.requestPasswordResetOtp(email) {
                                authState = AuthState.OtpVerification(email, "RESET_PASSWORD")
                            }
                        }
                    ) {
                        TextButton(onClick = { authState = AuthState.Login }) {
                            Text("Volver al inicio de sesión")
                        }
                    }
                }

                is AuthState.OtpVerification -> {
                    // Leemos el estado del OTP directamente del ViewModel
                    val vmOtpCode by authViewModel.otp.collectAsState()

                    OtpVerificationForm(
                        email = state.email,
                        // Usamos el estado del ViewModel
                        otpCode = vmOtpCode,
                        // Escribimos el valor de entrada directamente al ViewModel
                        onOtpChange = { newOtp ->
                            authViewModel.setOtp(newOtp)
                        },
                        isLoading = isLoading,
                        onSubmit = {
                            // Lógica de verificación. Solo se usa para REGISTER o RESET_PASSWORD
                            authViewModel.confirmOtp(state.email, state.nextAction) {
                                // ✅ LÓGICA DE NAVEGACIÓN DESPUÉS DE LA VERIFICACIÓN DE OTP:
                                if (state.nextAction == "RESET_PASSWORD") {
                                    // Si la verificación para RESET_PASSWORD es exitosa, pasa al formulario de cambio.
                                    authState = AuthState.ResetPassword(state.email)
                                }
                                // Para "REGISTER", el LaunchedEffect se encargará de volver a AuthState.Login
                            }
                        },
                        onRequestNewOtp = {
                            when (state.nextAction) {
                                "REGISTER" -> authViewModel.registerAndSendOtp(state.email, password) { /* Se queda en la misma pantalla */ }
                                // ❌ ELIMINADA la llamada a loginAndSendOtp
                                // Si es password reset, usamos la función de reset.
                                "RESET_PASSWORD" -> authViewModel.requestPasswordResetOtp(state.email) { /* Se queda en la misma pantalla */ }
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
                    // El OTP para el reset ya está almacenado en el ViewModel,
                    // por lo que solo necesitamos la nueva contraseña.
                    ResetPasswordForm(
                        email = state.email,
                        password = password, onPasswordChange = { password = it },
                        confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it },
                        isLoading = isLoading,
                        onSubmit = {
                            // Obtenemos el OTP guardado
                            val currentOtp = authViewModel.otp.value

                            if (password == confirmPassword) {
                                if (currentOtp.isNotBlank()) {
                                    // Pasamos el OTP almacenado para la API
                                    authViewModel.resetPassword(state.email, password, currentOtp) {
                                        // El onPasswordResetSuccess del VM establece el mensaje de éxito
                                        // y este LaunchedEffect navegará a AuthState.Login
                                    }
                                } else {
                                    authViewModel.setErrorMessage("El código de verificación está vacío o ha expirado. Por favor, solicítelo de nuevo.")
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
    onBiometricLogin: () -> Unit,
    footer: @Composable () -> Unit
)  {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)

    val isBiometricAvailable = remember {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }
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

        if (isBiometricAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBiometricLogin, // <--- Llama al callback para disparar el prompt
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Login Biométrico",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar Sesión con Huella/PIN")
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