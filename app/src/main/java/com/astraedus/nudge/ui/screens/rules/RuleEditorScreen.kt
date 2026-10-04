package com.astraedus.nudge.ui.screens.rules

import com.astraedus.nudge.ui.localization.builtInCopy

import com.astraedus.nudge.ui.localization.ruleDescription
import com.astraedus.nudge.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import com.astraedus.nudge.ui.hasGrayscalePermission
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astraedus.nudge.domain.model.BlockMode
import com.astraedus.nudge.ui.components.blockModeDescription
import com.astraedus.nudge.ui.components.blockModeLabel
import com.astraedus.nudge.ui.components.CustomTimeDialog
import com.astraedus.nudge.ui.components.MinutesField
import com.astraedus.nudge.ui.components.formatMinutesDisplay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RuleEditorScreen(
    viewModel: RuleEditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRuleEditor: (String, Long) -> Unit,
    onCreateNewRule: (String) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.existingRuleId == null) strings.getString(R.string.ui_new_rule) else strings.getString(R.string.ui_edit_rule)) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                state.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // --- Existing Rules Summary ---
            if (state.allRulesForPackage.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            strings.getString(R.string.ui_current_rules),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_if_multiple_active_rules_match_nudge_uses_the_strongest_action_hard_bl)
                        )
                    }

                    state.allRulesForPackage.forEach { rule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToRuleEditor(state.packageName, rule.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (rule.enabled)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    rule.sourceRule?.let { strings.ruleDescription(it) } ?: rule.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color = if (rule.enabled)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Switch(
                                    checked = rule.enabled,
                                    onCheckedChange = { viewModel.toggleRuleEnabled(rule.id, rule.enabled) }
                                )
                            }
                        }
                    }

                    if (state.existingRuleId != null) {
                        OutlinedButton(
                            onClick = { onCreateNewRule(state.packageName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.getString(R.string.ui_add_rule))
                        }
                    }
                }

                HorizontalDivider()
            }

            // --- Block Mode ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        strings.getString(R.string.ui_block_mode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    InfoButton(
                        strings.getString(R.string.ui_choose_what_this_rule_does_when_it_matches_hard_block_completely_preve)
                    )
                }

                // BlockMode.NONE is excluded here: this editor has no "block the whole app" switch,
                // so a NONE option would produce a rule that silently does nothing.
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    EDITOR_BLOCKING_MODES.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.blockMode == mode,
                            onClick = { viewModel.setBlockMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = EDITOR_BLOCKING_MODES.size
                            ),
                            // No check icon. With four modes in the row its 18dp + 8dp would cost
                            // more than a quarter of the width left for "Hard Block", and the
                            // selected segment is already unmistakable from its container colour.
                            icon = {}
                        ) {
                            Text(
                                strings.builtInCopy(blockModeLabel(mode)),
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    strings.builtInCopy(blockModeDescription(state.blockMode)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.blockMode.usesDuration) {
                val durationLabel =
                    if (state.blockMode == BlockMode.HOLD) strings.getString(R.string.ui_hold_duration) else strings.getString(R.string.ui_delay_duration)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        durationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    val delayPresets = remember { listOf(5, 15, 30, 60) }
                    var showDelayDialog by remember { mutableStateOf(false) }
                    val isCustomDelay = state.delaySeconds !in delayPresets

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        delayPresets.forEach { seconds ->
                            FilterChip(
                                selected = state.delaySeconds == seconds,
                                onClick = { viewModel.setDelaySeconds(seconds) },
                                label = { Text(strings.getString(R.string.ui_s, seconds)) }
                            )
                        }
                        FilterChip(
                            selected = isCustomDelay,
                            onClick = { showDelayDialog = true },
                            label = {
                                Text(if (isCustomDelay) strings.getString(R.string.ui_s, state.delaySeconds) else strings.getString(R.string.ui_custom))
                            }
                        )
                    }

                    if (showDelayDialog) {
                        CustomTimeDialog(
                            title = strings.getString(R.string.ui_custom_2, durationLabel),
                            unit = strings.getString(R.string.ui_seconds),
                            currentValue = state.delaySeconds,
                            min = 1,
                            max = 300,
                            onConfirm = { seconds ->
                                viewModel.setDelaySeconds(seconds)
                                showDelayDialog = false
                            },
                            onDismiss = { showDelayDialog = false }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Daily Time Limit ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            strings.getString(R.string.ui_daily_time_limit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_set_a_daily_usage_budget_for_this_app_once_you_ve_used_the_app_for_thi_2)
                        )
                    }
                    Switch(
                        checked = state.dailyLimitEnabled,
                        onCheckedChange = { viewModel.setDailyLimitEnabled(it) }
                    )
                }

                if (state.dailyLimitEnabled) {
                    val dailyPresets = remember { listOf(15, 30, 60, 120) }
                    val dailyPresetLabels = remember { mapOf(15 to strings.getString(R.string.ui_15m), 30 to strings.getString(R.string.ui_30m), 60 to strings.getString(R.string.ui_1h), 120 to strings.getString(R.string.ui_2h)) }
                    var showDailyLimitDialog by remember { mutableStateOf(false) }
                    val isCustomDaily = state.dailyLimitMinutes !in dailyPresets

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        dailyPresets.forEach { minutes ->
                            FilterChip(
                                selected = state.dailyLimitMinutes == minutes,
                                onClick = { viewModel.setDailyLimitMinutes(minutes) },
                                label = { Text(dailyPresetLabels[minutes] ?: strings.getString(R.string.ui_m, minutes)) }
                            )
                        }
                        FilterChip(
                            selected = isCustomDaily,
                            onClick = { showDailyLimitDialog = true },
                            label = {
                                Text(
                                    if (isCustomDaily) strings.durationLabel(formatMinutesDisplay(state.dailyLimitMinutes))
                                    else strings.getString(R.string.ui_custom)
                                )
                            }
                        )
                    }

                    if (showDailyLimitDialog) {
                        CustomTimeDialog(
                            title = strings.getString(R.string.ui_custom_daily_limit),
                            unit = strings.getString(R.string.ui_minutes),
                            currentValue = state.dailyLimitMinutes,
                            min = 1,
                            max = 480,
                            onConfirm = { minutes ->
                                viewModel.setDailyLimitMinutes(minutes)
                                showDailyLimitDialog = false
                            },
                            onDismiss = { showDailyLimitDialog = false }
                        )
                    }

                    // Show time remaining overlay toggle
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    strings.getString(R.string.ui_show_time_remaining),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                InfoButton(
                                    strings.getString(R.string.ui_displays_remaining_daily_time_as_a_floating_overlay_while_you_use_this)
                                )
                            }
                            Text(
                                strings.getString(R.string.ui_floating_overlay_showing_remaining_daily_time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.showTimeRemaining,
                            onCheckedChange = { viewModel.setShowTimeRemaining(it) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Schedule ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            strings.getString(R.string.ui_schedule),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_only_apply_this_rule_during_specific_times_select_which_days_the_rule)
                        )
                    }
                    Switch(
                        checked = state.scheduleEnabled,
                        onCheckedChange = { viewModel.setScheduleEnabled(it) }
                    )
                }

                if (state.scheduleEnabled) {
                    Text(
                        strings.getString(R.string.ui_active_days),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dayLabels = remember { listOf(
                            1 to strings.getString(R.string.ui_mon), 2 to strings.getString(R.string.ui_tue), 3 to strings.getString(R.string.ui_wed), 4 to strings.getString(R.string.ui_thu),
                            5 to strings.getString(R.string.ui_fri), 6 to strings.getString(R.string.ui_sat), 7 to strings.getString(R.string.ui_sun)
                        ) }
                        dayLabels.forEach { (day, label) ->
                            FilterChip(
                                selected = day in state.scheduleDays,
                                onClick = { viewModel.toggleScheduleDay(day) },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Time pickers - start time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.getString(R.string.ui_start_time), style = MaterialTheme.typography.bodyMedium)
                        TimeSelector(
                            hour = state.scheduleStartHour,
                            minute = state.scheduleStartMinuteOfHour,
                            onTimeSelected = { h, m -> viewModel.setScheduleStartTime(h, m) }
                        )
                    }

                    // Time pickers - end time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.getString(R.string.ui_end_time), style = MaterialTheme.typography.bodyMedium)
                        TimeSelector(
                            hour = state.scheduleEndHour,
                            minute = state.scheduleEndMinuteOfHour,
                            onTimeSelected = { h, m -> viewModel.setScheduleEndTime(h, m) }
                        )
                    }

                    if (state.scheduleDays.isEmpty()) {
                        Text(
                            strings.getString(R.string.ui_no_days_selected_rule_will_apply_every_day_during_the_time_window),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Apply Rule To (only for supported apps) ---
            if (state.supportsInAppBlocking) {
                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            strings.getString(R.string.ui_apply_rule_to),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_choose_where_this_rule_applies_if_no_features_are_selected_this_rule_a)
                        )
                    }

                    Text(
                        strings.getString(R.string.ui_select_features_to_scope_this_rule_leave_all_off_for_the_whole_app),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val pkg = state.packageName

                    // Instagram features
                    if (pkg == "com.instagram.android") {
                        InAppCheckbox(
                            label = "Reels",
                            checked = state.inAppReels,
                            onCheckedChange = { viewModel.setInAppReels(it) }
                        )
                        InAppCheckbox(
                            label = strings.getString(R.string.ui_explore),
                            checked = state.inAppExplore,
                            onCheckedChange = { viewModel.setInAppExplore(it) }
                        )
                    }

                    // YouTube features
                    if (pkg == "com.google.android.youtube") {
                        InAppCheckbox(
                            label = "Shorts",
                            checked = state.inAppShorts,
                            onCheckedChange = { viewModel.setInAppShorts(it) }
                        )
                    }

                    // TikTok features
                    if (pkg == "com.zhiliaoapp.musically" || pkg == "com.ss.android.ugc.trill") {
                        InAppCheckbox(
                            label = strings.getString(R.string.ui_tiktok_feed),
                            checked = state.inAppTikTokFeed,
                            onCheckedChange = { viewModel.setInAppTikTokFeed(it) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Interaction Counter ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                strings.getString(R.string.ui_interaction_counter_2),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            InfoButton(
                                strings.getString(R.string.ui_shows_a_floating_counter_on_screen_while_you_use_this_app_for_youtube)
                            )
                        }
                        Text(
                            strings.getString(R.string.ui_show_floating_counter_while_using_this_app),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.showCounter,
                        onCheckedChange = { viewModel.setShowCounter(it) }
                    )
                }
            }

            HorizontalDivider()

            // --- Auto-close app ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                strings.getString(R.string.ui_auto_close_app),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            InfoButton(
                                strings.getString(R.string.ui_automatically_sends_you_to_the_home_screen_after_n_scrolls_taps_and_or)
                            )
                        }
                        Text(
                            strings.getString(R.string.ui_sends_you_to_the_home_screen_whichever_trigger_fires_first_wins),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.autoKickEnabled,
                        onCheckedChange = { viewModel.setAutoKickEnabled(it) }
                    )
                }

                if (state.autoKickEnabled) {
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.showCounter) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    strings.getString(R.string.ui_after_n_interactions),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Switch(
                                    checked = state.autoKickByInteractions,
                                    onCheckedChange = { viewModel.setAutoKickByInteractions(it) }
                                )
                            }

                            if (state.autoKickByInteractions) {
                                Text(
                                    strings.getString(R.string.ui_after_scrolls_taps, state.autoKickAfter),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Slider(
                                    value = state.autoKickAfter.toFloat(),
                                    onValueChange = { viewModel.setAutoKickAfter(it.toInt()) },
                                    valueRange = 5f..100f,
                                    steps = 18, // (100-5)/5 - 1 = 18 steps for increments of 5
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "5",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "100",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(
                                strings.getString(R.string.ui_turn_on_the_interaction_counter_above_to_also_kick_after_n_scrolls_or),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        MinutesField(
                            value = state.autoKickAfterMinutesText,
                            onValueChange = { viewModel.setAutoKickAfterMinutesText(it) },
                            labelText = strings.getString(R.string.ui_or_after_this_long_in_the_app),
                            supportingText = strings.getString(R.string.ui_counts_foreground_time_in_one_session_leave_blank_for_off)
                        )

                        Spacer(Modifier.height(8.dp))

                        MinutesField(
                            value = state.autoKickCooldownMinutesText,
                            onValueChange = { viewModel.setAutoKickCooldownMinutesText(it) },
                            labelText = strings.getString(R.string.ui_cooldown),
                            supportingText = strings.getString(R.string.ui_wait_this_long_before_you_can_re_open_the_app_after_an_auto_close)
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Grayscale ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                strings.getString(R.string.ui_grayscale_mode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            InfoButton(
                                strings.getString(R.string.ui_makes_your_phone_screen_black_and_white_while_this_app_is_in_the_foreg)
                            )
                        }
                        Text(
                            strings.getString(R.string.ui_make_screen_gray_when_this_app_is_open),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.grayscale,
                        onCheckedChange = { enabled ->
                            if (enabled && !hasGrayscalePermission(context)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        strings.getString(R.string.ui_grayscale_requires_setup_check_settings_2)
                                    )
                                }
                            } else {
                                viewModel.setGrayscale(enabled)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.existingRuleId == null) strings.getString(R.string.ui_create_rule) else strings.getString(R.string.ui_save_rule))
            }

            if (state.existingRuleId != null) {
                OutlinedButton(
                    onClick = { viewModel.delete() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings.getString(R.string.ui_delete_rule))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoButton(explanation: String) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = strings.getString(R.string.ui_more_info),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(strings.getString(R.string.ui_got_it))
                }
            },
            text = {
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

@Composable
private fun InAppCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * Simple time selector using hour/minute FilterChips.
 * Displays current time as a button, cycles through preset options.
 */
@Composable
private fun TimeSelector(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour selector
        FilterChip(
            selected = true,
            onClick = {
                // Cycle hour: +1, wrap at 24
                onTimeSelected((hour + 1) % 24, minute)
            },
            label = { Text(String.format("%02d", hour)) }
        )
        Text(":", style = MaterialTheme.typography.bodyLarge)
        // Minute selector
        FilterChip(
            selected = true,
            onClick = {
                // Cycle minute in 15-min increments
                onTimeSelected(hour, (minute + 15) % 60)
            },
            label = { Text(String.format("%02d", minute)) }
        )
    }
}

/**
 * Modes offered by this editor. Excludes [BlockMode.NONE], which is only meaningful alongside the
 * "Block the whole app" switch in `UnifiedAppConfigScreen`.
 */
private val EDITOR_BLOCKING_MODES =
    listOf(BlockMode.HARD_BLOCK, BlockMode.DELAY, BlockMode.HOLD, BlockMode.BREATHING)
