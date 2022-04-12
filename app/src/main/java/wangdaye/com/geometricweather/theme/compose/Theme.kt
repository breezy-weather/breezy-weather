package wangdaye.com.geometricweather.theme.compose

import android.os.Build
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import wangdaye.com.geometricweather.theme.compose.day.LightThemeColors
import wangdaye.com.geometricweather.theme.compose.day.dynamicLightColors
import wangdaye.com.geometricweather.theme.compose.night.DarkThemeColors
import wangdaye.com.geometricweather.theme.compose.night.dynamicDarkColors

class GeometricWeatherColors {

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

        val DarkText = Color(0xff424242)
        val DarkText2nd = Color(0xff5f6267)
        val GreyText = Color(0xff9e9e9e)
        val GreyText2nd = Color(0xffbdbdbd)
        val LightText = Color(0xfffafafa)
        val LightText2nd = Color(0xfff5f5f5)

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
        val DarkSubtitleText = GreyText
    }
}

private val DayColors = GeometricWeatherDayNightColors(
    titleColor = GeometricWeatherColors.LightTitleText,
    bodyColor = GeometricWeatherColors.LightContentText,
    captionColor = GeometricWeatherColors.LightSubtitleText,
    isDark = false,
)
private val NightColors = GeometricWeatherDayNightColors(
    titleColor = GeometricWeatherColors.DarkTitleText,
    bodyColor = GeometricWeatherColors.DarkContentText,
    captionColor = GeometricWeatherColors.DarkSubtitleText,
    isDark = true,
)

@Composable
fun GeometricWeatherTheme(
    lightTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lightTheme ->
            dynamicLightColors(LocalContext.current)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lightTheme ->
            dynamicDarkColors(LocalContext.current)
        lightTheme ->
            LightThemeColors
        else ->
            DarkThemeColors
    }
    val colors = if (lightTheme) DayColors else NightColors

    ProvideGeometricWeatherDayNightColors(colors = colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = GeometricWeatherTypography,
            content = content
        )
    }
}

@Composable
fun ProvideGeometricWeatherDayNightColors(
    colors: GeometricWeatherDayNightColors,
    content: @Composable () -> Unit
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
    val colors: GeometricWeatherDayNightColors
        @Composable
        get() = LocalDayNightColors.current
}

private val LocalDayNightColors = staticCompositionLocalOf<GeometricWeatherDayNightColors> {
    error("No GeometricWeatherDayNightColors provided")
}

@Stable
class GeometricWeatherDayNightColors(
    titleColor: Color,
    bodyColor: Color,
    captionColor: Color,
    isDark: Boolean
) {
    var titleColor by mutableStateOf(titleColor)
        private set
    var bodyColor by mutableStateOf(bodyColor)
        private set
    var captionColor by mutableStateOf(captionColor)
        private set
    var isDark by mutableStateOf(isDark)
        private set

    fun update(other: GeometricWeatherDayNightColors) {
        titleColor = other.titleColor
        bodyColor = other.bodyColor
        captionColor = other.captionColor
        isDark = other.isDark
    }

    fun copy(): GeometricWeatherDayNightColors = GeometricWeatherDayNightColors(
        titleColor = titleColor,
        bodyColor = bodyColor,
        captionColor = captionColor,
        isDark = isDark,
    )
}

@Composable
fun rememberThemeRipple(
    bounded: Boolean = true
) = rememberRipple(
    color = MaterialTheme.colorScheme.primary,
    bounded = bounded
)