package com.example.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.theme.CoinGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VividPurple
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }

data class GridPoint(val x: Int, val y: Int)

object SnakeEngine {
    const val GRID_SIZE = 16

    fun initialSnake(): List<GridPoint> = listOf(
        GridPoint(8, 8),
        GridPoint(7, 8),
        GridPoint(6, 8)
    )

    fun spawnFood(snake: List<GridPoint>, random: Random = Random.Default): GridPoint {
        val occupied = snake.toSet()
        val free = buildList {
            for (x in 0 until GRID_SIZE) {
                for (y in 0 until GRID_SIZE) {
                    val pt = GridPoint(x, y)
                    if (pt !in occupied) add(pt)
                }
            }
        }
        return if (free.isEmpty()) GridPoint(0, 0) else free[random.nextInt(free.size)]
    }

    fun isOpposite(a: SnakeDirection, b: SnakeDirection): Boolean = when (a) {
        SnakeDirection.UP -> b == SnakeDirection.DOWN
        SnakeDirection.DOWN -> b == SnakeDirection.UP
        SnakeDirection.LEFT -> b == SnakeDirection.RIGHT
        SnakeDirection.RIGHT -> b == SnakeDirection.LEFT
    }

    data class StepResult(
        val snake: List<GridPoint>,
        val food: GridPoint,
        val ateFood: Boolean,
        val collided: Boolean
    )

    fun step(
        snake: List<GridPoint>,
        direction: SnakeDirection,
        food: GridPoint,
        random: Random = Random.Default
    ): StepResult {
        val head = snake.first()
        val nextHead = when (direction) {
            SnakeDirection.UP -> GridPoint(head.x, head.y - 1)
            SnakeDirection.DOWN -> GridPoint(head.x, head.y + 1)
            SnakeDirection.LEFT -> GridPoint(head.x - 1, head.y)
            SnakeDirection.RIGHT -> GridPoint(head.x + 1, head.y)
        }
        if (nextHead.x !in 0 until GRID_SIZE || nextHead.y !in 0 until GRID_SIZE) {
            return StepResult(snake, food, ateFood = false, collided = true)
        }
        val ate = nextHead == food
        val bodyToCheck = if (ate) snake else snake.dropLast(1)
        if (nextHead in bodyToCheck) {
            return StepResult(snake, food, ateFood = false, collided = true)
        }
        val nextSnake = if (ate) {
            listOf(nextHead) + snake
        } else {
            listOf(nextHead) + snake.dropLast(1)
        }
        val nextFood = if (ate) spawnFood(nextSnake, random) else food
        return StepResult(nextSnake, nextFood, ateFood = ate, collided = false)
    }
}

@Composable
fun SnakeGameScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onGameFinished: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var snake by remember { mutableStateOf(SnakeEngine.initialSnake()) }
    var food by remember { mutableStateOf(SnakeEngine.spawnFood(SnakeEngine.initialSnake())) }
    var direction by remember { mutableStateOf(SnakeDirection.RIGHT) }
    var pendingDirection by remember { mutableStateOf(SnakeDirection.RIGHT) }
    var score by remember { mutableIntStateOf(0) }
    var localBest by remember(bestScore) { mutableIntStateOf(bestScore) }
    var isRunning by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }
    var isFastSpeed by remember { mutableStateOf(false) }
    var scoreSubmitted by remember { mutableStateOf(false) }

    fun resetGame() {
        val initial = SnakeEngine.initialSnake()
        snake = initial
        food = SnakeEngine.spawnFood(initial)
        direction = SnakeDirection.RIGHT
        pendingDirection = SnakeDirection.RIGHT
        score = 0
        isGameOver = false
        isRunning = true
        scoreSubmitted = false
    }

    fun queueDirection(next: SnakeDirection) {
        if (!SnakeEngine.isOpposite(direction, next)) {
            pendingDirection = next
        }
    }

    LaunchedEffect(isRunning, isGameOver, isFastSpeed) {
        while (isRunning && !isGameOver) {
            delay(if (isFastSpeed) 115L else 175L)
            direction = pendingDirection
            val result = SnakeEngine.step(snake, direction, food)
            if (result.collided) {
                isGameOver = true
                isRunning = false
                if (!scoreSubmitted && score > 0) {
                    scoreSubmitted = true
                    if (score > localBest) localBest = score
                    onGameFinished(score)
                }
            } else {
                snake = result.snake
                food = result.food
                if (result.ateFood) {
                    score += 1
                    if (score > localBest) localBest = score
                }
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("snake_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.gameSnakeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isFastSpeed,
                    onClick = { isFastSpeed = false },
                    label = { Text(strings.speedNormal) }
                )
                FilterChip(
                    selected = isFastSpeed,
                    onClick = { isFastSpeed = true },
                    label = { Text(strings.speedFast) }
                )
            }
        }

        // Score strip
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.score, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("snake_score_text")
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
            FilledTonalIconButton(
                onClick = { if (!isGameOver) isRunning = !isRunning },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) strings.pause else strings.resume
                )
            }
            FilledTonalIconButton(
                onClick = { resetGame() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("snake_reset_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = strings.reset)
            }
        }

        // Snake Canvas Board
        val boardBg = MaterialTheme.colorScheme.surfaceVariant
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(boardBg)
                .pointerInput(Unit) {
                    var totalDx = 0f
                    var totalDy = 0f
                    detectDragGestures(
                        onDragStart = {
                            totalDx = 0f
                            totalDy = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDx += dragAmount.x
                            totalDy += dragAmount.y
                        },
                        onDragEnd = {
                            if (abs(totalDx) > abs(totalDy)) {
                                if (totalDx > 24f) queueDirection(SnakeDirection.RIGHT)
                                else if (totalDx < -24f) queueDirection(SnakeDirection.LEFT)
                            } else {
                                if (totalDy > 24f) queueDirection(SnakeDirection.DOWN)
                                else if (totalDy < -24f) queueDirection(SnakeDirection.UP)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val cellSize = size.width / SnakeEngine.GRID_SIZE.toFloat()

                // Subtle grid lines
                for (i in 0..SnakeEngine.GRID_SIZE) {
                    val p = i * cellSize
                    drawLine(gridLineColor, Offset(p, 0f), Offset(p, size.height), strokeWidth = 1f)
                    drawLine(gridLineColor, Offset(0f, p), Offset(size.width, p), strokeWidth = 1f)
                }

                // Draw Food Star
                val foodCenter = Offset(
                    food.x * cellSize + cellSize / 2f,
                    food.y * cellSize + cellSize / 2f
                )
                drawCircle(
                    color = CoinGold.copy(alpha = 0.28f),
                    radius = cellSize * 0.48f,
                    center = foodCenter
                )
                drawCircle(
                    color = CoinGold,
                    radius = cellSize * 0.34f,
                    center = foodCenter
                )

                // Draw Snake Segments
                snake.forEachIndexed { idx, pt ->
                    val pad = cellSize * 0.08f
                    val color = when {
                        idx == 0 -> RoyalBlue
                        idx % 2 == 0 -> VividPurple
                        else -> CyanAccent
                    }
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(pt.x * cellSize + pad, pt.y * cellSize + pad),
                        size = Size(cellSize - pad * 2, cellSize - pad * 2),
                        cornerRadius = CornerRadius(cellSize * 0.28f, cellSize * 0.28f)
                    )
                }
            }

            if (isGameOver) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { resetGame() },
                            modifier = Modifier.testTag("snake_play_again_button")
                        ) {
                            Text(strings.playAgain, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Tactile D-Pad Controls
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledIconButton(
                onClick = { queueDirection(SnakeDirection.UP) },
                modifier = Modifier
                    .size(56.dp)
                    .testTag("snake_dpad_up"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = PureWhite, modifier = Modifier.size(32.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { queueDirection(SnakeDirection.LEFT) },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("snake_dpad_left"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RoyalBlue)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = PureWhite, modifier = Modifier.size(32.dp))
                }
                FilledIconButton(
                    onClick = { queueDirection(SnakeDirection.RIGHT) },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("snake_dpad_right"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RoyalBlue)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = PureWhite, modifier = Modifier.size(32.dp))
                }
            }
            FilledIconButton(
                onClick = { queueDirection(SnakeDirection.DOWN) },
                modifier = Modifier
                    .size(56.dp)
                    .testTag("snake_dpad_down"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RoyalBlue)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = PureWhite, modifier = Modifier.size(32.dp))
            }
        }
    }
}
