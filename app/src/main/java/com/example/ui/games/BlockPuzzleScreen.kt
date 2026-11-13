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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.i18n.LocalStrings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VividPurple
import kotlin.random.Random

data class BlockPiece(
    val id: Int,
    val cells: List<Pair<Int, Int>>, // (rowOffset, colOffset)
    val colorHex: Long
)

object BlockPuzzleEngine {
    const val BOARD_SIZE = 8

    private val SHAPES: List<List<Pair<Int, Int>>> = listOf(
        listOf(0 to 0),
        listOf(0 to 0, 0 to 1),
        listOf(0 to 0, 1 to 0),
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(0 to 0, 1 to 0, 2 to 0),
        listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),
        listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1),
        listOf(0 to 0, 0 to 1, 0 to 2, 1 to 1),
        listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2),
        listOf(0 to 0, 1 to 0, 1 to 1),
        listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3),
        listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
    )

    private val PIECE_COLORS = listOf(
        0xFF2563EB,
        0xFF7C3AED,
        0xFF06B6D4,
        0xFF10B981,
        0xFFF59E0B,
        0xFFEC4899
    )

    fun emptyBoard(): List<Long> = List(BOARD_SIZE * BOARD_SIZE) { 0L }

    fun generateTray(startId: Int = 1, random: Random = Random.Default): List<BlockPiece> {
        return (0 until 3).map { idx ->
            BlockPiece(
                id = startId + idx,
                cells = SHAPES[random.nextInt(SHAPES.size)],
                colorHex = PIECE_COLORS[random.nextInt(PIECE_COLORS.size)]
            )
        }
    }

    fun canPlace(board: List<Long>, piece: BlockPiece, anchorRow: Int, anchorCol: Int): Boolean {
        for ((dr, dc) in piece.cells) {
            val r = anchorRow + dr
            val c = anchorCol + dc
            if (r !in 0 until BOARD_SIZE || c !in 0 until BOARD_SIZE) return false
            if (board[r * BOARD_SIZE + c] != 0L) return false
        }
        return true
    }

    fun canPlaceAnywhere(board: List<Long>, piece: BlockPiece): Boolean {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (canPlace(board, piece, r, c)) return true
            }
        }
        return false
    }

    data class PlaceResult(
        val board: List<Long>,
        val linesCleared: Int,
        val pointsEarned: Int
    )

    fun placeAndClear(board: List<Long>, piece: BlockPiece, anchorRow: Int, anchorCol: Int): PlaceResult {
        val next = board.toMutableList()
        for ((dr, dc) in piece.cells) {
            val r = anchorRow + dr
            val c = anchorCol + dc
            next[r * BOARD_SIZE + c] = piece.colorHex
        }

        val fullRows = (0 until BOARD_SIZE).filter { r ->
            (0 until BOARD_SIZE).all { c -> next[r * BOARD_SIZE + c] != 0L }
        }
        val fullCols = (0 until BOARD_SIZE).filter { c ->
            (0 until BOARD_SIZE).all { r -> next[r * BOARD_SIZE + c] != 0L }
        }

        for (r in fullRows) {
            for (c in 0 until BOARD_SIZE) {
                next[r * BOARD_SIZE + c] = 0L
            }
        }
        for (c in fullCols) {
            for (r in 0 until BOARD_SIZE) {
                next[r * BOARD_SIZE + c] = 0L
            }
        }

        val lines = fullRows.size + fullCols.size
        val points = piece.cells.size * 5 + lines * lines * 25
        return PlaceResult(next, lines, points)
    }
}

@Composable
fun BlockPuzzleScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onGameFinished: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var board by remember { mutableStateOf(BlockPuzzleEngine.emptyBoard()) }
    var nextIdSeed by remember { mutableIntStateOf(4) }
    var tray by remember { mutableStateOf(BlockPuzzleEngine.generateTray(1)) }
    var selectedTrayIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var localBest by remember(bestScore) { mutableIntStateOf(bestScore) }
    var isGameOver by remember { mutableStateOf(false) }
    var scoreSubmitted by remember { mutableStateOf(false) }

    fun resetGame() {
        board = BlockPuzzleEngine.emptyBoard()
        tray = BlockPuzzleEngine.generateTray(1)
        nextIdSeed = 4
        selectedTrayIndex = 0
        score = 0
        isGameOver = false
        scoreSubmitted = false
    }

    fun tryPlaceAt(row: Int, col: Int) {
        if (isGameOver || selectedTrayIndex !in tray.indices) return
        val piece = tray[selectedTrayIndex]
        if (!BlockPuzzleEngine.canPlace(board, piece, row, col)) return

        val placed = BlockPuzzleEngine.placeAndClear(board, piece, row, col)
        board = placed.board
        score += placed.pointsEarned
        if (score > localBest) localBest = score

        val remaining = tray.toMutableList().apply { removeAt(selectedTrayIndex) }
        val nextTray = if (remaining.isEmpty()) {
            val fresh = BlockPuzzleEngine.generateTray(nextIdSeed)
            nextIdSeed += 3
            fresh
        } else remaining

        tray = nextTray
        selectedTrayIndex = 0

        val anyMoveAvailable = nextTray.any { BlockPuzzleEngine.canPlaceAnywhere(board, it) }
        if (!anyMoveAvailable) {
            isGameOver = true
            if (!scoreSubmitted && score > 0) {
                scoreSubmitted = true
                onGameFinished(score)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("blocks_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.gameBlocksTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("blocks_reset_button")) {
                Icon(Icons.Default.Refresh, contentDescription = strings.reset)
            }
        }

        // Score & Best
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.score, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.testTag("blocks_score_text")
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.best, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$localBest",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = VividPurple
                    )
                }
            }
        }

        Text(
            text = strings.blocksHint,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 8x8 Grid
        val selectedPiece = tray.getOrNull(selectedTrayIndex)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (r in 0 until BlockPuzzleEngine.BOARD_SIZE) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (c in 0 until BlockPuzzleEngine.BOARD_SIZE) {
                            val cellHex = board[r * BlockPuzzleEngine.BOARD_SIZE + c]
                            val canPlaceHere = selectedPiece != null &&
                                BlockPuzzleEngine.canPlace(board, selectedPiece, r, c)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (cellHex != 0L) Color(cellHex)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (cellHex == 0L && canPlaceHere) 1.dp else 0.dp,
                                        color = if (cellHex == 0L && canPlaceHere) {
                                            Color(selectedPiece!!.colorHex).copy(alpha = 0.28f)
                                        } else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable(enabled = !isGameOver) { tryPlaceAt(r, c) }
                                    .testTag("blocks_cell_${r}_${c}")
                            )
                        }
                    }
                }
            }

            if (isGameOver) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.gameOver,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${strings.score}: $score",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = RoyalBlue
                        )
                        Button(onClick = { resetGame() }) {
                            Text(strings.playAgain, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Piece Tray (3 pieces)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tray.forEachIndexed { idx, piece ->
                val isSelected = idx == selectedTrayIndex
                val canFit = BlockPuzzleEngine.canPlaceAnywhere(board, piece)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color(piece.colorHex) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { selectedTrayIndex = idx }
                        .testTag("blocks_tray_piece_$idx"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(piece.colorHex).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MiniPiecePreview(piece = piece, enabled = canFit)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPiecePreview(piece: BlockPiece, enabled: Boolean) {
    val maxR = piece.cells.maxOf { it.first }
    val maxC = piece.cells.maxOf { it.second }
    val color = Color(piece.colorHex).copy(alpha = if (enabled) 1f else 0.35f)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (r in 0..maxR) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (c in 0..maxC) {
                    val filled = (r to c) in piece.cells
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (filled) color else Color.Transparent)
                    )
                }
            }
        }
    }
}
