package com.astraedus.nudge.ui.screens.onboarding

import com.astraedus.nudge.R

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astraedus.nudge.ui.components.AccessibilityDisclosureDialog
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> PermissionsPage(
                    onGrantAccessibility = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onGrantOverlay = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    },
                    onGrantUsageStats = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
                2 -> ReadyPage()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.getString(R.string.ui_next))
                }
            } else {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.getString(R.string.ui_get_started))
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.SelfImprovement,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "Nudge",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            strings.getString(R.string.ui_mindful_app_usage_through_gentle_friction),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            strings.getString(R.string.ui_take_back_control_of_your_screen_time_with_breathing_exercises_delays),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionsPage(
    onGrantAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantUsageStats: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onConfirm = {
                showAccessibilityDisclosure = false
                onGrantAccessibility()
            },
            onDismiss = {
                showAccessibilityDisclosure = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            strings.getString(R.string.ui_permissions),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            strings.getString(R.string.ui_nudge_needs_these_permissions_to_work_no_data_is_ever_sent_anywhere_th),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        PermissionCard(
            icon = Icons.Outlined.Accessibility,
            title = strings.getString(R.string.ui_accessibility_service),
            description = strings.getString(R.string.ui_detects_which_app_is_in_the_foreground_so_nudge_can_trigger_your_block),
            onClick = { showAccessibilityDisclosure = true }
        )

        Spacer(Modifier.height(12.dp))

        PermissionCard(
            icon = Icons.Outlined.Layers,
            title = strings.getString(R.string.ui_display_over_other_apps),
            description = strings.getString(R.string.ui_shows_the_delay_countdown_or_breathing_exercise_overlay_on_top_of_bloc),
            onClick = onGrantOverlay
        )

        Spacer(Modifier.height(12.dp))

        PermissionCard(
            icon = Icons.Outlined.QueryStats,
            title = strings.getString(R.string.ui_usage_stats_access),
            description = strings.getString(R.string.ui_tracks_how_long_you_use_each_app_per_day_to_enforce_daily_time_budgets),
            onClick = onGrantUsageStats
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(strings.getString(R.string.ui_grant_permission))
            }
        }
    }
}

@Composable
private fun ReadyPage() {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            strings.getString(R.string.ui_you_re_all_set),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            strings.getString(R.string.ui_add_apps_to_start_building_healthier_habits),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
