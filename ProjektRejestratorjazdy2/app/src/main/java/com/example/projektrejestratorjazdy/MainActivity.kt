package com.example.projektrejestratorjazdy

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Pobieranie ViewModel i Stanu UI
            val viewModel: RecorderViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            val navController = rememberNavController()

            // 2. Obsługa uprawnień (Kamera + GPS)
            val permissionsState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )

            // Zapytanie o uprawnienia przy starcie
            LaunchedEffect(Unit) {
                permissionsState.launchMultiplePermissionRequest()
            }

            // Start sensorów dopiero gdy uprawnienia są przyznane
            LaunchedEffect(permissionsState.allPermissionsGranted) {
                if (permissionsState.allPermissionsGranted) {
                    viewModel.startSensors()
                }
            }

            // 3. Główny widok aplikacji
            if (permissionsState.allPermissionsGranted) {
                NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

                    // --- EKRAN 1: DASHBOARD (Kamera + Liczniki) ---
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            state = state,
                            onToggleRecord = { viewModel.toggleRecording() },
                            onNavigateHistory = { navController.navigate(Screen.History.route) }
                        )
                    }

                    // --- EKRAN 2: HISTORIA (Lista przejazdów) ---
                    composable(Screen.History.route) {
                        HistoryScreen(
                            history = state.history,
                            onDelete = { viewModel.deleteSession(it) },
                            onSessionClick = { sessionId ->
                                // Kliknięcie w element listy otwiera szczegóły (wykresy)
                                navController.navigate(Screen.Details.createRoute(sessionId))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // --- EKRAN 3: SZCZEGÓŁY (Wykresy prędkości i G-Force) ---
                    composable(Screen.Details.route) { backStackEntry ->
                        // Pobranie przekazanego ID sesji
                        val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull() ?: 0

                        SessionDetailScreen(
                            sessionId = sessionId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            } else {
                // Ekran informacyjny w przypadku braku uprawnień
                Text("Aplikacja wymaga uprawnień do Kamery i GPS, aby działać poprawnie. Proszę nadać uprawnienia w ustawieniach.")
            }
        }
    }
}