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
import com.astraedus.nudge.ui.screens.settings.SettingsScreen
import com.astraedus.nudge.ui.theme.NudgeTheme
import android.graphics.Bitmap
import android.os.Build
import java.io.File
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Exercises the actual settings picker, AppCompat recreation and non-Activity resource lookup. */
class LanguageSwitchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun grantNotificationsOnModernAndroid() {
        if (Build.VERSION.SDK_INT >= 33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant dev.astraedus.nudge android.permission.POST_NOTIFICATIONS"
            ).use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).readBytes() }
        }
    }

    private fun screenshot(tag: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "language-screenshots").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(folder, "$tag.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun showPicker() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent { NudgeTheme { SettingsScreen(onNavigateBack = {}) } }
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
            showPicker()
            screenshot(tag)
            compose.onNodeWithText(compose.activity.getString(R.string.ui_cancel)).performClick()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            assertEquals(title, ContextCompat.getContextForLanguage(context).getString(R.string.language_title))
        }
        showPicker()
        compose.onNodeWithText(compose.activity.getString(R.string.language_system)).performClick()
        compose.waitUntil(15_000) { AppCompatDelegate.getApplicationLocales().isEmpty }
    }
}
