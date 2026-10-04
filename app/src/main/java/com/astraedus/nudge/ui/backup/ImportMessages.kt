package com.astraedus.nudge.ui.backup

import com.astraedus.nudge.domain.usecase.ImportOutcome
import com.astraedus.nudge.domain.usecase.ImportPreview

/**
 * Pure text builders for the import dialogs.
 *
 * They live outside the composable so the wording -- especially "we skipped N entries you had in
 * your backup", the one thing the user must not miss -- is JVM-testable rather than only verifiable
 * by eye on a device.
 */

/** Cap on how many per-entry reasons a dialog lists before collapsing the rest into a count. */
private const val MAX_LISTED_REASONS = 3

/**
 * Body text of the "Import backup" confirmation dialog, shown BEFORE anything is written.
 *
 * A file with no history reads EXACTLY as it did before history existed -- a rules-only backup from
 * an older Nudge must not sprout a line about a feature it knows nothing about.
 */
fun buildImportPreviewMessage(preview: ImportPreview, copy: CopyText = ::englishCopy): String = buildString {
    val result = preview.result
    append(copy("Import %1\$s rule(s)", listOf(result.rules.size)))
    if (result.groups.isNotEmpty()) append(copy(" and %1\$s group(s)", listOf(result.groups.size)))
    append(copy("?\n\nDuplicate rules will be skipped.", listOf()))
    if (result.history.isNotEmpty()) {
        append(copy("\n\nIncludes %1\$s history event(s) (%2\$s new).", listOf(result.history.size, preview.newHistoryCount)))
    }
    if (result.invalidCount > 0) {
        append(copy("\n\n", listOf()))
        append(entriesCouldNotBeRead(result.invalidCount, copy))
        append(copy(" and will be left out:", listOf()))
        appendReasons(result.invalidReasons, copy)
    }
    if (result.settings != null) {
        // Warned BEFORE anything is written, because unlike rules and history this REPLACES what
        // is already on the device: custom block messages, the content filter, Strict Mode.
        append(copy("\n\nAlso restores app settings, replacing this device's.", listOf()))
    }
    if (result.invalidHistoryCount > 0) {
        append(copy("\n\n", listOf()))
        append(historyEntriesCouldNotBeRead(result.invalidHistoryCount, copy))
        append(copy(" and will be left out:", listOf()))
        appendReasons(result.invalidHistoryReasons, copy)
    }
    if (result.invalidSettingsCount > 0) {
        append(copy("\n\n", listOf()))
        append(settingsCouldNotBeRead(result.invalidSettingsCount, copy))
        append(copy(" and will be left out:", listOf()))
        appendReasons(result.invalidSettingsReasons, copy)
    }
}

/** Body text of the "Import Complete" dialog. */
fun buildImportOutcomeMessage(outcome: ImportOutcome, copy: CopyText = ::englishCopy): String = buildString {
    append(copy("Imported: %1\$s rule(s)", listOf(outcome.importedCount)))
    if (outcome.groupsCreated > 0) append(copy("\nGroups created: %1\$s", listOf(outcome.groupsCreated)))
    if (outcome.duplicateCount > 0) append(copy("\nSkipped (duplicates): %1\$s", listOf(outcome.duplicateCount)))
    if (outcome.hasHistory) {
        append(copy("\n\nHistory events restored: %1\$s", listOf(outcome.historyImportedCount)))
        if (outcome.historyDuplicateCount > 0) {
            append(copy("\nHistory already present: %1\$s", listOf(outcome.historyDuplicateCount)))
        }
        if (outcome.historyInvalidCount > 0) {
            append(copy("\nHistory could not be read: %1\$s", listOf(outcome.historyInvalidCount)))
        }
    }
    if (outcome.settingsApplied) append(copy("\n\nApp settings restored", listOf()))
    if (outcome.settingsInvalidCount > 0) {
        append(copy("\nSettings that could not be read: %1\$s", listOf(outcome.settingsInvalidCount)))
    }
    if (outcome.invalidCount > 0) {
        append(copy("\n\nSkipped (could not be read): %1\$s", listOf(outcome.invalidCount)))
        appendReasons(outcome.invalidReasons, copy)
    }
}

/**
 * Whether the import touched history at all. A rules-only file stays silent about it: reporting
 * "History events restored: 0" on a v1 backup is noise that reads like a failure.
 */
private val ImportOutcome.hasHistory: Boolean
    get() = historyImportedCount > 0 || historyDuplicateCount > 0 || historyInvalidCount > 0

private fun StringBuilder.appendReasons(reasons: List<String>, copy: CopyText) {
    reasons.take(MAX_LISTED_REASONS).forEach { append("\n• ${copy(it, emptyList())}") }
    val extra = reasons.size - MAX_LISTED_REASONS
    if (extra > 0) append(copy("\n• ...and %1\$s more", listOf(extra)))
}

private fun entriesCouldNotBeRead(count: Int, copy: CopyText): String =
    if (count == 1) copy("1 entry could not be read", listOf()) else copy("%1\$s entries could not be read", listOf(count))

private fun historyEntriesCouldNotBeRead(count: Int, copy: CopyText): String =
    if (count == 1) copy("1 history event could not be read", listOf())
    else copy("%1\$s history events could not be read", listOf(count))

private fun settingsCouldNotBeRead(count: Int, copy: CopyText): String =
    if (count == 1) copy("1 setting could not be read", listOf()) else copy("%1\$s settings could not be read", listOf(count))

/** Templates and argument lists are stable; Android supplies a resource-backed formatter. */
typealias CopyText = (String, List<Any>) -> String

private fun englishCopy(template: String, args: List<Any>): String =
    if (args.isEmpty()) template else String.format(java.util.Locale.ROOT, template, *args.toTypedArray())
