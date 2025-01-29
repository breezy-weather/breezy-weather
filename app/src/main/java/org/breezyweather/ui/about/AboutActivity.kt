package org.breezyweather.ui.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

@AndroidEntryPoint
class AboutActivity : GeoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                AboutScreen(
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }
    }
}
