package com.memorycard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.memorycard.ui.screens.GameScreen
import com.memorycard.ui.screens.HomeScreen
import com.memorycard.ui.screens.ResultScreen
import com.memorycard.viewmodel.GameViewModel

/**
 * Composable principal que define el grafo de navegación de la aplicación MemoryCard.
 *
 * Gestiona el flujo entre las tres pantallas principales del juego:
 * - **Home** → pantalla inicial donde el jugador ingresa su nombre.
 * - **Game** → pantalla de juego donde se desarrolla la partida.
 * - **Result** → pantalla de resultados al finalizar la partida.
 *
 * Cada transición elimina la pantalla anterior del back stack (`popUpTo + inclusive = true`),
 * evitando que el usuario pueda regresar con el botón "Atrás" a estados ya completados.
 *
 * @param viewModel Instancia compartida de [GameViewModel] que mantiene el estado
 * del juego y es pasada a las pantallas que la necesitan.
 */
@Composable
fun AppNavigation(viewModel: GameViewModel) {
    /**
     * NavHost: contenedor del grafo de navegación.
     * Define "home" como destino inicial al lanzar la app.
     */
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToGame = { name ->
                    // Guarda el nombre del jugador en el ViewModel
                    viewModel.setPlayerName(name)
                    // Reinicia el estado del juego antes de comenzar
                    viewModel.resetGame()
                     // Navega a "game" y elimina "home" del back stack
                    // para evitar regresar a esta pantalla con "Atrás"
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
                     // Al terminar la partida, navega a "result", (pantalla de resultado del jugador)
                    // y elimina "game" del back stack para
                    // impedir volver al juego ya finalizado
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
                    // Al reiniciar, vuelve a "home" y limpia
                    // completamente el back stack desde "result"
                    navController.navigate("home") {
                        popUpTo("result") { inclusive = true }
                    }
                }
            )
        }
    }
}
