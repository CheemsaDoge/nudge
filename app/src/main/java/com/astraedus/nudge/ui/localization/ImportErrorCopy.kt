package com.astraedus.nudge.ui.localization

import android.content.res.Resources
import com.astraedus.nudge.R

/**
 * Presentation adapter for the existing pure importer's diagnostics. Field names, values and
 * platform JSON-parser details remain verbatim; only app-owned prose is translated.
 */
fun Resources.importErrorCopy(value: String): String {
    if (configuration.locales[0].language == "en") return value
    fun match(pattern: String) = Regex(pattern, RegexOption.DOT_MATCHES_ALL).matchEntire(value)
    match("(Rule|Group|History event) ([0-9]+): (.*)")?.let { m ->
        val kind = when (m.groupValues[1]) {
            "Rule" -> R.string.error_rule
            "Group" -> R.string.error_group
            else -> R.string.error_history
        }
        return getString(R.string.error_entry, getString(kind), m.groupValues[2], importErrorCopy(m.groupValues[3]))
    }
    match("Setting (\".*\"): (.*)")?.let { m ->
        return getString(R.string.error_setting, m.groupValues[1], importErrorCopy(m.groupValues[2]))
    }
    match("Export version ([0-9]+) is newer than supported \\(([0-9]+)\\). Please update the app\\.")?.let { m ->
        return getString(R.string.error_version_newer, m.groupValues[1], m.groupValues[2])
    }
    match("No valid rules or groups found\\. All ([0-9]+) entries were invalid:\\n(.*)")?.let { m ->
        return getString(R.string.error_no_valid, m.groupValues[1]) + "\n" +
            m.groupValues[2].lines().joinToString("\n") { importErrorCopy(it) }
    }
    match("\\.\\.\\.and ([0-9]+) more")?.let { return getString(R.string.error_more, it.groupValues[1]) }
    match("(Invalid JSON format|Invalid data): (.*)")?.let { m ->
        val key = if (m.groupValues[1] == "Invalid data") R.string.error_invalid_data else R.string.error_invalid_json
        return getString(key, importErrorCopy(m.groupValues[2]))
    }
    match("[Uu]nknown block mode: (.*)")?.let { return getString(R.string.error_unknown_mode, it.groupValues[1]) }
    match("challenge length ([0-9]+) is outside 1\\.\\.([0-9]+)")?.let {
        return getString(R.string.error_challenge_range, it.groupValues[1], it.groupValues[2])
    }
    match("(\".*\") (is missing or is not text|is missing or is not a number)")?.let { m ->
        val key = if (m.groupValues[2].endsWith("text")) R.string.error_missing_text else R.string.error_missing_number
        return getString(key, m.groupValues[1])
    }
    match("(\".*\") (is not true or false|is not text|is not a number|must be an array|must be an object)")?.let { m ->
        return getString(R.string.error_field_type, m.groupValues[1], builtInCopy(m.groupValues[2]))
    }
    return builtInCopy(value)
}
