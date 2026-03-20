package com.memorycard.ui.screens

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorycard.app.R
import com.memorycard.model.CardExpression
import com.memorycard.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val currentRound by viewModel.currentRound.collectAsState()
    val score by viewModel.score.collectAsState()
    val card1 by viewModel.card1.collectAsState()
    val card2 by viewModel.card2.collectAsState()
    val resultsRevealed by viewModel.resultsRevealed.collectAsState()
    val gameOver by viewModel.gameOver.collectAsState()

    var showNextRoundEffect by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.gameEvent.collectLatest { event ->
            when (event) {
                is GameViewModel.GameEvent.Win -> {
                    MediaPlayer.create(context, R.raw.win).start()
                }
                is GameViewModel.GameEvent.Lose -> {
                    MediaPlayer.create(context, R.raw.lose).start()
                }
                is GameViewModel.GameEvent.NextRound -> {
                    showNextRoundEffect = true
                    kotlinx.coroutines.delay(1000)
                    showNextRoundEffect = false
                }
            }
        }
    }

    LaunchedEffect(gameOver) {
        if (gameOver) {
            onNavigateToResult()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RONDA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$currentRound",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PUNTAJE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$score",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Cards with animations
                if (card1 != null && card2 != null) {
                    val animateCard1 by animateFloatAsState(
                        targetValue = if (resultsRevealed) 180f else 0f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                    
                    val animateCard2 by animateFloatAsState(
                        targetValue = if (resultsRevealed) 180f else 0f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )

                    GameCard(
                        card = card1!!,
                        rotation = animateCard1,
                        revealed = resultsRevealed,
                        onClick = { viewModel.onCardSelected(card1!!) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    GameCard(
                        card = card2!!,
                        rotation = animateCard2,
                        revealed = resultsRevealed,
                        onClick = { viewModel.onCardSelected(card2!!) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = if (resultsRevealed) "¡Revelando!" else "¿Cuál expresión es mayor?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            // Next Round Effect
            AnimatedVisibility(
                visible = showNextRoundEffect,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp
                ) {
                    Text(
                        text = "¡Correcto!",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    card: CardExpression,
    rotation: Float,
    revealed: Boolean,
    onClick: () -> Unit
) {
    val isBack = rotation > 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                rotationX = rotation
                cameraDistance = 12f * density
            }
            .clickable { if (!revealed) onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBack) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isBack) {
                // Front Side (Hidden result)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.expression,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "?",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Back Side (Revealed result)
                Box(modifier = Modifier.graphicsLayer { rotationX = 180f }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.expression,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${card.result}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
