package com.astraedus.nudge.ui.localization

import kotlinx.coroutines.flow.MutableStateFlow

/** Refresh non-Activity surfaces after AppCompat recreates the main Activity. No polling or I/O. */
object LocaleUpdates {
    val configurationTags = MutableStateFlow("")
}
