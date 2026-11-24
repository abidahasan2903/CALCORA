package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.engine.Routes
import com.example.model.CalcoraThemeId
import com.example.navigation.CalcoraApp
import com.example.ui.theme.CalcoraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalcoraUiAndNavigationTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private fun createViewModel(): CalcoraViewModel {
    val app = ApplicationProvider.getApplicationContext<Application>()
    return CalcoraViewModel(app)
  }

  @Test
  fun `splash transitions to home and displays worlds and developer credit`() {
    val vm = createViewModel()
    composeTestRule.setContent {
      CalcoraTheme {
        CalcoraApp(viewModel = vm, startDestination = Routes.SPLASH, splashDurationMs = 50L)
      }
    }
    composeTestRule.onNodeWithTag("splash_screen").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Choose Your Calculation World").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_science"))
    composeTestRule.onNodeWithTag("world_card_science").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_commerce"))
    composeTestRule.onNodeWithTag("world_card_commerce").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_arts"))
    composeTestRule.onNodeWithTag("world_card_arts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_universal"))
    composeTestRule.onNodeWithTag("world_card_universal").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("developer_credit"))
    composeTestRule.onNodeWithTag("developer_credit").assertIsDisplayed()
  }

  @Test
  fun `navigation covers all four worlds, all tools, history, and settings to themes persistence`() {
    val vm = createViewModel()
    composeTestRule.setContent {
      val prefs = vm.preferences.value
      CalcoraTheme(themeId = prefs.themeId, appearanceMode = prefs.appearanceMode) {
        CalcoraApp(viewModel = vm, startDestination = Routes.HOME)
      }
    }
    composeTestRule.waitForIdle()

    // Home -> Science Calculator -> perform calculation -> Back
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_science"))
    composeTestRule.onNodeWithTag("world_card_science").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sci_key_7").performClick()
    composeTestRule.onNodeWithTag("sci_key_equals").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("top_bar_back").performClick()
    composeTestRule.waitForIdle()

    // Home -> Commerce -> Back
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_commerce"))
    composeTestRule.onNodeWithTag("world_card_commerce").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Commerce Calculator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_bar_back").performClick()
    composeTestRule.waitForIdle()

    // Home -> Arts -> Back
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_arts"))
    composeTestRule.onNodeWithTag("world_card_arts").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Arts & General Calculator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_bar_back").performClick()
    composeTestRule.waitForIdle()

    // Home -> Universal -> Birthday & Age -> Calculate -> Back
    composeTestRule.onNodeWithTag("home_lazy_column").performScrollToNode(hasTestTag("world_card_universal"))
    composeTestRule.onNodeWithTag("world_card_universal").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("uni_tool_uni_age").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("age_calculate_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("age_lazy_column").performScrollToNode(hasTestTag("age_result_card"))
    composeTestRule.onNodeWithTag("age_result_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_bar_back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("top_bar_back").performClick()
    composeTestRule.waitForIdle()

    // Bottom Nav -> Tools -> History (populated & clearable) -> Settings -> Themes
    composeTestRule.onNodeWithTag("nav_${Routes.TOOLS}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("All Tools Directory").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_${Routes.HISTORY}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Session History").assertIsDisplayed()
    assertEquals(true, vm.historyEntries.value.isNotEmpty())
    composeTestRule.onNodeWithTag("clear_history_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(true, vm.historyEntries.value.isEmpty())
    composeTestRule.onNodeWithTag("history_empty_state").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_${Routes.SETTINGS}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_open_themes").performClick()
    composeTestRule.waitForIdle()

    // Verify all 7 themes are selectable and FOX_FINANCE persists
    for (theme in CalcoraThemeId.entries) {
      composeTestRule.onNodeWithTag("themes_lazy_column").performScrollToNode(hasTestTag("theme_card_${theme.id}"))
      composeTestRule.onNodeWithTag("theme_card_${theme.id}").performClick()
      composeTestRule.waitForIdle()
      assertEquals(theme, vm.preferences.value.themeId)
    }

    composeTestRule.onNodeWithTag("themes_lazy_column").performScrollToNode(hasTestTag("theme_card_fox_finance"))
    composeTestRule.onNodeWithTag("theme_card_fox_finance").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CalcoraThemeId.FOX_FINANCE, vm.preferences.value.themeId)

    // Verify preferences persist across new repository instance
    val vm2 = createViewModel()
    assertEquals(CalcoraThemeId.FOX_FINANCE, vm2.preferences.value.themeId)
  }

  @Test
  fun `smart search evaluates natural language query and opens target calculator`() {
    val vm = createViewModel()
    composeTestRule.setContent {
      CalcoraTheme {
        CalcoraApp(viewModel = vm, startDestination = Routes.HOME)
      }
    }
    composeTestRule.onNodeWithTag("home_search_input").performTextInput("Calculate 15% of 500")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("smart_quick_answer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("smart_quick_answer").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Percentage Calculator").assertIsDisplayed()
  }
}
