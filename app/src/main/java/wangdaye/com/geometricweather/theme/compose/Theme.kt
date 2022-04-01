package wangdaye.com.geometricweather.theme.compose

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import wangdaye.com.geometricweather.theme.compose.day.LightThemeColors
import wangdaye.com.geometricweather.theme.compose.night.DarkThemeColors

class GeometricWeatherColors {

    companion object {
        val LightAccent = Color(0xff212121)
        val DarkAccent = Color(0xfffafafa)

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

        val LightSeparator = Color(0xfff1f1f1)
        val DarkSeparator = Color(0xff363636)

        val LightRoot = Color.White
        val DarkRoot = Color.Black

        val LightSurface = Color(0xfffafafa)
        var DarkSurface = Color(0xff1a1a1a)

        val AlertText = Color(0xfffcc96b)

        val Ripple = Color(0x40808080)

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

        val StrikingRed = Level4

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

@Composable
fun GeometricWeatherTheme(
    lightTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lightTheme ->
                dynamicLightColorScheme(LocalContext.current)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !lightTheme ->
                dynamicDarkColorScheme(LocalContext.current)
            lightTheme ->
                LightThemeColors
            else ->
                DarkThemeColors
        },
        typography = GeometricWeatherTypography,
        content = content
    )
}

//@Composable
//fun AppTheme(
//    useDarkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable() () -> Unit
//) {
//    val colors = if (!useDarkTheme) {
//        LightThemeColors
//    } else {
//        DarkThemeColors
//    }
//
//    MaterialTheme(
//        colorScheme = colors,
//        typography = AppTypography,
//        content = content
//    )
//}
//
//data class CustomColor(val name:String, val color:Color, val harmonized: Boolean, var roles: ColorRoles)
//data class ExtendedColors(val colors: Array<CustomColor>)
//
//
//fun setupErrorColors(colorScheme: ColorScheme, isLight: Boolean): ColorScheme {
//    val harmonizedError =
//        MaterialColors.harmonize(error, colorScheme.primary)
//    val roles = MaterialColors.getColorRoles(harmonizedError, isLight)
//    //returns a colorScheme with newly harmonized error colors
//    return colorScheme.copy(
//        error = roles.color,
//        onError = roles.onColor,
//        errorContainer = roles.colorContainer,
//        onErrorContainer = roles.onColorContainer
//    )
//}
//val initializeExtended = ExtendedColors(
//    arrayOf(
//    ))
//
//fun setupCustomColors(
//    colorScheme: ColorScheme,
//    isLight: Boolean
//): ExtendedColors {
//    initializeExtended.colors.forEach {customColor ->
//        // Retrieve record
//        val shouldHarmonize = customColor.harmonized
//        // Blend or not
//        if (shouldHarmonize) {
//            val blendedColor =
//                MaterialColors.harmonize(customColor.color, colorScheme.primary)
//            customColor.roles = MaterialColors.getColorRoles(blendedColor, isLight)
//        } else {
//            customColor.roles = MaterialColors.getColorRoles(customColor.color, isLight)
//        }
//    }
//    return initializeExtended
//}
//
//val LocalExtendedColors = staticCompositionLocalOf {
//    initializeExtended
//}
//
//
//@RequiresApi(Build.VERSION_CODES.S)
//@Composable
//fun HarmonizedTheme(
//    useDarkTheme: Boolean = isSystemInDarkTheme(),
//    isDynamic: Boolean = true,
//    content: @Composable() () -> Unit
//) {
//    val colors = if (isDynamic) {
//        val context = LocalContext.current
//        if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//    } else {
//        if (useDarkTheme) DarkThemeColors else LightThemeColors
//    }
//    val colorsWithHarmonizedError = if(errorHarmonize) setupErrorColors(colors, !useDarkTheme) else colors
//
//    val extendedColors = setupCustomColors(colors, !useDarkTheme)
//    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
//        MaterialTheme(
//            colorScheme = colorsWithHarmonizedError,
//            typography = AppTypography,
//            content = content
//        )
//    }
//}
//
//object Extended {
//}