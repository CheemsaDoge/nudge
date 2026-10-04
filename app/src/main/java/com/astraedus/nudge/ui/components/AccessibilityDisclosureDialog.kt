package com.astraedus.nudge.ui.components

import com.astraedus.nudge.R

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Prominent disclosure dialog shown BEFORE requesting the Accessibility Service permission.
 *
 * Required by Google Play policy. The dialog explains:
 * - WHY the service is needed (detect foreground apps)
 * - WHAT data is accessed (package names only)
 * - HOW data is used (locally, never sent anywhere)
 *
 * Back press and tapping outside dismiss the dialog WITHOUT granting consent.
 */
@Composable
fun AccessibilityDisclosureDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(strings.getString(R.string.ui_how_nudge_works))
        },
        text = {
            Column {
                Text(
                    strings.getString(R.string.ui_nudge_uses_android_s_accessibility_service_to_detect_which_app_is_curr),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    strings.getString(R.string.ui_this_lets_nudge_show_breathing_exercises_delays_or_blocks_when_you_ope),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    strings.getString(R.string.ui_nudge_only_reads_the_name_of_the_app_in_the_foreground_it_does_not_rea),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    strings.getString(R.string.ui_all_data_stays_on_your_device_nudge_has_no_internet_permission_and_can),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(strings.getString(R.string.ui_i_understand))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.getString(R.string.ui_not_now))
            }
        }
    )
}
