package com.example

import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches Study Time`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Study Time", appName)
    }

    @Test
    fun `navigate tabs toggle hindi and verify timer and games`() {
        composeTestRule.waitForIdle()

        // Home dashboard shows start studying button
        composeTestRule.onNodeWithTag("home_start_studying_button").assertIsDisplayed()

        // Toggle language to Hindi and wait for DataStore IO update
        composeTestRule.onNodeWithTag("language_toggle_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("टाइमर").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("टाइमर").assertIsDisplayed()

        // Toggle back to English
        composeTestRule.onNodeWithTag("language_toggle_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Timer").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Timer").assertIsDisplayed()

        // Navigate to Timer tab, Start -> Pause -> Resume -> Stop
        composeTestRule.onNodeWithTag("nav_timer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("timer_start_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("timer_pause_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("timer_pause_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("timer_resume_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("timer_resume_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("timer_stop_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("timer_stop_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("timer_start_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate to Reports tab and switch periods
        composeTestRule.onNodeWithTag("nav_reports").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("report_period_weekly").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("report_period_monthly").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Games Hub, open Snake first, then Tic Tac Toe
        composeTestRule.onNodeWithTag("nav_games").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("game_play_snake").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("game_play_snake").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("snake_back_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("snake_back_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("game_play_tictactoe").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("game_play_tictactoe").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("ttt_cell_0").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("ttt_cell_0").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("X").fetchSemanticsNodes().size >= 2
        }
        composeTestRule.onNodeWithTag("ttt_cell_0").assert(hasText("X"))
        composeTestRule.onNodeWithTag("ttt_back_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("nav_rewards").fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate to Rewards tab
        composeTestRule.onNodeWithTag("nav_rewards").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("rewards_total_coins").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("rewards_total_coins").assertIsDisplayed()
    }
}
