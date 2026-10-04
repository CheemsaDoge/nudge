package com.astraedus.nudge.ui.localization

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.astraedus.nudge.MainActivity
import com.astraedus.nudge.R
import com.astraedus.nudge.ui.screens.settings.LanguageSetting
import com.astraedus.nudge.ui.theme.NudgeTheme
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Exercises the actual settings picker, AppCompat recreation and non-Activity resource lookup. */
class LanguageSwitchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun showPicker() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent { NudgeTheme { LanguageSetting() } }
        }
        compose.waitForIdle()
        compose.onNodeWithText(compose.activity.getString(R.string.language_title)).performClick()
    }

    private fun awaitLanguage(language: String, expectedTitle: String) {
        compose.waitUntil(15_000) {
            var matches = false
            compose.activityRule.scenario.onActivity {
                matches = it.resources.configuration.locales[0].language == language &&
                    it.getString(R.string.language_title) == expectedTitle
            }
            matches
        }
        compose.waitForIdle()
    }

    @After fun resetToSystem() {
        compose.runOnUiThread { AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.getEmptyLocaleList()) }
    }

    @Test fun pickerChangesLanguagesSurvivesRecreationAndCanFollowSystemAgain() {
        for ((nativeName, tag, title) in listOf(
            Triple("简体中文", "zh", "语言"),
            Triple("日本語", "ja", "言語"),
            Triple("Français", "fr", "Langue"),
            Triple("English", "en", "Language")
        )) {
            showPicker()
            compose.onNodeWithText(nativeName).performClick()
            awaitLanguage(tag, title)
            compose.activityRule.scenario.recreate()
            awaitLanguage(tag, title)
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            assertEquals(title, ContextCompat.getContextForLanguage(context).getString(R.string.language_title))
        }
        showPicker()
        compose.onNodeWithText(compose.activity.getString(R.string.language_system)).performClick()
        compose.waitUntil(15_000) { AppCompatDelegate.getApplicationLocales().isEmpty }
    }
}
