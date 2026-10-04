package com.astraedus.nudge.ui.screens.stats

import com.astraedus.nudge.ui.localization.*

import com.astraedus.nudge.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.astraedus.nudge.ui.screens.stats.charts.BarSegment
import com.astraedus.nudge.ui.screens.stats.charts.RateBar
import com.astraedus.nudge.ui.screens.stats.charts.RateBarChart
import com.astraedus.nudge.ui.screens.stats.charts.SegmentedBar
import com.astraedus.nudge.ui.screens.stats.charts.WalkAwayRing

/**
 * "Your willpower, visualized." Supportive, never shaming — the hero always speaks in
 * terms of what the user DID (walked away), not what they failed to do.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WillpowerScreen(
    viewModel: WillpowerViewModel,
    onNavigateBack: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val calculator = viewModel.calculator

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.getString(R.string.ui_willpower)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.getString(R.string.ui_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InsightsRangeToggle(
                    selected = state.range,
                    onSelect = viewModel::selectRange,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp)
                )
            }

            item { WillpowerHero(state.insights) }

            item { TimeReclaimedSection(state) }

            item { WillpowerClockSection(state.insights, calculator) }

            item { ResistanceLeaderboardSection(state.apps) }

            item { WeeklyTrendSection(state.insights, calculator) }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WillpowerHero(insights: WillpowerInsights, modifier: Modifier = Modifier) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // The supportive line below carries the empty-state wording for this hero, so the
        // ring must not print its own — two "no blocks yet" sentences stacked reads broken.
        WalkAwayRing(
            walkedAway = insights.walkAways,
            gaveIn = insights.gaveIn,
            emptyMessage = ""
        )
        val supportiveLine = if (insights.attempts > 0) {
            strings.getString(R.string.ui_you_walked_away_of_times, insights.walkAways, insights.attempts)
        } else {
            strings.getString(R.string.ui_no_blocks_yet_in_this_period_that_is_a_clean_slate_not_a_failure)
        }
        Text(
            supportiveLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimeReclaimedSection(state: WillpowerUiState, modifier: Modifier = Modifier) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    InsightSection(
        title = strings.getString(R.string.ui_time_reclaimed),
        subtitle = strings.getString(R.string.ui_estimated_from_your_average_session_length_in_each_app),
        modifier = modifier
    ) {
        if (state.timeReclaimed.totalMs <= 0L) {
            InsightEmptyState(strings.getString(R.string.ui_no_time_reclaimed_yet_your_walk_aways_will_show_up_here))
        } else {
            Text(
                strings.durationLabel(state.timeReclaimedFormatted),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                strings.getString(R.string.ui_time_reclaimed_est),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.timeReclaimed.appsEstimatedFromDefault > 0) {
                Text(
                    strings.getString(R.string.ui_some_apps_use_a_5_minute_default),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WillpowerClockSection(
    insights: WillpowerInsights,
    calculator: InsightsCalculator,
    modifier: Modifier = Modifier
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    InsightSection(title = strings.getString(R.string.ui_willpower_clock), modifier = modifier) {
        val maxAttempts = insights.hours.maxOfOrNull { it.attempts } ?: 0
        val bars = insights.hours.map { hour ->
            RateBar(
                label = if (hour.hour % 6 == 0) strings.hourLabel(hour.hour) else "",
                fraction = hour.rate,
                confidence = if (hour.attempts == 0 || maxAttempts <= 0) {
                    0f
                } else {
                    (hour.attempts.toFloat() / maxAttempts.toFloat()).coerceIn(0f, 1f)
                },
                readout = strings.getString(R.string.ui_of_walked_away, strings.hourLabel(hour.hour), hour.walkAways, hour.attempts, calculator.formatPercent(hour.rate))
            )
        }
        RateBarChart(bars = bars, emptyMessage = strings.getString(R.string.ui_no_walk_aways_yet_in_this_period))

        val strongest = insights.strongestHour
        val weakest = insights.weakestHour
        val callout = when {
            strongest != null && weakest != null ->
                strings.getString(R.string.ui_strongest_at_weakest_at, strings.hourLabel(strongest), strings.hourLabel(weakest))
            strongest != null -> strings.getString(R.string.ui_strongest_at, strings.hourLabel(strongest))
            weakest != null -> strings.getString(R.string.ui_weakest_at, strings.hourLabel(weakest))
            else -> strings.getString(R.string.ui_not_enough_data_yet_to_spot_an_hourly_pattern)
        }
        Text(
            callout,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ResistanceLeaderboardSection(apps: List<WillpowerAppRow>, modifier: Modifier = Modifier) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    InsightSection(title = strings.getString(R.string.ui_resistance_leaderboard), modifier = modifier) {
        if (apps.isEmpty()) {
            InsightEmptyState(strings.getString(R.string.ui_no_blocks_yet_in_this_period))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                apps.forEach { row ->
                    AppResistanceRow(row)
                }
            }
        }
    }
}

@Composable
private fun AppResistanceRow(row: WillpowerAppRow, modifier: Modifier = Modifier) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val icon = row.icon
                if (icon != null) {
                    val bitmap = remember(icon) { icon.toBitmap(32, 32).asImageBitmap() }
                    Image(
                        bitmap = bitmap,
                        contentDescription = if (row.packageName == "web") strings.getString(R.string.websites) else row.label,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    if (row.packageName == "web") strings.getString(R.string.websites) else row.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                row.ratePercent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }

        SegmentedBar(
            segments = listOf(
                BarSegment(row.walkAways.toFloat(), MaterialTheme.colorScheme.primary),
                BarSegment(row.gaveIn.toFloat(), MaterialTheme.colorScheme.surfaceVariant)
            )
        )
        Text(
            strings.getString(R.string.ui_walked_away_gave_in, row.walkAways, row.gaveIn),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeeklyTrendSection(
    insights: WillpowerInsights,
    calculator: InsightsCalculator,
    modifier: Modifier = Modifier
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    InsightSection(title = strings.getString(R.string.ui_weekly_trend), modifier = modifier) {
        val maxAttempts = insights.weeks.maxOfOrNull { it.attempts } ?: 0
        val bars = insights.weeks.map { week ->
            RateBar(
                label = strings.weekLabel(week.label),
                fraction = week.rate,
                confidence = if (week.attempts == 0 || maxAttempts <= 0) {
                    0f
                } else {
                    (week.attempts.toFloat() / maxAttempts.toFloat()).coerceIn(0f, 1f)
                },
                readout = strings.getString(R.string.ui_of_walked_away, strings.weekLabel(week.label), week.walkAways, week.attempts, calculator.formatPercent(week.rate))
            )
        }
        RateBarChart(bars = bars, emptyMessage = strings.getString(R.string.ui_no_data_in_this_period))

        val trendText = if (insights.weeks.size >= 2) {
            val previous = insights.weeks[insights.weeks.size - 2]
            val last = insights.weeks.last()
            if (previous.attempts > 0) {
                val deltaPoints = calculator.percentOf(last.rate) - calculator.percentOf(previous.rate)
                when {
                    deltaPoints > 0 -> strings.getString(R.string.ui_up_points_vs_last_week, deltaPoints)
                    deltaPoints < 0 -> strings.getString(R.string.ui_down_points_vs_last_week, -deltaPoints)
                    else -> strings.getString(R.string.ui_level_with_last_week)
                }
            } else {
                null
            }
        } else {
            strings.getString(R.string.ui_switch_to_30_days_to_see_your_weekly_trend)
        }
        if (trendText != null) {
            Text(
                trendText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
