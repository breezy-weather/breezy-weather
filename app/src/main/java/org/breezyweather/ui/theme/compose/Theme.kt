/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.theme.compose

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.breezyweather.ui.theme.compose.day.LightThemeColors
import org.breezyweather.ui.theme.compose.day.dynamicLightColors
import org.breezyweather.ui.theme.compose.night.DarkThemeColors
import org.breezyweather.ui.theme.compose.night.dynamicDarkColors

class BreezyWeatherColors {

    companion object {
        val LightPrimary1 = Color(0xffcfebf0)
        val LightPrimary2 = Color(0xffb6e3e7)
        val LightPrimary3 = Color(0xff96d6db)
        val LightPrimary4 = Color(0xff7ac7d3)
        val LightPrimary5 = Color(0xff75becb)

        val DarkPrimary1 = Color(0xff4b5073)
        val DarkPrimary2 = Color(0xff343851)
        val DarkPrimary3 = Color(0xff2c2f43)
        val DarkPrimary4 = Color(0xff20222f)
        val DarkPrimary5 = Color(0xff1a1b22)

        val DarkText = Color(0xff000000)
        val DarkText2nd = Color(0xff666666)
        val GreyText = Color(0xff4d4d4d)
        val GreyText2nd = Color(0xffb2b2b2)
        val LightText = Color(0xffffffff)
        val LightText2nd = Color(0xff999999)

        val Level1 = Color(0xff72d572)
        val Level2 = Color(0xffffca28)
        val Level3 = Color(0xffffa726)
        val Level4 = Color(0xffe52f35)
        val Level5 = Color(0xff99004c)
        val Level6 = Color(0xff7e0023)

        val WeatherSourceACCU = Color(0xffef5823)
        val WeatherSourceCN = Color(0xff033566)
        val WeatherSourceCaiYun = Color(0xff5ebb8e)

        val LightTitleText = DarkText
        val DarkTitleText = LightText

        val LightContentText = DarkText2nd
        val DarkContentText = LightText2nd

        val LightSubtitleText = GreyText2nd
        val DarkSubtitleText = LightText2nd
    }
}

private val DayColors = BreezyWeatherDayNightColors(
    titleColor = BreezyWeatherColors.LightTitleText,
    bodyColor = BreezyWeatherColors.LightContentText,
    captionColor = BreezyWeatherColors.LightSubtitleText,
    isDark = false
)
private val NightColors = BreezyWeatherDayNightColors(
    titleColor = BreezyWeatherColors.DarkTitleText,
    bodyColor = BreezyWeatherColors.DarkContentText,
    captionColor = BreezyWeatherColors.DarkSubtitleText,
    isDark = true
)

@Composable
fun BreezyWeatherTheme(
    lightTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lightTheme -> dynamicLightColors(LocalContext.current)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lightTheme -> dynamicDarkColors(LocalContext.current)
        lightTheme -> LightThemeColors
        else -> DarkThemeColors
    }
    val colors = if (lightTheme) DayColors else NightColors

    ProvideBreezyWeatherDayNightColors(colors = colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = BreezyWeatherTypography,
            content = content
        )
    }
}

@Composable
fun ProvideBreezyWeatherDayNightColors(
    colors: BreezyWeatherDayNightColors,
    content: @Composable () -> Unit,
) {
    val value = remember {
        // Explicitly creating a new object here so we don't mutate the initial [colors]
        // provided, and overwrite the values set in it.
        colors.copy()
    }
    value.update(colors)
    CompositionLocalProvider(
        LocalDayNightColors provides value,
        content = content
    )
}

object DayNightTheme {
    val colors: BreezyWeatherDayNightColors
        @Composable
        get() = LocalDayNightColors.current
}

private val LocalDayNightColors = staticCompositionLocalOf<BreezyWeatherDayNightColors> {
    error("No BreezyWeatherDayNightColors provided")
}

@Stable
class BreezyWeatherDayNightColors(
    titleColor: Color,
    bodyColor: Color,
    captionColor: Color,
    isDark: Boolean,
) {
    var titleColor by mutableStateOf(titleColor)
        private set
    var bodyColor by mutableStateOf(bodyColor)
        private set
    var captionColor by mutableStateOf(captionColor)
        private set
    var isDark by mutableStateOf(isDark)
        private set

    fun update(other: BreezyWeatherDayNightColors) {
        titleColor = other.titleColor
        bodyColor = other.bodyColor
        captionColor = other.captionColor
        isDark = other.isDark
    }

    fun copy(): BreezyWeatherDayNightColors = BreezyWeatherDayNightColors(
        titleColor = titleColor,
        bodyColor = bodyColor,
        captionColor = captionColor,
        isDark = isDark
    )
}

@Composable
fun themeRipple(
    bounded: Boolean = true,
) = ripple(
    color = MaterialTheme.colorScheme.primary,
    bounded = bounded
)
