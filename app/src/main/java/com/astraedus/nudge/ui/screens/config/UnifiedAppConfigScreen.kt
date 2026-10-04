package com.astraedus.nudge.ui.screens.config

import com.astraedus.nudge.ui.localization.durationLabel
import com.astraedus.nudge.ui.localization.builtInCopy

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.astraedus.nudge.domain.model.FeatureMode
import com.astraedus.nudge.ui.components.CustomTimeDialog
import com.astraedus.nudge.ui.components.MinutesField
import com.astraedus.nudge.ui.components.StrictModeChallengeHost
import com.astraedus.nudge.ui.components.formatMinutesDisplay
import com.astraedus.nudge.ui.hasGrayscalePermission
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnifiedAppConfigScreen(
    viewModel: UnifiedAppConfigViewModel,
    onNavigateBack: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val challenge by viewModel.challenge.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    StrictModeChallengeHost(
        challenge = challenge,
        onVerify = viewModel::verifyChallenge,
        onCancel = viewModel::cancelChallenge
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.appName.ifEmpty { state.packageName })
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.getString(R.string.ui_back))
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text(strings.getString(R.string.ui_save))
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

            // ═══ MASTER TOGGLE ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.getString(R.string.ui_enabled),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.enabled,
                    onCheckedChange = viewModel::setEnabled
                )
            }

            HorizontalDivider()

            // ═══ ALWAYS ACTIVE ═══
            SectionHeader(strings.getString(R.string.ui_always_active))

            // Daily Time Limit
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
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_set_a_daily_usage_budget_for_this_app_once_you_ve_used_the_app_for_thi) +
                            // Honest about the one direction that does not work rather than letting
                            // the budget silently never count web time. The reverse DOES work and is
                            // worth saying: spending the budget in the app closes the website too.
                            if (state.webDomainEnabled) {
                                strings.getString(R.string.ui_this_budget_counts_time_in_the_app_only_once_it_runs_out_the_websites)
                            } else {
                                ""
                            }
                        )
                    }
                    Switch(
                        checked = state.dailyLimitEnabled,
                        onCheckedChange = viewModel::setDailyLimitEnabled
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

                    // Show time remaining sub-toggle
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
                                strings.getString(R.string.ui_show_time_remaining),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            InfoButton(
                                strings.getString(R.string.ui_displays_remaining_daily_time_as_a_floating_overlay_changes_color_as_t)
                            )
                        }
                        Switch(
                            checked = state.showTimeRemaining,
                            onCheckedChange = viewModel::setShowTimeRemaining
                        )
                    }
                }
            }

            // Interaction counter
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
                        strings.getString(R.string.ui_interaction_counter),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    InfoButton(
                        strings.getString(R.string.ui_shows_a_floating_counter_while_you_use_this_app_for_youtube_instagram)
                    )
                }
                Switch(
                    checked = state.showCounter,
                    onCheckedChange = viewModel::setShowCounter
                )
            }

            // Grayscale
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
                            strings.getString(R.string.ui_grayscale),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_makes_your_screen_black_and_white_while_this_app_is_open_removes_the_c)
                        )
                    }
                    Text(
                        // Honest about the limitation rather than promising a setting that
                        // silently does nothing: grayscale rides inside a BlockDecision.Block, so
                        // with the app itself unblocked only a feature override can trigger it.
                        // See BlockMode.NONE.
                        if (state.blocksWholeApp) {
                            strings.getString(R.string.ui_requires_adb_permission_setup)
                        } else {
                            strings.getString(R.string.ui_requires_adb_permission_setup_with_the_whole_app_unblocked_this_only_a)
                        },
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
                                    strings.getString(R.string.ui_grayscale_requires_setup_check_settings)
                                )
                            }
                        } else {
                            viewModel.setGrayscale(enabled)
                        }
                    }
                )
            }

            // Web domain blocking
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
                            strings.getString(R.string.ui_block_on_web_too),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_also_blocks_this_app_s_website_in_the_browser_works_even_with_block_th)
                        )
                    }
                    Switch(
                        checked = state.webDomainEnabled,
                        onCheckedChange = viewModel::setWebDomainEnabled
                    )
                }

                if (state.webDomainEnabled) {
                    OutlinedTextField(
                        value = state.webDomains,
                        onValueChange = viewModel::setWebDomains,
                        label = { Text(strings.getString(R.string.ui_domains_comma_separated)) },
                        placeholder = { Text("instagram.com, www.instagram.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        strings.getString(R.string.ui_subdomains_like_www_and_m_are_matched_automatically),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Websites enforce independently of the app-level mode (issue #21). While the
                    // app itself is blocked they simply follow its mode; when it is not, there is
                    // no app-level mode to follow, so the website mode is chosen here instead of
                    // silently enforcing nothing (which is what used to happen).
                    if (state.blocksWholeApp) {
                        Text(
                            strings.getString(R.string.ui_websites_use_the_same_block_mode_as_the_app),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            strings.getString(R.string.ui_website_block_mode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            BLOCKING_MODES.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = state.webBlockMode == mode,
                                    onClick = { viewModel.setWebBlockMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = BLOCKING_MODES.size
                                    )
                                ) {
                                    Text(strings.builtInCopy(blockModeLabel(mode)))
                                }
                            }
                        }
                        Text(
                            strings.getString(R.string.ui_opens_normally_these_websites_are_still_blocked, state.appName) +
                                strings.builtInCopy(blockModeDescription(state.webBlockMode)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // ═══ DEFAULT BEHAVIOR ═══
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SectionHeader(strings.getString(R.string.ui_default_behavior))
                InfoButton(strings.getString(R.string.ui_these_settings_apply_whenever_no_scheduled_override_is_active))
            }

            // Whether the app itself is gated at all. Off => the app-level rule is BlockMode.NONE,
            // so the app opens freely and only the feature overrides below apply. This is the
            // switch that makes "block only Shorts, leave YouTube alone" expressible.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.getString(R.string.ui_block_the_whole_app), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (state.blocksWholeApp) {
                            strings.getString(R.string.ui_opening_triggers_the_block_below, state.appName)
                        } else if (state.supportsFeatures) {
                            strings.getString(R.string.ui_opens_normally_only_the_features_you_turn_on_below_are_blocked, state.appName)
                        } else {
                            strings.getString(R.string.ui_opens_normally_any_daily_limit_below_still_applies, state.appName)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.blocksWholeApp,
                    onCheckedChange = { viewModel.setBlocksWholeApp(it) }
                )
            }

            // Block mode segmented button — only meaningful when the app itself is blocked.
            if (state.blocksWholeApp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        BLOCKING_MODES.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = state.defaultMode == mode,
                                onClick = { viewModel.setDefaultMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = BLOCKING_MODES.size
                                ),
                                // No check icon: with four modes in the row its 18dp + 8dp would
                                // cost more than a quarter of the width left for "Hard Block", and
                                // the selected segment is already unmistakable from its colour.
                                icon = {}
                            ) {
                                Text(strings.builtInCopy(blockModeLabel(mode)), maxLines = 1)
                            }
                        }
                    }

                    Text(
                        strings.builtInCopy(blockModeDescription(state.defaultMode)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delay duration (if applicable). One `delaySeconds` is stored per rule and it feeds
            // whichever delay is live — the app's, or (with whole-app blocking off) the website
            // one — so the control has to stay reachable in the web-only case too, else a web
            // DELAY would be permanently stuck at whatever was last saved.
            if (state.showDelayDuration) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        strings.builtInCopy(state.delayDurationLabel),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    val delayPresets = remember { listOf(5, 15, 30, 60) }
                    var showDelayDialog by remember { mutableStateOf(false) }
                    val isCustomDelay = state.defaultDelaySeconds !in delayPresets

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        delayPresets.forEach { seconds ->
                            FilterChip(
                                selected = state.defaultDelaySeconds == seconds,
                                onClick = { viewModel.setDefaultDelaySeconds(seconds) },
                                label = { Text(strings.getString(R.string.ui_s, seconds)) }
                            )
                        }
                        FilterChip(
                            selected = isCustomDelay,
                            onClick = { showDelayDialog = true },
                            label = {
                                Text(if (isCustomDelay) strings.getString(R.string.ui_s, state.defaultDelaySeconds) else strings.getString(R.string.ui_custom))
                            }
                        )
                    }

                    if (showDelayDialog) {
                        CustomTimeDialog(
                            title = strings.getString(R.string.ui_custom_2, strings.builtInCopy(state.delayDurationLabel)),
                            unit = strings.getString(R.string.ui_seconds),
                            currentValue = state.defaultDelaySeconds,
                            min = 1,
                            max = 300,
                            onConfirm = { seconds ->
                                viewModel.setDefaultDelaySeconds(seconds)
                                showDelayDialog = false
                            },
                            onDismiss = { showDelayDialog = false }
                        )
                    }
                }
            }

            // Auto-kick
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
                            strings.getString(R.string.ui_auto_kick),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        InfoButton(
                            strings.getString(R.string.ui_sends_you_to_the_home_screen_after_a_set_number_of_scrolls_taps_or_aft) +
                            // The two triggers differ on the web: the timer measures browser time on
                            // the blocked site, while scrolls/taps arrive carrying the browser's
                            // package, not the site's, so they cannot be attributed to it.
                            if (state.webDomainEnabled) {
                                strings.getString(R.string.ui_the_time_trigger_also_covers_the_websites_below_each_site_gets_its_own)
                            } else {
                                ""
                            }
                        )
                    }
                    Switch(
                        checked = state.defaultAutoKickEnabled,
                        onCheckedChange = viewModel::setDefaultAutoKickEnabled
                    )
                }

                if (state.defaultAutoKickEnabled) {
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            strings.getString(R.string.ui_whichever_trigger_fires_first_sends_you_to_the_home_screen),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

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
                                checked = state.defaultAutoKickByInteractions,
                                onCheckedChange = viewModel::setDefaultAutoKickByInteractions
                            )
                        }

                        if (state.defaultAutoKickByInteractions) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    strings.getString(R.string.ui_after_interactions, state.defaultAutoKickAfter),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = state.defaultAutoKickAfter.toFloat(),
                                    onValueChange = { viewModel.setDefaultAutoKickAfter(it.toInt()) },
                                    valueRange = 5f..100f,
                                    steps = 18,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("5", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("100", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        MinutesField(
                            value = state.defaultAutoKickAfterMinutesText,
                            onValueChange = viewModel::setDefaultAutoKickAfterMinutesText,
                            labelText = strings.getString(R.string.ui_or_after_this_long_in_the_app),
                            supportingText = strings.getString(R.string.ui_counts_foreground_time_in_one_session_leave_blank_for_off)
                        )

                        MinutesField(
                            value = state.defaultAutoKickCooldownMinutesText,
                            onValueChange = viewModel::setDefaultAutoKickCooldownMinutesText,
                            labelText = strings.getString(R.string.ui_cooldown),
                            supportingText = strings.getString(R.string.ui_wait_this_long_before_you_can_re_open_the_app_after_an_auto_close)
                        )
                    }
                }
            }

            // ═══ FEATURE OVERRIDES ═══
            if (state.supportsFeatures) {
                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SectionHeader(strings.getString(R.string.ui_feature_rules))
                    InfoButton(strings.getString(R.string.ui_override_behavior_for_specific_app_features_inherit_uses_the_default_b))
                }

                state.availableFeatures.forEach { feature ->
                    FeatureOverrideCard(
                        featureName = strings.builtInCopy(feature.displayName),
                        override = state.featureOverrides[feature.key] ?: FeatureOverride(),
                        onUpdate = { viewModel.setFeatureOverride(feature.key, it) }
                    )
                }

                // Tab Vanish -- covers the in-app tab (e.g. Instagram's Reels tab) while a rule
                // covering that feature resolves to a hard block, so the icon disappears rather
                // than merely refusing to open. Visibility asks the registry (supportsTabVanish),
                // never a hardcoded package check.
                if (state.supportsTabVanish) {
                    val vanishLabel = state.vanishableFeatureLabel ?: "Reels"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.getString(R.string.ui_hide_the_tab, vanishLabel), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                strings.getString(R.string.ui_while_is_blocked_nudge_covers_the_tab_in_instagram_s_bottom_bar_so_the, vanishLabel, vanishLabel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.tabVanish,
                            onCheckedChange = viewModel::setTabVanish
                        )
                    }
                }

                // Following steer -- experimental, default off. Opens Instagram's Following feed
                // instead of Home. Visibility asks the registry (supportsFollowingSteer).
                if (state.supportsFollowingSteer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.getString(R.string.ui_open_to_following_instead_of_home),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                strings.getString(R.string.ui_experimental_when_you_open_instagram_s_home_feed_nudge_switches_it_to),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.followingSteer,
                            onCheckedChange = viewModel::setFollowingSteer
                        )
                    }
                }
            }

            HorizontalDivider()

            // ═══ SCHEDULED OVERRIDE ═══
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SectionHeader(strings.getString(R.string.ui_scheduled_override))
                InfoButton(strings.getString(R.string.ui_apply_different_settings_during_specific_times_outside_this_schedule_t))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.getString(R.string.ui_enable_schedule),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.scheduledOverrideEnabled,
                    onCheckedChange = viewModel::setScheduledOverrideEnabled
                )
            }

            if (state.scheduledOverrideEnabled) {
                // Day selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        strings.getString(R.string.ui_active_days),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dayLabels = remember {
                            listOf(
                                1 to strings.getString(R.string.ui_mon), 2 to strings.getString(R.string.ui_tue), 3 to strings.getString(R.string.ui_wed), 4 to strings.getString(R.string.ui_thu),
                                5 to strings.getString(R.string.ui_fri), 6 to strings.getString(R.string.ui_sat), 7 to strings.getString(R.string.ui_sun)
                            )
                        }
                        dayLabels.forEach { (day, label) ->
                            FilterChip(
                                selected = day in state.scheduleDays,
                                onClick = {
                                    val newDays = state.scheduleDays.toMutableSet()
                                    if (day in newDays) newDays.remove(day) else newDays.add(day)
                                    viewModel.setScheduleDays(newDays)
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Start time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.getString(R.string.ui_start_time), style = MaterialTheme.typography.bodyMedium)
                        TimeSelector(
                            hour = state.scheduleStartHour,
                            minute = state.scheduleStartMinute,
                            onTimeSelected = { h, m -> viewModel.setScheduleStartTime(h, m) }
                        )
                    }

                    // End time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.getString(R.string.ui_end_time), style = MaterialTheme.typography.bodyMedium)
                        TimeSelector(
                            hour = state.scheduleEndHour,
                            minute = state.scheduleEndMinute,
                            onTimeSelected = { h, m -> viewModel.setScheduleEndTime(h, m) }
                        )
                    }

                    // Scheduled mode
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        BLOCKING_MODES.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = state.scheduledMode == mode,
                                onClick = { viewModel.setScheduledMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = BLOCKING_MODES.size
                                )
                            ) {
                                Text(strings.builtInCopy(blockModeLabel(mode)))
                            }
                        }
                    }

                    // Scheduled delay duration
                    if (state.scheduledMode != BlockMode.HARD_BLOCK) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 15, 30, 60).forEach { seconds ->
                                FilterChip(
                                    selected = state.scheduledDelaySeconds == seconds,
                                    onClick = { viewModel.setScheduledDelaySeconds(seconds) },
                                    label = { Text(strings.getString(R.string.ui_s, seconds)) }
                                )
                            }
                        }
                    }

                    // Scheduled feature overrides
                    if (state.supportsFeatures) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            strings.getString(R.string.ui_feature_overrides_during_schedule),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        state.availableFeatures.forEach { feature ->
                            FeatureOverrideCard(
                                featureName = strings.builtInCopy(feature.displayName),
                                override = state.scheduledFeatureOverrides[feature.key]
                                    ?: FeatureOverride(),
                                onUpdate = { viewModel.setScheduledFeatureOverride(feature.key, it) }
                            )
                        }
                    }
                }
            }

            // ═══ DANGER ZONE ═══
            if (state.hasExistingRules) {
                HorizontalDivider()
                OutlinedButton(
                    onClick = viewModel::showDeleteConfirmation,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.getString(R.string.ui_remove_all_rules))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            title = { Text(strings.getString(R.string.ui_remove_all_rules_2)) },
            text = {
                Text(
                    strings.getString(R.string.ui_this_will_delete_all_rules_for, state.appName.ifEmpty { state.packageName }) +
                    strings.getString(R.string.ui_the_app_will_no_longer_be_blocked)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissDeleteConfirmation()
                        viewModel.deleteAllRules()
                    }
                ) {
                    Text(strings.getString(R.string.ui_remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirmation) {
                    Text(strings.getString(R.string.ui_cancel))
                }
            }
        )
    }
}

// ═══ Reusable composables ═══

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureOverrideCard(
    featureName: String,
    override: FeatureOverride,
    onUpdate: (FeatureOverride) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                featureName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            // Mode selector
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FeatureMode.entries.forEach { mode ->
                    FilterChip(
                        selected = override.mode == mode,
                        onClick = { onUpdate(override.copy(mode = mode)) },
                        label = {
                            Text(
                                strings.builtInCopy(featureModeLabel(mode)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            // Expanded settings for every mode that spends a duration.
            if (override.mode.usesDuration) {
                // Delay duration chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 15, 30, 60).forEach { s ->
                        FilterChip(
                            selected = override.delaySeconds == s,
                            onClick = { onUpdate(override.copy(delaySeconds = s)) },
                            label = { Text(strings.getString(R.string.ui_s, s)) }
                        )
                    }
                }

                // Auto-kick toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.getString(R.string.ui_auto_kick), style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = override.autoKickEnabled,
                        onCheckedChange = { onUpdate(override.copy(autoKickEnabled = it)) }
                    )
                }

                if (override.autoKickEnabled) {
                    Text(
                        strings.getString(R.string.ui_after_scrolls, override.autoKickAfter),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = override.autoKickAfter.toFloat(),
                        onValueChange = { onUpdate(override.copy(autoKickAfter = it.toInt())) },
                        valueRange = 5f..100f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MinutesField(
                        value = override.autoKickCooldownMinutesText,
                        onValueChange = { onUpdate(override.copy(autoKickCooldownMinutesText = it)) },
                        labelText = strings.getString(R.string.ui_cooldown)
                    )
                }
            }
        }
    }
}

/**
 * Simple time selector using hour/minute FilterChips.
 * Cycles hour by +1 and minute in 15-minute increments on click.
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
        FilterChip(
            selected = true,
            onClick = { onTimeSelected((hour + 1) % 24, minute) },
            label = { Text(String.format("%02d", hour)) }
        )
        Text(":", style = MaterialTheme.typography.bodyLarge)
        FilterChip(
            selected = true,
            onClick = { onTimeSelected(hour, (minute + 15) % 60) },
            label = { Text(String.format("%02d", minute)) }
        )
    }
}

/**
 * The modes that actually gate an app, in ascending severity.
 *
 * Deliberately NOT `BlockMode.entries`: [BlockMode.NONE] must never appear in a mode picker.
 * At app level it is expressed by the "Block the whole app" switch, and for a SCHEDULED override
 * it would be an outright lie — a scheduled rule is additive, so a NONE scheduled rule adds
 * nothing rather than carving out an unblocked window during those hours.
 */
private val BLOCKING_MODES =
    listOf(BlockMode.HARD_BLOCK, BlockMode.DELAY, BlockMode.HOLD, BlockMode.BREATHING)

/**
 * The words on an in-app FEATURE override chip. Separate from [blockModeLabel] only because
 * [FeatureMode] carries [FeatureMode.INHERIT], which is not a block mode at all; the rest read the
 * same as the app-level picker on purpose.
 */
private fun featureModeLabel(mode: FeatureMode): String = when (mode) {
    FeatureMode.INHERIT -> "Inherit"
    FeatureMode.BLOCK -> "Block"
    FeatureMode.DELAY -> "Delay"
    FeatureMode.HOLD -> "Hold"
    FeatureMode.BREATHING -> "Breathing"
}
