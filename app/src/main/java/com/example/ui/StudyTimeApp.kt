package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.GameType
import com.example.data.local.TimerMode
import com.example.data.local.TimerStatus
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.components.AddSubjectDialog
import com.example.ui.components.EditGoalDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StudyTopBar
import com.example.ui.games.BlockPuzzleScreen
import com.example.ui.games.MemoryGameScreen
import com.example.ui.games.SnakeGameScreen
import com.example.ui.games.TicTacToeScreen
import com.example.ui.screens.GamesHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.StudyTimeTheme
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

object StudyRoutes {
    const val HOME = "home"
    const val TIMER = "timer"
    const val REPORTS = "reports"
    const val GAMES = "games"
    const val REWARDS = "rewards"
    const val GAME_SNAKE = "game_snake"
    const val GAME_TICTACTOE = "game_tictactoe"
    const val GAME_BLOCKS = "game_blocks"
    const val GAME_MEMORY = "game_memory"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun StudyTimeApp(
    state: MainUiState,
    viewModel: MainViewModel
) {
    val strings = stringsFor(state.language)

    CompositionLocalProvider(LocalStrings provides strings) {
        StudyTimeTheme(themeMode = state.themeMode) {
            val navController = rememberNavController()
            val navBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route ?: StudyRoutes.HOME

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(state.feedbackBanner) {
                val msg = state.feedbackBanner
                if (msg != null) {
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearFeedbackBanner()
                }
            }

            var showSettingsDialog by remember { mutableStateOf(false) }
            var showAddSubjectDialog by remember { mutableStateOf(false) }
            var showEditGoalDialog by remember { mutableStateOf(false) }

            val tabs = listOf(
                BottomTab(StudyRoutes.HOME, strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
                BottomTab(StudyRoutes.TIMER, strings.navTimer, Icons.Filled.Timer, Icons.Outlined.Timer, "nav_timer"),
                BottomTab(StudyRoutes.REPORTS, strings.navReports, Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_reports"),
                BottomTab(StudyRoutes.GAMES, strings.navGames, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, "nav_games"),
                BottomTab(StudyRoutes.REWARDS, strings.navRewards, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_rewards")
            )
            val showTopLevelChrome = currentRoute in tabs.map { it.route }

            fun navigateTab(route: String) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        if (showTopLevelChrome) {
                            val title = when (currentRoute) {
                                StudyRoutes.HOME -> strings.appTitle
                                StudyRoutes.TIMER -> strings.navTimer
                                StudyRoutes.REPORTS -> strings.reportsTitle
                                StudyRoutes.GAMES -> strings.gamesTitle
                                StudyRoutes.REWARDS -> strings.rewardsTitle
                                else -> strings.appTitle
                            }
                            StudyTopBar(
                                title = title,
                                totalCoins = state.totalCoins,
                                language = state.language,
                                themeMode = state.themeMode,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onOpenSettings = { showSettingsDialog = true }
                            )
                        }
                    },
                    bottomBar = {
                        if (showTopLevelChrome && !useNavRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                tabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (showTopLevelChrome && useNavRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                tabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label, maxLines = 1) },
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = StudyRoutes.HOME,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(StudyRoutes.HOME) {
                                HomeScreen(
                                    state = state,
                                    onSelectSubject = { viewModel.selectSubject(it) },
                                    onStartStudyNow = {
                                        if (state.timerStatus == TimerStatus.IDLE) {
                                            viewModel.switchTimerMode(TimerMode.STUDY)
                                            viewModel.startTimer()
                                        }
                                        navigateTab(StudyRoutes.TIMER)
                                    },
                                    onStartQuickBreak = {
                                        if (state.timerStatus == TimerStatus.IDLE) {
                                            viewModel.switchTimerMode(TimerMode.BREAK)
                                        }
                                        navigateTab(StudyRoutes.TIMER)
                                    },
                                    onOpenTimer = { navigateTab(StudyRoutes.TIMER) },
                                    onOpenGames = { navigateTab(StudyRoutes.GAMES) },
                                    onOpenAddSubject = { showAddSubjectDialog = true },
                                    onDeleteSession = { viewModel.deleteSession(it) }
                                )
                            }

                            composable(StudyRoutes.TIMER) {
                                TimerScreen(
                                    state = state,
                                    onSwitchMode = { viewModel.switchTimerMode(it) },
                                    onSelectSubject = { viewModel.selectSubject(it) },
                                    onOpenAddSubject = { showAddSubjectDialog = true },
                                    onSetBreakMinutes = { viewModel.setBreakDurationMinutes(it) },
                                    onStart = { viewModel.startTimer() },
                                    onPause = { viewModel.pauseTimer() },
                                    onResume = { viewModel.resumeTimer() },
                                    onStopAndSave = { viewModel.stopAndSaveTimer() },
                                    onDeleteSession = { viewModel.deleteSession(it) }
                                )
                            }

                            composable(StudyRoutes.REPORTS) {
                                ReportsScreen(
                                    state = state,
                                    onSelectPeriod = { viewModel.setReportPeriod(it) },
                                    onOpenGoalEditor = { showEditGoalDialog = true },
                                    onDeleteSession = { viewModel.deleteSession(it) }
                                )
                            }

                            composable(StudyRoutes.GAMES) {
                                GamesHubScreen(
                                    state = state,
                                    onLaunchGame = { type ->
                                        val route = when (type) {
                                            GameType.SNAKE -> StudyRoutes.GAME_SNAKE
                                            GameType.TICTACTOE -> StudyRoutes.GAME_TICTACTOE
                                            GameType.BLOCKS -> StudyRoutes.GAME_BLOCKS
                                            GameType.MEMORY -> StudyRoutes.GAME_MEMORY
                                        }
                                        navController.navigate(route)
                                    }
                                )
                            }

                            composable(StudyRoutes.REWARDS) {
                                RewardsScreen(state = state)
                            }

                            composable(StudyRoutes.GAME_SNAKE) {
                                SnakeGameScreen(
                                    bestScore = state.gameHighScores[GameType.SNAKE] ?: 0,
                                    onBack = { navController.popBackStack() },
                                    onGameFinished = { score ->
                                        viewModel.recordGameScore(GameType.SNAKE, score, won = score > 0)
                                    }
                                )
                            }

                            composable(StudyRoutes.GAME_TICTACTOE) {
                                TicTacToeScreen(
                                    onBack = { navController.popBackStack() },
                                    onMatchCompleted = { score, won ->
                                        viewModel.recordGameScore(GameType.TICTACTOE, score, won)
                                    }
                                )
                            }

                            composable(StudyRoutes.GAME_BLOCKS) {
                                BlockPuzzleScreen(
                                    bestScore = state.gameHighScores[GameType.BLOCKS] ?: 0,
                                    onBack = { navController.popBackStack() },
                                    onGameFinished = { score ->
                                        viewModel.recordGameScore(GameType.BLOCKS, score, won = score >= 100)
                                    }
                                )
                            }

                            composable(StudyRoutes.GAME_MEMORY) {
                                MemoryGameScreen(
                                    bestScore = state.gameHighScores[GameType.MEMORY] ?: 0,
                                    onBack = { navController.popBackStack() },
                                    onGameWon = { score ->
                                        viewModel.recordGameScore(GameType.MEMORY, score, won = true)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    currentLanguage = state.language,
                    currentThemeMode = state.themeMode,
                    currentGoalMinutes = state.dailyGoalMinutes,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onSelectTheme = { viewModel.setThemeMode(it) },
                    onOpenGoalEditor = { showEditGoalDialog = true },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            if (showAddSubjectDialog) {
                AddSubjectDialog(
                    onDismiss = { showAddSubjectDialog = false },
                    onConfirm = { name, colorHex, goalMins ->
                        viewModel.addSubject(name, colorHex, goalMins)
                    }
                )
            }

            if (showEditGoalDialog) {
                EditGoalDialog(
                    currentMinutes = state.dailyGoalMinutes,
                    onDismiss = { showEditGoalDialog = false },
                    onConfirm = { viewModel.setDailyGoalMinutes(it) }
                )
            }
        }
    }
}
