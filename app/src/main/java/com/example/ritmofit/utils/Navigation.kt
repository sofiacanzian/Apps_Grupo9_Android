package com.example.ritmofit.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ritmofit.ui.theme.auth.LoginScreen
import com.example.ritmofit.ui.theme.home.HomeScreen
import com.example.ritmofit.ui.theme.profile.ProfileScreen

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Rutas de navegación de la aplicación
 */
object RitmoFitDestinations {
    const val LOGIN_ROUTE = "login"
    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val RESERVATIONS_ROUTE = "reservations"
    const val QR_SCANNER_ROUTE = "qr_scanner"
    const val HISTORY_ROUTE = "history"
    const val CLASS_DETAIL_ROUTE = "class_detail"
}

/**
 * Composable principal de navegación
 */
@Composable
fun RitmoFitNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = RitmoFitDestinations.LOGIN_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de Login
        composable(RitmoFitDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    // Navegar a Home y limpiar el stack de navegación
                    navController.navigate(RitmoFitDestinations.HOME_ROUTE) {
                        popUpTo(RitmoFitDestinations.LOGIN_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Pantalla Principal (Home)
        composable(RitmoFitDestinations.HOME_ROUTE) {
            HomeScreen(
                onNavigateToReservations = {
                    navController.navigate(RitmoFitDestinations.RESERVATIONS_ROUTE)
                },
                onNavigateToProfile = {
                    navController.navigate(RitmoFitDestinations.PROFILE_ROUTE)
                },
                onNavigateToQrScanner = {
                    navController.navigate(RitmoFitDestinations.QR_SCANNER_ROUTE)
                },
                onNavigateToHistory = {
                    navController.navigate(RitmoFitDestinations.HISTORY_ROUTE)
                },
                onClassClick = { classId ->
                    navController.navigate("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/$classId")
                }
            )
        }

        // Pantalla de Perfil
        composable(RitmoFitDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    // Volver al login y limpiar stack
                    navController.navigate(RitmoFitDestinations.LOGIN_ROUTE) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Pantalla de Mis Reservas
        composable(RitmoFitDestinations.RESERVATIONS_ROUTE) {
            ReservationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClassClick = { classId ->
                    navController.navigate("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/$classId")
                }
            )
        }

        // Pantalla de Scanner QR
        composable(RitmoFitDestinations.QR_SCANNER_ROUTE) {
            QrScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanSuccess = { qrData ->
                    // Manejar resultado del QR y volver
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Historial
        composable(RitmoFitDestinations.HISTORY_ROUTE) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Detalle de Clase
        composable("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/{classId}") { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            ClassDetailScreen(
                classId = classId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReservationSuccess = {
                    // Opcional: navegar a reservas o mostrar confirmación
                    navController.popBackStack()
                }
            )
        }
    }
}

// Pantallas temporales - las implementaremos después

@Composable
fun ReservationsScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (String) -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text("Mis Reservas")
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Button(onClick = onNavigateBack) {
                androidx.compose.material3.Text("Volver")
            }
        }
    }
}

@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onScanSuccess: (String) -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text("Scanner QR")
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Button(onClick = onNavigateBack) {
                androidx.compose.material3.Text("Volver")
            }
            androidx.compose.material3.Button(onClick = { onScanSuccess("mock_qr_data") }) {
                androidx.compose.material3.Text("Simular Scan")
            }
        }
    }
}

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text("Historial")
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Button(onClick = onNavigateBack) {
                androidx.compose.material3.Text("Volver")
            }
        }
    }
}

@Composable
fun ClassDetailScreen(
    classId: String,
    onNavigateBack: () -> Unit,
    onReservationSuccess: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text("Detalle de Clase: $classId")
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Button(onClick = onNavigateBack) {
                androidx.compose.material3.Text("Volver")
            }
            androidx.compose.material3.Button(onClick = onReservationSuccess) {
                androidx.compose.material3.Text("Reservar")
            }
        }
    }
}