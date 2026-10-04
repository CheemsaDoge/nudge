package com.astraedus.nudge.ui.localization

import android.content.res.Resources
import com.astraedus.nudge.R

/** Only for app-generated overlay rule descriptions, never user names or stored rule contents. */
fun Resources.generatedRuleName(raw: String?): String? {
    if (raw == null || configuration.locales[0].language == "en") return raw
    if (raw == "Restricted content") return getString(R.string.restricted_content)
    if (raw == "Auto-kick cooldown") return getString(R.string.auto_kick_cooldown)
    if (raw.startsWith("Auto-kick cooldown — ")) {
        return getString(R.string.web_auto_kick_cooldown, raw.removePrefix("Auto-kick cooldown — "))
    }
    val reached = raw.endsWith(" (limit reached)")
    val base = raw.removeSuffix(" (limit reached)")
    val qualified = Regex("(.*) \\((.*)\\)").matchEntire(base)
    val label = qualified?.groupValues?.get(1) ?: base
    val parts = label.split(" - ")
    val mode = when (parts.last()) {
        "Daily limit" -> getString(R.string.daily_limit_label)
        "No block" -> getString(R.string.no_block_label)
        "Hard Block", "Delay", "Hold", "Breathing", "Off" -> builtInCopy(parts.last())
        else -> return builtInCopy(raw)
    }
    val prefix = parts.dropLast(1).joinToString(" – ") { scope ->
        if (scope == "Web") getString(R.string.counter_web)
        else scope.split(", ").joinToString(", ") { featureName(it) }
    }
    val qualifiers = qualified?.groupValues?.get(2)?.split(", ")?.map { qualifier ->
        if (qualifier == "scheduled") getString(R.string.scheduled)
        else Regex("([0-9]+) min/day").matchEntire(qualifier)?.let {
            getString(R.string.rule_daily_limit, it.groupValues[1].toInt())
        } ?: qualifier
    }.orEmpty()
    val translated = (if (prefix.isEmpty()) mode else "$prefix – $mode") +
        (if (qualifiers.isEmpty()) "" else " (${qualifiers.joinToString(", ")})")
    return if (reached) getString(R.string.rule_limit_reached, translated) else translated
}
