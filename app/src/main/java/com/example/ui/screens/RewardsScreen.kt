package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AchievementBadge
import com.example.data.local.BadgeId
import com.example.i18n.AppStrings
import com.example.i18n.LocalStrings
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DeepIndigo
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.VividPurple
import com.example.ui.viewmodel.MainUiState

@Composable
fun RewardsScreen(
    state: MainUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trophy & Coins Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(DeepIndigo, VividPurple, RoyalBlue))
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_rewards_banner),
                        contentDescription = strings.rewardsTitle,
                        contentScale = ContentScale.Crop,
                        alpha = 0.34f,
                        modifier = Modifier.matchParentSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.totalCoins,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite.copy(alpha = 0.88f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = CoinGold,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${state.totalCoins}",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite,
                                    modifier = Modifier.testTag("rewards_total_coins")
                                )
                            }
                            Text(
                                text = strings.earnCoinsHint,
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Current & Best Streak Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(StreakOrange.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = StreakOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${state.currentStreak} ${strings.daysUnit}",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.currentStreak,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CoinGold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CoinGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${state.bestStreak} ${strings.daysUnit}",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.bestStreak,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Weekly Streak Strip
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.weeklyStreak,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        strings.weekdayLabels.forEachIndexed { idx, label ->
                            val active = idx in state.weeklyActiveDays
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (active) EmeraldSuccess
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (active) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = label,
                                            tint = PureWhite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Achievement Badges
        item {
            val unlockedCount = state.badges.count { it.unlocked }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.badgesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$unlockedCount / ${state.badges.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Two-column badge pairs
        val rows = state.badges.chunked(2)
        items(rows.size) { rowIndex ->
            val pair = rows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        strings = strings,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun BadgeCard(
    badge: AchievementBadge,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val (title, desc, icon, tint) = badgeMeta(badge.id, strings)
    Card(
        modifier = modifier
            .alpha(if (badge.unlocked) 1f else 0.78f)
            .testTag("badge_${badge.id.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (badge.unlocked) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (badge.unlocked) tint.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (badge.unlocked) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = if (badge.unlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = if (badge.unlocked) strings.unlocked else strings.locked,
                    tint = if (badge.unlocked) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            LinearProgressIndicator(
                progress = { badge.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (badge.unlocked) EmeraldSuccess else tint,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private data class BadgeMeta(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val tint: Color
)

private fun badgeMeta(id: BadgeId, strings: AppStrings): BadgeMeta = when (id) {
    BadgeId.FIRST_STEP -> BadgeMeta(
        strings.badgeFirstStepTitle,
        strings.badgeFirstStepDesc,
        Icons.Default.Star,
        RoyalBlue
    )
    BadgeId.DEEP_FOCUS -> BadgeMeta(
        strings.badgeDeepFocusTitle,
        strings.badgeDeepFocusDesc,
        Icons.Default.Bolt,
        VividPurple
    )
    BadgeId.GOAL_CRUSHER -> BadgeMeta(
        strings.badgeGoalCrusherTitle,
        strings.badgeGoalCrusherDesc,
        Icons.Default.Flag,
        EmeraldSuccess
    )
    BadgeId.STREAK_3 -> BadgeMeta(
        strings.badgeStreak3Title,
        strings.badgeStreak3Desc,
        Icons.Default.LocalFireDepartment,
        StreakOrange
    )
    BadgeId.STREAK_7 -> BadgeMeta(
        strings.badgeStreak7Title,
        strings.badgeStreak7Desc,
        Icons.Default.WorkspacePremium,
        CoinGold
    )
    BadgeId.EXPLORER -> BadgeMeta(
        strings.badgeExplorerTitle,
        strings.badgeExplorerDesc,
        Icons.Default.Explore,
        Color(0xFF06B6D4)
    )
    BadgeId.COIN_100 -> BadgeMeta(
        strings.badgeRich100Title,
        strings.badgeRich100Desc,
        Icons.Default.MonetizationOn,
        CoinGold
    )
    BadgeId.GAMER -> BadgeMeta(
        strings.badgeGamerTitle,
        strings.badgeGamerDesc,
        Icons.Default.SportsEsports,
        Color(0xFFEC4899)
    )
}
