package com.memorycard.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorycard.model.CardExpression
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val _playerName = MutableStateFlow("")
    val playerName = _playerName.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound = _currentRound.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _card1 = MutableStateFlow<CardExpression?>(null)
    val card1 = _card1.asStateFlow()

    private val _card2 = MutableStateFlow<CardExpression?>(null)
    val card2 = _card2.asStateFlow()

    private val _resultsRevealed = MutableStateFlow(false)
    val resultsRevealed = _resultsRevealed.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver = _gameOver.asStateFlow()

    private val _timeLeft = MutableStateFlow(10f)
    val timeLeft = _timeLeft.asStateFlow()

    private val _gameEvent = kotlinx.coroutines.flow.MutableSharedFlow<GameEvent>()
    val gameEvent = _gameEvent.asSharedFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    sealed class GameEvent {
        object Win : GameEvent()
        object Lose : GameEvent()
        object NextRound : GameEvent()
    }

    init {
        generateNewRound()
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    fun resetGame() {
        _currentRound.value = 1
        _score.value = 0
        _resultsRevealed.value = false
        _gameOver.value = false
        generateNewRound()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timeLeft.value = 10f
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_resultsRevealed.value) {
                delay(100)
                _timeLeft.value -= 0.1f
            }
            if (_timeLeft.value <= 0 && !_resultsRevealed.value) {
                _gameOver.value = true
                _gameEvent.emit(GameEvent.Lose)
            }
        }
    }

    fun onCardSelected(selectedCard: CardExpression) {
        if (_resultsRevealed.value || _gameOver.value) return

        _resultsRevealed.value = true
        timerJob?.cancel()
        val otherCard = if (selectedCard == _card1.value) _card2.value else _card1.value

        if (selectedCard.result > (otherCard?.result ?: 0)) {
            // Correct guess
            _score.value += 1
            viewModelScope.launch {
                _gameEvent.emit(GameEvent.Win)
                delay(500)
                _gameEvent.emit(GameEvent.NextRound) // This will show "Correcto"
                delay(2000) // Wait while "Correcto" is shown on current cards
                _currentRound.value += 1
                _resultsRevealed.value = false
                generateNewRound()
            }
        } else {
            // Wrong guess
            viewModelScope.launch {
                _gameEvent.emit(GameEvent.Lose)
                delay(2000)
                _gameOver.value = true
            }
        }
    }

    private fun generateNewRound() {
        var expr1: CardExpression
        var expr2: CardExpression
        
        do {
            expr1 = generateExpression()
            expr2 = generateExpression()
        } while (expr1.result == expr2.result)

        _card1.value = expr1
        _card2.value = expr2
        startTimer()
    }

    private fun generateExpression(): CardExpression {
        val ops = listOf("+", "-", "*", "/")
        val op = ops.random()
        
        var val1: Int
        var val2: Int
        var res: Int
        
        do {
            when (op) {
                "+" -> {
                    res = Random.nextInt(2, 21) // 2 to 20
                    val1 = Random.nextInt(1, res)
                    val2 = res - val1
                }
                "-" -> {
                    val1 = Random.nextInt(2, 21)
                    val2 = Random.nextInt(1, val1)
                    res = val1 - val2
                }
                "*" -> {
                    res = Random.nextInt(1, 21)
                    val divisors = (1..res).filter { res % it == 0 }
                    val1 = divisors.random()
                    val2 = res / val1
                }
                "/" -> {
                    res = Random.nextInt(1, 21)
                    val2 = Random.nextInt(1, 10)
                    val1 = res * val2
                    // Ensure val1 doesn't look too complicated, though technically res 1-20 is the rule.
                }
                else -> {
                    val1 = 1; val2 = 1; res = 2
                }
            }
        } while (res < 1 || res > 20)

        val expressionStr = when (op) {
            "*" -> "$val1 × $val2"
            "/" -> "$val1 ÷ $val2"
            else -> "$val1 $op $val2"
        }

        return CardExpression(expressionStr, res)
    }
}
