package com.example.ritmofit.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ritmofit.home.HomeScreen
import com.example.ritmofit.ui.theme.auth.LoginScreen
import com.example.ritmofit.ui.theme.auth.OtpScreen
import com.example.ritmofit.ui.theme.auth.AuthViewModel
import com.example.ritmofit.ui.theme.classes.ClassDetailScreen
import com.example.ritmofit.ui.theme.history.HistoryScreen
import com.example.ritmofit.ui.theme.reservation.ReservationsScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.ritmofit.home.HomeViewModel
import com.example.ritmofit.ui.theme.classes.ClassesViewModel
import com.example.ritmofit.ui.theme.reservation.ReservationsViewModel
import com.example.ritmofit.profile.ProfileScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.ritmofit.data.models.GymClass
import kotlinx.coroutines.flow.onEach

object RitmoFitDestinations {
    const val LOGIN_ROUTE = "login"
    const val OTP_ROUTE = "otp"
    const val OTP_ARG = "email"
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
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(Unit) {
        authViewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthViewModel.NavigationEvent.NavigateToOtp -> {
                    navController.navigate("${RitmoFitDestinations.OTP_ROUTE}/${event.email}")
                }
                is AuthViewModel.NavigationEvent.NavigateToHome -> {
                    navController.navigate(RitmoFitDestinations.HOME_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val reservationsViewModel: ReservationsViewModel = viewModel()
    val classesViewModel: ClassesViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(RitmoFitDestinations.LOGIN_ROUTE) {
            LoginScreen(authViewModel = authViewModel)
        }

        composable(
            route = "${RitmoFitDestinations.OTP_ROUTE}/{${RitmoFitDestinations.OTP_ARG}}",
            arguments = listOf(navArgument(RitmoFitDestinations.OTP_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString(RitmoFitDestinations.OTP_ARG) ?: ""
            OtpScreen(
                email = email,
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
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
                onClassClick = { gymClass ->
                    navController.navigate("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/${gymClass.id}")
                },
                homeViewModel = homeViewModel,
                classesViewModel = classesViewModel
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
                onClassClick = { gymClassId ->
                    navController.navigate("${RitmoFitDestinations.CLASS_DETAIL_ROUTE}/${gymClassId}")
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
                }
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