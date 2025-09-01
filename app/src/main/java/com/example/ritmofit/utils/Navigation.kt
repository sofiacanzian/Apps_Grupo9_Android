package com.example.ritmofit.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.ui.theme.auth.LoginScreen
import com.example.ritmofit.ui.theme.home.HomeScreen
import com.example.ritmofit.ui.theme.profile.ProfileScreen
import com.example.ritmofit.ui.theme.classes.ClassDetailScreen
import com.example.ritmofit.ui.theme.reservations.ReservationsScreen
import com.example.ritmofit.ui.theme.history.HistoryScreen
import com.example.ritmofit.ui.theme.reservations.ReservationsViewModel

object RitmoFitDestinations {
    const val LOGIN_ROUTE = "login"
    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val RESERVATIONS_ROUTE = "reservations"
    const val QR_SCANNER_ROUTE = "qr_scanner"
    const val HISTORY_ROUTE = "history"
    const val CLASS_DETAIL_ROUTE = "class_detail"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitmoFitNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = RitmoFitDestinations.LOGIN_ROUTE
) {
    val reservationsViewModel: ReservationsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(RitmoFitDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(RitmoFitDestinations.HOME_ROUTE) {
                        popUpTo(RitmoFitDestinations.LOGIN_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }

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

        composable(RitmoFitDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(RitmoFitDestinations.LOGIN_ROUTE) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(RitmoFitDestinations.RESERVATIONS_ROUTE) {
            ReservationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClassClick = { classId ->
                    navController.navigate("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/$classId")
                },
                reservationsViewModel = reservationsViewModel
            )
        }

        composable(RitmoFitDestinations.HISTORY_ROUTE) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/{classId}") { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            ClassDetailScreen(
                classId = classId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReservationSuccess = {
                    navController.popBackStack()
                },
                reservationsViewModel = reservationsViewModel
            )
        }

        composable(RitmoFitDestinations.QR_SCANNER_ROUTE) {
            QrScannerPlaceholder(
                onNavigateBack = { navController.popBackStack() },
                onScanSuccess = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerPlaceholder(
    onNavigateBack: () -> Unit,
    onScanSuccess: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner QR") },
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Aquí irá el lector QR 📷")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onScanSuccess("mock_qr_data") }) {
                Text("Simular Scan")
            }
        }
    }
}