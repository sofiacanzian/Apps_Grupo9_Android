package com.example.ritmofit.utils

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ritmofit.ui.classes.ClassListScreen
import com.example.ritmofit.ui.classes.ClassDetailScreen
import com.example.ritmofit.di.ViewModelProvider

@Composable
fun RitmoFitNavigation() {
    val navController = rememberNavController()
    val classViewModel = ViewModelProvider.provideClassViewModel()

    NavHost(
        navController = navController,
        startDestination = "classList"
    ) {
        composable("classList") {
            ClassListScreen(
                viewModel = classViewModel,
                onClassClick = { classId ->
                    navController.navigate("classDetail/$classId")
                }
            )
        }

        composable(
            route = "classDetail/{classId}"
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId")
            ClassDetailScreen(
                classId = classId ?: "",
                viewModel = classViewModel,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
