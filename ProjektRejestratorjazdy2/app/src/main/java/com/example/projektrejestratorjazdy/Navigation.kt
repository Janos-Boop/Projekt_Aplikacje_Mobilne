package com.example.projektrejestratorjazdy

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Details : Screen("details/{sessionId}") { // Nowa trasa z parametrem
        fun createRoute(sessionId: Int) = "details/$sessionId"
    }
}