package com.astraedus.nudge.ui.screens.settings

import com.astraedus.nudge.R

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.astraedus.nudge.ui.hasGrayscalePermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrayscaleGuideScreen(
    onNavigateBack: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val context = LocalContext.current
    val permissionGranted by remember { mutableStateOf(hasGrayscalePermission(context)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.getString(R.string.ui_grayscale_mode_setup)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.getString(R.string.ui_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Status indicator
            PermissionStatusCard(granted = permissionGranted)

            if (permissionGranted) {
                // Permission already granted -- show success only
                Spacer(Modifier.height(16.dp))
            } else {
                // Explanation
                Text(
                    strings.getString(R.string.ui_grayscale_mode_makes_your_phone_screen_black_and_white_when_blocked_ap),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    strings.getString(R.string.ui_this_requires_a_one_time_setup_because_android_restricts_apps_from_cha),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Wireless
                SectionHeader(strings.getString(R.string.ui_option_1_wireless_android_11_no_computer_needed))

                NumberedStep(1, strings.getString(R.string.ui_go_to_settings_about_phone_tap_build_number_7_times_to_enable_develope))
                NumberedStep(2, strings.getString(R.string.ui_go_to_settings_developer_options_enable_wireless_debugging))
                NumberedStep(3, strings.getString(R.string.ui_tap_pair_device_with_pairing_code_and_note_the_pairing_code_and_port))
                NumberedStep(4, strings.getString(R.string.ui_open_a_terminal_app_like_termux_and_run_the_pairing_command))

                AdbCommandCard(
                    command = "adb pair <ip>:<port>",
                    label = strings.getString(R.string.ui_then_enter_the_pairing_code_when_prompted),
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    context = context
                )

                NumberedStep(5, strings.getString(R.string.ui_then_run_this_command_to_grant_the_permission))

                AdbCommandCard(
                    command = "adb shell pm grant com.astraedus.nudge android.permission.WRITE_SECURE_SETTINGS",
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    context = context
                )

                Spacer(Modifier.height(8.dp))

                // Option 2: With a Computer
                SectionHeader(strings.getString(R.string.ui_option_2_with_a_computer))

                NumberedStep(1, strings.getString(R.string.ui_install_adb_on_your_computer_search_install_adb_for_your_os))
                NumberedStep(2, strings.getString(R.string.ui_enable_usb_debugging_on_your_phone_settings_developer_options_usb_debu))
                NumberedStep(3, strings.getString(R.string.ui_connect_your_phone_via_usb))
                NumberedStep(4, strings.getString(R.string.ui_run_this_command_on_your_computer))

                AdbCommandCard(
                    command = "adb shell pm grant com.astraedus.nudge android.permission.WRITE_SECURE_SETTINGS",
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    context = context
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    strings.getString(R.string.ui_that_s_it_one_time_setup_grayscale_will_work_automatically_after_this),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionStatusCard(granted: Boolean) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) {
                Color(0xFF1B5E20).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (granted) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Column {
                    Text(
                        strings.getString(R.string.ui_permission_granted),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        strings.getString(R.string.ui_grayscale_mode_is_ready_to_use_enable_it_per_app_in_rule_settings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    strings.getString(R.string.ui_not_granted),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    strings.getString(R.string.ui_follow_the_guide_below_to_enable_grayscale),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdbCommandCard(
    command: String,
    label: String? = null,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                command,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(strings.getString(R.string.ui_adb_command), command))
                scope.launch {
                    snackbarHostState.showSnackbar(strings.getString(R.string.ui_copied_to_clipboard))
                }
            }) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = strings.getString(R.string.ui_copy_command),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
        }
    }
}
