package com.example.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.theme.CoinGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.VividPurple
import kotlinx.coroutines.delay

data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

object MemoryGameEngine {
    fun createDeck(pairCount: Int): List<MemoryCard> {
        val pairs = (0 until pairCount).flatMap { pairId ->
            listOf(pairId, pairId)
        }.shuffled()
        return pairs.mapIndexed { idx, pairId ->
            MemoryCard(id = idx, pairId = pairId)
        }
    }
}

private data class MemorySymbol(val icon: ImageVector, val color: Color)

private val MEMORY_SYMBOLS = listOf(
    MemorySymbol(Icons.Default.MenuBook, RoyalBlue),
    MemorySymbol(Icons.Default.EmojiEvents, CoinGold),
    MemorySymbol(Icons.Default.RocketLaunch, VividPurple),
    MemorySymbol(Icons.Default.Science, EmeraldSuccess),
    MemorySymbol(Icons.Default.Lightbulb, StreakOrange),
    MemorySymbol(Icons.Default.MusicNote, Color(0xFFEC4899)),
    MemorySymbol(Icons.Default.Bolt, CyanAccent),
    MemorySymbol(Icons.Default.Favorite, Color(0xFFEF4444)),
    MemorySymbol(Icons.Default.Extension, Color(0xFF4F46E5)),
    MemorySymbol(Icons.Default.Alarm, Color(0xFF0EA5E9))
)

@Composable
fun MemoryGameScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onGameWon: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var pairCount by remember { mutableIntStateOf(8) } // 8 = 4x4, 10 = 4x5
    var cards by remember { mutableStateOf(MemoryGameEngine.createDeck(8)) }
    var firstFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var secondFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var winSubmitted by remember { mutableStateOf(false) }

    val matchedPairs = cards.count { it.isMatched } / 2
    val hasWon = matchedPairs == pairCount

    fun resetDeck(newPairs: Int = pairCount) {
        pairCount = newPairs
        cards = MemoryGameEngine.createDeck(newPairs)
        firstFlippedIndex = null
        secondFlippedIndex = null
        moves = 0
        isLocked = false
        winSubmitted = false
    }

    LaunchedEffect(secondFlippedIndex) {
        val firstIdx = firstFlippedIndex
        val secondIdx = secondFlippedIndex
        if (firstIdx != null && secondIdx != null) {
            isLocked = true
            val firstCard = cards[firstIdx]
            val secondCard = cards[secondIdx]
            if (firstCard.pairId == secondCard.pairId) {
                cards = cards.mapIndexed { idx, card ->
                    if (idx == firstIdx || idx == secondIdx) card.copy(isMatched = true) else card
                }
                firstFlippedIndex = null
                secondFlippedIndex = null
                isLocked = false
            } else {
                delay(650L)
                cards = cards.mapIndexed { idx, card ->
                    if (idx == firstIdx || idx == secondIdx) card.copy(isFaceUp = false) else card
                }
                firstFlippedIndex = null
                secondFlippedIndex = null
                isLocked = false
            }
        }
    }

    LaunchedEffect(hasWon) {
        if (hasWon && !winSubmitted) {
            winSubmitted = true
            val calculatedScore = (pairCount * 25 - moves * 3).coerceAtLeast(20)
            onGameWon(calculatedScore)
        }
    }

    fun onCardTap(index: Int) {
        if (isLocked || hasWon) return
        val card = cards[index]
        if (card.isFaceUp || card.isMatched) return

        cards = cards.mapIndexed { idx, c -> if (idx == index) c.copy(isFaceUp = true) else c }
        if (firstFlippedIndex == null) {
            firstFlippedIndex = index
        } else if (secondFlippedIndex == null) {
            secondFlippedIndex = index
            moves += 1
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("memory_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.gameMemoryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = pairCount == 8,
                    onClick = { resetDeck(8) },
                    label = { Text(strings.grid4x4) }
                )
                FilterChip(
                    selected = pairCount == 10,
                    onClick = { resetDeck(10) },
                    label = { Text(strings.grid4x5) }
                )
                IconButton(onClick = { resetDeck(pairCount) }, modifier = Modifier.testTag("memory_reset_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.reset)
                }
            }
        }

        // Stats Row
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
                    Text(strings.moves, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$moves",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.testTag("memory_moves_text")
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
                    Text(strings.pairsFound, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$matchedPairs/$pairCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }
        }

        if (hasWon) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = EmeraldSuccess.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.youWon,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "${strings.moves}: $moves",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(onClick = { resetDeck(pairCount) }) {
                        Text(strings.playAgain, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card Grid (4 columns x 4 or 5 rows)
        val rows = cards.chunked(4)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEachIndexed { rowIndex, rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCards.forEachIndexed { colIndex, card ->
                        val cardIndex = rowIndex * 4 + colIndex
                        MemoryCardTile(
                            card = card,
                            symbol = MEMORY_SYMBOLS[card.pairId % MEMORY_SYMBOLS.size],
                            onClick = { onCardTap(cardIndex) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .testTag("memory_card_$cardIndex")
                        )
                    }
                }
            }
        }

        if (bestScore > 0) {
            Text(
                text = "${strings.highScore}: $bestScore",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemoryCardTile(
    card: MemoryCard,
    symbol: MemorySymbol,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val revealed = card.isFaceUp || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "cardFlip"
    )
    val showFront = rotation > 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    card.isMatched -> symbol.color.copy(alpha = 0.22f)
                    showFront -> MaterialTheme.colorScheme.surface
                    else -> RoyalBlue
                }
            )
            .border(
                width = 2.dp,
                color = when {
                    card.isMatched -> EmeraldSuccess
                    showFront -> symbol.color
                    else -> VividPurple
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = !revealed, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (showFront) {
            Icon(
                imageVector = symbol.icon,
                contentDescription = null,
                tint = symbol.color,
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer { rotationY = 180f }
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PureWhite.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
