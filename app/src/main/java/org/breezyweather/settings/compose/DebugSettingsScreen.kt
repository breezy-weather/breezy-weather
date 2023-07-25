package org.breezyweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import io.objectbox.android.Admin
import kotlinx.coroutines.launch
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.utils.CrashLogUtils
import org.breezyweather.db.ObjectBox
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView

@Composable
fun DebugSettingsScreen(
    context: Context,
    paddingValues: PaddingValues
) {
    val scope = rememberCoroutineScope()
    val isObjectBrowserRunning = remember { mutableStateOf(ObjectBox.boxStore.isObjectBrowserRunning) }

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

        if (BreezyWeather.instance.debugMode) {
            clickablePreferenceItem(R.string.settings_debug_force_weather_update) { id ->
                PreferenceView(
                    title = stringResource(id),
                    summary = "Execute job for debugging purpose"
                ) {
                    WeatherUpdateJob.startNow(context)
                }
            }
            if (!isObjectBrowserRunning.value) {
                clickablePreferenceItem(R.string.settings_debug_start_objectbox_admin) { id ->
                    PreferenceView(
                        title = stringResource(id),
                        summary = "Allows to view the Objects and schema of database inside the web browser"
                    ) {
                        val started = Admin(ObjectBox.boxStore).start(context.applicationContext)
                        if (started) {
                            isObjectBrowserRunning.value = true
                        }
                    }
                }
            }
        }

        bottomInsetItem()
    }
}