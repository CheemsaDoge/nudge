package com.astraedus.nudge.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.astraedus.nudge.R

/** Native names let users recover even after selecting a language they cannot read. */
internal enum class AppLanguage(val tag: String, val labelRes: Int) {
    SYSTEM("", R.string.language_system),
    ENGLISH("en", R.string.language_english),
    CHINESE("zh-Hans", R.string.language_chinese),
    JAPANESE("ja", R.string.language_japanese),
    FRENCH("fr", R.string.language_french);

    companion object {
        fun fromTag(tag: String): AppLanguage = when (tag.substringBefore(',').substringBefore('-').lowercase(java.util.Locale.ROOT)) {
            "en" -> ENGLISH
            "zh" -> CHINESE
            "ja" -> JAPANESE
            "fr" -> FRENCH
            else -> SYSTEM
        }
    }
}

@Composable
internal fun LanguageSetting() {
    // Also re-read after a language change made in Android's per-app settings.
    LocalConfiguration.current
    val selected = AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags())
    var showPicker by rememberSaveable { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(stringResource(R.string.language_title)) },
        supportingContent = { Text(stringResource(selected.labelRes)) },
        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
        modifier = Modifier.clickable { showPicker = true }
    )
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(R.string.language_title)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { language ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = selected == language,
                                role = Role.RadioButton,
                                onClick = {
                                    showPicker = false
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(language.tag)
                                    )
                                }
                            ).padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = selected == language, onClick = null)
                            Text(stringResource(language.labelRes), Modifier.padding(start = 12.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.ui_cancel)) }
            }
        )
    }
}
