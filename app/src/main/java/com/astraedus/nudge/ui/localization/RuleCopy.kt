package com.astraedus.nudge.ui.localization

import android.content.res.Resources
import com.astraedus.nudge.R
import com.astraedus.nudge.data.db.entity.BlockRule
import com.astraedus.nudge.domain.model.BlockMode
import com.astraedus.nudge.ui.components.blockModeLabel

/** Display only: feature keys and rule data remain unchanged in storage and matching. */
fun Resources.featureName(key: String): String = when (key.uppercase()) {
    "EXPLORE" -> getString(R.string.ui_explore)
    "TIKTOK_FEED" -> getString(R.string.ui_tiktok_feed)
    "REELS" -> "Reels"
    "SHORTS" -> "Shorts"
    else -> key
}

fun Resources.ruleMode(mode: String, seconds: Int): String {
    val blockMode = BlockMode.entries.firstOrNull { it.name == mode } ?: return mode
    val label = builtInCopy(blockModeLabel(blockMode))
    return if (blockMode.usesDuration) getString(R.string.mode_duration, label, seconds) else label
}

fun Resources.ruleDescription(rule: BlockRule): String {
    val scope = rule.inAppFeatures?.split(",")?.filter(String::isNotBlank)
        ?.joinToString(", ") { featureName(it.trim()) }
        ?.takeIf(String::isNotBlank) ?: getString(R.string.whole_app)
    val extras = buildList {
        rule.dailyLimitMinutes?.let { add(getString(R.string.rule_daily_limit, it)) }
        if (rule.scheduleDays != null) add(getString(R.string.scheduled))
        if (rule.grayscale) add(getString(R.string.ui_grayscale))
        if (rule.showCounter) add(getString(R.string.counter))
        rule.autoKickAfter?.let { add(getString(R.string.rule_kick_interactions, it)) }
        rule.autoKickAfterMinutes?.let { add(getString(R.string.rule_kick_minutes, it)) }
        if (rule.showTimeRemaining) add(getString(R.string.time_remaining))
    }
    return "$scope: ${ruleMode(rule.mode, rule.delaySeconds)}" +
        if (extras.isEmpty()) "" else " + ${extras.joinToString(", ")}"
}

fun Resources.ruleGroupSummary(rules: List<BlockRule>): String = buildList {
    rules.firstOrNull { it.inAppFeatures.isNullOrBlank() }?.let { rule ->
        add(ruleMode(rule.mode, rule.delaySeconds))
        rule.dailyLimitMinutes?.let { add(getString(R.string.rule_daily_limit, it)) }
        if (rule.scheduleDays != null || rule.scheduleStartMinute != null) add(getString(R.string.scheduled))
    }
    rules.filter { !it.inAppFeatures.isNullOrBlank() }.forEach { rule ->
        val scope = rule.inAppFeatures!!.split(",").filter(String::isNotBlank)
            .joinToString("/") { featureName(it.trim()) }
        add("$scope: ${ruleMode(rule.mode, rule.delaySeconds)}")
    }
}.ifEmpty { listOf(getString(R.string.configured)) }.joinToString(" · ")
