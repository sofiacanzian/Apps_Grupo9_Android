package com.example.ritmofit.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ritmofit.home.HomeScreen
import com.example.ritmofit.ui.theme.auth.LoginScreen
import com.example.ritmofit.ui.theme.auth.OtpScreen
import com.example.ritmofit.ui.theme.auth.AuthViewModel
import com.example.ritmofit.ui.theme.classes.ClassesScreen
import com.example.ritmofit.ui.theme.history.HistoryScreen
import com.example.ritmofit.profile.ProfileScreen
import com.example.ritmofit.ui.theme.reservation.ReservationsScreen
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.ui.theme.classes.ClassDetailScreen
import com.example.ritmofit.home.HomeViewModel
import com.example.ritmofit.ui.theme.classes.ClassesViewModel
import com.example.ritmofit.ui.theme.classes.ClassDetailViewModel
import com.example.ritmofit.ui.theme.history.HistoryViewModel
import com.example.ritmofit.profile.ProfileViewModel
import com.example.ritmofit.ui.theme.reservation.ReservationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitmoFitNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val isUserAuthenticated by authViewModel.isAuthenticated.collectAsState()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    LaunchedEffect(isUserAuthenticated) {
        if (isUserAuthenticated) {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (currentRoute) {
                        "login" -> Text("Iniciar Sesión")
                        "otp/{email}" -> Text("Verificación OTP")
                        "home" -> Text("RitmoFit")
                        "profile" -> Text("Mi Perfil")
                        "classes" -> Text("Clases")
                        "reservations" -> Text("Mis Reservas")
                        "history" -> Text("Historial")
                        "classDetail/{classId}" -> Text("Detalles de Clase")
                        "qrscanner" -> Text("Escaner QR")
                        else -> Text("RitmoFit")
                    }
                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isUserAuthenticated) "home" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToOtp = { email -> navController.navigate("otp/$email") }
                )
            }

            composable(
                route = "otp/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                OtpScreen(
                    email = email,
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onVerificationSuccess = { navController.navigate("home") }
                )
            }

            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
                HomeScreen(
                    onNavigateToReservations = { navController.navigate("reservations") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToQrScanner = { navController.navigate("qrscanner") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToClasses = { navController.navigate("classes") },
                    onClassClick = { gymClass -> navController.navigate("classDetail/${gymClass.id}") },
                    homeViewModel = homeViewModel
                )
            }

            composable("classes") {
                val classesViewModel: ClassesViewModel = viewModel(factory = ClassesViewModel.Factory)
                ClassesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onClassClick = { gymClass ->
                        navController.navigate("classDetail/${gymClass.id}")
                    },
                    classesViewModel = classesViewModel
                )
            }

            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        SessionManager.userId = null
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    profileViewModel = profileViewModel
                )
            }

            composable("reservations") {
                val reservationsViewModel: ReservationsViewModel = viewModel(factory = ReservationsViewModel.Factory)
                ReservationsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onClassClick = { gymClass ->
                        navController.navigate("classDetail/${gymClass.id}")
                    },
                    reservationsViewModel = reservationsViewModel
                )
            }

            composable("history") {
                val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onClassClick = { gymClass ->
                        navController.navigate("classDetail/${gymClass.id}")
                    },
                    historyViewModel = historyViewModel
                )
            }

            composable("classDetail/{classId}", arguments = listOf(navArgument("classId") { type = NavType.StringType })) { backStackEntry ->
                val classDetailViewModel: ClassDetailViewModel = viewModel(factory = ClassDetailViewModel.Factory)
                val reservationsViewModel: ReservationsViewModel = viewModel(factory = ReservationsViewModel.Factory)
                val classId = backStackEntry.arguments?.getString("classId") ?: ""
                ClassDetailScreen(
                    classId = classId,
                    onNavigateBack = { navController.popBackStack() },
                    onReservationSuccess = { navController.popBackStack() },
                    classDetailViewModel = classDetailViewModel,
                    reservationsViewModel = reservationsViewModel
                )
            }

            composable("qrscanner") {
                QrScannerPlaceholder(
                    onNavigateBack = { navController.popBackStack() },
                    onScanSuccess = { navController.popBackStack() }
                )
            }
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