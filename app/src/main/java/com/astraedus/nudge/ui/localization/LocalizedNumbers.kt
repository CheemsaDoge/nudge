package com.astraedus.nudge.ui.localization

import android.content.res.Resources
import com.astraedus.nudge.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

/** Use the Activity/widget configuration, including an explicit per-app locale. */
fun Resources.hourLabel(hour: Int): String = getString(R.string.hour_label, hour)

fun Resources.weekdayLabel(index: Int, full: Boolean = false): String =
    if (index !in 0..6) "" else DayOfWeek.of(index + 1).getDisplayName(
        if (full) TextStyle.FULL else TextStyle.SHORT, configuration.locales[0]
    )

/** Pure calculators keep their numerical output; presentation owns translated units. */
fun Resources.durationLabel(raw: String): String {
    if (raw.replace(" ", "") == "<1m") return getString(R.string.duration_under_minute)
    if (!raw.matches(Regex("\\d+[hms](?: \\d+[hms])*"))) return raw
    return Regex("(\\d+)([hms])").findAll(raw).joinToString(" ") { part ->
        val resource = when (part.groupValues[2]) {
            "h" -> R.string.duration_hour
            "m" -> R.string.duration_minute
            else -> R.string.duration_second
        }
        getString(resource, part.groupValues[1])
    }
}

fun Resources.weekLabel(raw: String): String {
    val weeks = Regex("(\\d+)w ago").matchEntire(raw)?.groupValues?.get(1)
    return if (weeks != null) getString(R.string.weeks_ago, weeks) else builtInCopy(raw)
}

fun Resources.dayLabel(date: LocalDate?, fallback: String): String = when (date) {
    null -> builtInCopy(fallback)
    LocalDate.now() -> getString(R.string.ui_today)
    LocalDate.now().minusDays(1) -> getString(R.string.yesterday)
    else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(configuration.locales[0]))
}

fun Resources.rangeLabel(start: LocalDate?, end: LocalDate?, fallback: String): String =
    if (start == null || end == null) builtInCopy(fallback)
    else if (end == LocalDate.now()) getString(R.string.ui_last_7_days)
    else "${dayLabel(start, "")} – ${dayLabel(end, "")}"
