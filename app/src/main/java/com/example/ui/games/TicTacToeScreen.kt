package com.example.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.LocalStrings
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VividPurple
import kotlinx.coroutines.delay

object TicTacToeEngine {
    val WIN_LINES = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    data class WinResult(val winner: String?, val line: List<Int>)

    fun checkWinner(board: List<String>): WinResult {
        for (line in WIN_LINES) {
            val a = board[line[0]]
            if (a.isNotEmpty() && a == board[line[1]] && a == board[line[2]]) {
                return WinResult(a, line)
            }
        }
        return WinResult(null, emptyList())
    }

    fun isBoardFull(board: List<String>): Boolean = board.none { it.isEmpty() }

    fun pickAiMove(board: List<String>, smart: Boolean): Int {
        val empty = board.indices.filter { board[it].isEmpty() }
        if (empty.isEmpty()) return -1
        if (!smart) return empty.random()

        // Minimax for unbeatable smart AI ('O' tries to maximize)
        var bestScore = Int.MIN_VALUE
        var bestMove = empty.first()
        for (idx in empty) {
            val copy = board.toMutableList()
            copy[idx] = "O"
            val score = minimax(copy, depth = 0, isMaximizing = false)
            if (score > bestScore) {
                bestScore = score
                bestMove = idx
            }
        }
        return bestMove
    }

    private fun minimax(board: MutableList<String>, depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner(board).winner
        if (winner == "O") return 10 - depth
        if (winner == "X") return depth - 10
        if (isBoardFull(board)) return 0

        val empty = board.indices.filter { board[it].isEmpty() }
        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (idx in empty) {
                board[idx] = "O"
                best = maxOf(best, minimax(board, depth + 1, false))
                board[idx] = ""
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (idx in empty) {
                board[idx] = "X"
                best = minOf(best, minimax(board, depth + 1, true))
                board[idx] = ""
            }
            best
        }
    }
}

@Composable
fun TicTacToeScreen(
    onBack: () -> Unit,
    onMatchCompleted: (score: Int, won: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var board by remember { mutableStateOf(List(9) { "" }) }
    var currentTurn by remember { mutableStateOf("X") }
    var vsAi by remember { mutableStateOf(true) }
    var smartAi by remember { mutableStateOf(true) }
    var xWins by remember { mutableIntStateOf(0) }
    var oWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var roundSubmitted by remember { mutableStateOf(false) }

    val winResult = TicTacToeEngine.checkWinner(board)
    val isDraw = winResult.winner == null && TicTacToeEngine.isBoardFull(board)
    val isRoundOver = winResult.winner != null || isDraw

    fun startNewRound() {
        board = List(9) { "" }
        currentTurn = "X"
        roundSubmitted = false
    }

    LaunchedEffect(winResult.winner, isDraw) {
        if (isRoundOver && !roundSubmitted) {
            roundSubmitted = true
            when (winResult.winner) {
                "X" -> {
                    xWins += 1
                    onMatchCompleted(xWins * 10, true)
                }
                "O" -> {
                    oWins += 1
                    onMatchCompleted(xWins * 10, !vsAi)
                }
                else -> {
                    draws += 1
                    onMatchCompleted(xWins * 10, false)
                }
            }
        }
    }

    LaunchedEffect(board, currentTurn, vsAi, smartAi, isRoundOver) {
        if (vsAi && currentTurn == "O" && !isRoundOver) {
            delay(360L)
            val aiIdx = TicTacToeEngine.pickAiMove(board, smartAi)
            if (aiIdx in 0..8 && board[aiIdx].isEmpty()) {
                board = board.toMutableList().also { it[aiIdx] = "O" }
                currentTurn = "X"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("ttt_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.gameTttTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { startNewRound() }) {
                Icon(Icons.Default.Refresh, contentDescription = strings.newRound)
            }
        }

        // Mode & Difficulty chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = vsAi,
                onClick = {
                    vsAi = true
                    startNewRound()
                },
                label = { Text(strings.vsAi) },
                modifier = Modifier.testTag("ttt_mode_ai")
            )
            FilterChip(
                selected = !vsAi,
                onClick = {
                    vsAi = false
                    startNewRound()
                },
                label = { Text(strings.twoPlayers) },
                modifier = Modifier.testTag("ttt_mode_2p")
            )
            if (vsAi) {
                Spacer(Modifier.weight(1f))
                FilterChip(
                    selected = !smartAi,
                    onClick = { smartAi = false },
                    label = { Text(strings.aiEasy) }
                )
                FilterChip(
                    selected = smartAi,
                    onClick = { smartAi = true },
                    label = { Text(strings.aiHard) }
                )
            }
        }

        // Scoreboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TttScoreTile("X", xWins, RoyalBlue, Modifier.weight(1f))
            TttScoreTile("=", draws, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            TttScoreTile(if (vsAi) "AI (O)" else "O", oWins, VividPurple, Modifier.weight(1f))
        }

        // Turn / Result Banner
        val statusText = when {
            winResult.winner == "X" -> strings.playerXWins
            winResult.winner == "O" -> if (vsAi) strings.aiWins else strings.playerOWins
            isDraw -> strings.drawMatch
            vsAi && currentTurn == "O" -> strings.aiThinking
            currentTurn == "X" -> strings.playerXTurn
            else -> strings.playerOTurn
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = when (winResult.winner) {
                "X" -> RoyalBlue.copy(alpha = 0.14f)
                "O" -> VividPurple.copy(alpha = 0.14f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("ttt_status_text")
                )
            }
        }

        // 3x3 Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val mark = board[idx]
                        val isWinCell = idx in winResult.line
                        val cellColor = when {
                            isWinCell -> EmeraldSuccess.copy(alpha = 0.22f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val borderColor = when {
                            isWinCell -> EmeraldSuccess
                            mark == "X" -> RoyalBlue.copy(alpha = 0.45f)
                            mark == "O" -> VividPurple.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(cellColor)
                                .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                                .clickable(
                                    enabled = mark.isEmpty() && !isRoundOver && !(vsAi && currentTurn == "O")
                                ) {
                                    board = board.toMutableList().also { it[idx] = currentTurn }
                                    currentTurn = if (currentTurn == "X") "O" else "X"
                                }
                                .testTag("ttt_cell_$idx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mark,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                color = if (mark == "X") RoyalBlue else VividPurple
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { startNewRound() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("ttt_new_round_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(strings.newRound, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TttScoreTile(
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold)
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
