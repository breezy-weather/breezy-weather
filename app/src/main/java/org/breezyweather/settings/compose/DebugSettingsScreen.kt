package org.breezyweather.settings.compose

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.utils.CrashLogUtils
import org.breezyweather.settings.activities.WorkerInfoActivity
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView

@Composable
fun DebugSettingsScreen(
    context: Context,
    paddingValues: PaddingValues
) {
    val scope = rememberCoroutineScope()
    PreferenceScreen(paddingValues = paddingValues) {
        clickablePreferenceItem(R.string.settings_debug_dump_crash_logs_title) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_debug_dump_crash_logs_summary
            ) {
                scope.launch {
                    CrashLogUtils(context).dumpLogs()
                }
            }
        }

        bottomInsetItem()
    }
}