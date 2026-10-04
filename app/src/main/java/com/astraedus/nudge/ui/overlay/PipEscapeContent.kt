package com.astraedus.nudge.ui.overlay

import com.astraedus.nudge.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Explains why a block was defeated by picture-in-picture and, when possible, deep-links the user
 * to the setting that fixes it for this one app. See [PipEscapeActivity] for the full issue #19
 * writeup — this composable is pure presentation, no Android framework calls.
 */
@Composable
fun PipEscapeContent(
    appLabel: String?,
    packageName: String,
    canOpenSettings: Boolean,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    // Two different fallbacks on purpose. The heading needs a title when the label is unresolvable;
    // the sentences need a SUBJECT, and reusing the heading's fallback there reads as nonsense
    // ("Picture-in-picture kept playing in a picture-in-picture window").
    val heading = appLabel ?: strings.getString(R.string.ui_picture_in_picture)
    val subject = appLabel ?: strings.getString(R.string.ui_this_app)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // targetSdk 36 enforces edge-to-edge with no opt-out, so the window now spans
                // under the status and navigation bars. The Surface above stays full-bleed (the
                // block must cover every pixel of the app behind it); only the CONTENT is inset.
                .safeDrawingPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PictureInPicture,
                contentDescription = strings.getString(R.string.ui_picture_in_picture),
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = heading,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.getString(R.string.ui_kept_playing_in_a_floating_picture_in_picture_window_so_nudge_s_block, subject),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.getString(R.string.ui_turn_off_picture_in_picture_for_and_nudge_can_block_it_properly, subject),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (canOpenSettings) {
                Button(onClick = onOpenSettings) {
                    Text(strings.getString(R.string.ui_open_picture_in_picture_settings))
                }
            } else {
                Text(
                    text = strings.getString(R.string.ui_settings_apps_special_app_access_picture_in_picture),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text(strings.getString(R.string.ui_not_now_2))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.getString(R.string.ui_nudge_only_shows_this_once_for_each_app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
