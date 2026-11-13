package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GameType
import com.example.data.local.PreferencesStore
import com.example.data.local.ReportPeriod
import com.example.data.local.StudyDatabase
import com.example.data.local.SubjectEntity
import com.example.data.repository.StudyRepository
import com.example.ui.games.BlockPiece
import com.example.ui.games.BlockPuzzleEngine
import com.example.ui.games.GridPoint
import com.example.ui.games.MemoryGameEngine
import com.example.ui.games.SnakeDirection
import com.example.ui.games.SnakeEngine
import com.example.ui.games.TicTacToeEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyEngineUnitTest {

    private lateinit var db: StudyDatabase
    private lateinit var repository: StudyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, StudyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StudyRepository(db.studyDao(), PreferencesStore(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `repository records study session awards coins updates streak and reports`() = runTest {
        repository.ensureDefaultSubjectsSeeded()
        val subjects = repository.subjectsFlow.first()
        assertTrue(subjects.size >= 6)

        val math: SubjectEntity = subjects.first()
        val earned = repository.recordCompletedSession(
            subject = math,
            durationSeconds = 1800L, // 30 mins
            isBreak = false,
            startedAtEpochMs = System.currentTimeMillis() - 1_800_000L
        )
        assertTrue(earned >= 60)

        val sessions = repository.sessionsFlow.first()
        assertEquals(1, sessions.size)
        assertEquals(1800L, sessions.first().durationSeconds)

        val prefs = repository.preferencesFlow.first()
        assertTrue(prefs.totalCoins >= earned)
        assertEquals(1, prefs.currentStreak)
        assertTrue("first_step" in prefs.unlockedBadges)

        val dailyBuckets = StudyRepository.buildReportBuckets(sessions, ReportPeriod.DAILY)
        assertEquals(7, dailyBuckets.size)
        assertEquals(1800L, dailyBuckets.last().totalSeconds)

        val breakdown = StudyRepository.buildSubjectBreakdown(sessions, ReportPeriod.DAILY)
        assertEquals(1, breakdown.size)
        assertEquals(1f, breakdown.first().percentage, 0.01f)
    }

    @Test
    fun `snake engine moves eats food and detects wall collision`() {
        val snake = listOf(GridPoint(5, 5), GridPoint(4, 5), GridPoint(3, 5))
        val food = GridPoint(6, 5)
        val ate = SnakeEngine.step(snake, SnakeDirection.RIGHT, food)
        assertFalse(ate.collided)
        assertTrue(ate.ateFood)
        assertEquals(4, ate.snake.size)
        assertEquals(GridPoint(6, 5), ate.snake.first())

        val wallSnake = listOf(GridPoint(0, 0), GridPoint(1, 0))
        val hitWall = SnakeEngine.step(wallSnake, SnakeDirection.LEFT, GridPoint(5, 5))
        assertTrue(hitWall.collided)
    }

    @Test
    fun `tic tac toe engine detects winner and smart AI blocks player`() {
        val winningBoard = listOf(
            "X", "X", "X",
            "O", "O", "",
            "", "", ""
        )
        assertEquals("X", TicTacToeEngine.checkWinner(winningBoard).winner)

        // X threatens top row; smart AI ('O') must block index 2
        val threatBoard = listOf(
            "X", "X", "",
            "", "O", "",
            "", "", ""
        )
        val move = TicTacToeEngine.pickAiMove(threatBoard, smart = true)
        assertEquals(2, move)
    }

    @Test
    fun `block puzzle clears full row and calculates points`() {
        val board = BlockPuzzleEngine.emptyBoard().toMutableList()
        for (c in 0 until 7) {
            board[c] = 0xFF2563EB
        }
        val singlePiece = BlockPiece(id = 1, cells = listOf(0 to 0), colorHex = 0xFF7C3AED)
        assertTrue(BlockPuzzleEngine.canPlace(board, singlePiece, 0, 7))
        val res = BlockPuzzleEngine.placeAndClear(board, singlePiece, 0, 7)
        assertEquals(1, res.linesCleared)
        assertEquals(0L, res.board[0])
        assertEquals(0L, res.board[7])
    }

    @Test
    fun `memory game creates exact pair deck`() {
        val deck4x4 = MemoryGameEngine.createDeck(8)
        assertEquals(16, deck4x4.size)
        val grouped = deck4x4.groupBy { it.pairId }
        assertEquals(8, grouped.size)
        assertTrue(grouped.values.all { it.size == 2 })
    }

    @Test
    fun `game score recording awards coins`() = runTest {
        val coinsBefore = repository.preferencesFlow.first().totalCoins
        val earned = repository.recordGameResult(GameType.SNAKE, score = 12, won = true)
        assertTrue(earned > 0)
        val coinsAfter = repository.preferencesFlow.first().totalCoins
        assertEquals(coinsBefore + earned, coinsAfter)
    }
}
