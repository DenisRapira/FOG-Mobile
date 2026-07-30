package dev.fogmobile.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.fogmobile.core.ConnectionUiState
import dev.fogmobile.ui.theme.FogTheme
import org.junit.Rule
import org.junit.Test

class DashboardSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun dashboardShowsFogWordmark() {
        compose.setContent {
            FogTheme {
                DashboardScreen(ConnectionUiState(onboardingComplete = true), {}, {}, {})
            }
        }

        compose.onNodeWithText("FOG").assertIsDisplayed()
        compose.onNodeWithText("Instagram").assertIsDisplayed()
        compose.onNodeWithText("YouTube").assertIsDisplayed()
    }
}
