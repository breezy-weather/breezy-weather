package org.breezyweather.ui.daily

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

@AndroidEntryPoint
class DailyActivity : GeoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                DailyWeatherScreen(
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID"
        const val KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX"
    }
}
