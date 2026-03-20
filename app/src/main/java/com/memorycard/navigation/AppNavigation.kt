package com.memorycard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.memorycard.ui.screens.GameScreen
import com.memorycard.ui.screens.HomeScreen
import com.memorycard.ui.screens.ResultScreen
import com.memorycard.viewmodel.GameViewModel

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToGame = { name ->
                    viewModel.setPlayerName(name)
                    viewModel.resetGame()
                    navController.navigate("game") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable("game") {
            GameScreen(
                viewModel = viewModel,
                onNavigateToResult = {
                    navController.navigate("result") {
                        popUpTo("game") { inclusive = true }
                    }
                }
            )
        }
        composable("result") {
            ResultScreen(
                viewModel = viewModel,
                onRestart = {
                    navController.navigate("home") {
                        popUpTo("result") { inclusive = true }
                    }
                }
            )
        }
    }
}
