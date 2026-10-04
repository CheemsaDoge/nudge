package com.astraedus.nudge.ui.nuke

import com.astraedus.nudge.ui.localization.builtInCopy

import com.astraedus.nudge.R

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astraedus.nudge.domain.nuke.NukeEmergencyCode
import com.astraedus.nudge.ui.components.ChallengeDialog
import com.astraedus.nudge.ui.qr.ScanQrContract

/** Whether this device has any camera at all. A phone without one gets null from every scan. */
fun hasAnyCamera(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

/**
 * The dialog every Nuke-weakening action goes through: scan the paired key, or type a fresh
 * 64-character emergency code. Renders nothing while [state] is null.
 *
 * Owns its own camera launcher, so any screen whose ViewModel holds a [NukeGate] gets the whole
 * flow from one call (the Home screen's master toggle, the Nuke screen). The scan result goes to
 * [onScanned]; the gate decides whether it was the key.
 *
 * The emergency half REUSES [ChallengeDialog] (paste suppressed, dash-insensitive, case-sensitive,
 * live x/y counter) with a Nuke title and a fresh [NukeEmergencyCode] per attempt.
 */
@Composable
fun NukeUnlockHost(
    state: NukeUnlockState?,
    onScanned: (String?) -> Unit,
    onUseEmergencyCode: () -> Unit,
    onVerifyEmergency: (String) -> Unit,
    onBackToChoice: () -> Unit,
    onCancel: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val scanner = rememberLauncherForActivityResult(ScanQrContract()) { payload -> onScanned(payload) }
    val context = LocalContext.current
    val hasCamera = remember { hasAnyCamera(context) }
    val active = state ?: return

    val target = active.emergencyTarget
    if (target != null) {
        ChallengeDialog(
            target = target,
            title = strings.getString(R.string.ui_emergency_end_nuke),
            prompt = strings.getString(R.string.ui_no_code_on_you_type_all_characters_below_exactly_every_attempt_gets_a, strings.builtInCopy(active.prompt), NukeEmergencyCode.LENGTH),
            onUnlock = onVerifyEmergency,
            onCancel = onBackToChoice,
            confirmLabel = strings.getString(R.string.ui_end_it)
        )
        return
    }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null) },
        title = { Text(strings.getString(R.string.ui_nuke_is_on)) },
        text = {
            Column {
                Text(
                    if (hasCamera) {
                        strings.getString(R.string.ui_that_needs_your_nuke_code_go_and_get_it_and_scan_it_here, strings.builtInCopy(active.prompt))
                    } else {
                        strings.getString(R.string.ui_that_needs_your_nuke_code_and_this_phone_has_no_camera_to_scan_it_with, strings.builtInCopy(active.prompt))
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                active.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(strings.builtInCopy(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onUseEmergencyCode) {
                    Text(strings.getString(R.string.ui_no_code_on_you_type_the_emergency_code))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = hasCamera,
                onClick = {
                    scanner.launch(ScanQrContract.Request(title = strings.getString(R.string.ui_scan_your_nuke_code), subtitle = strings.builtInCopy(active.prompt)))
                }
            ) {
                Text(strings.getString(R.string.ui_scan_code))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(strings.getString(R.string.ui_never_mind)) }
        }
    )
}
