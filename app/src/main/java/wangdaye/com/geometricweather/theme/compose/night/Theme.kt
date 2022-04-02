package wangdaye.com.geometricweather.theme.compose.night

import android.R
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color

val DarkThemeColors = darkColorScheme(

	primary = night_md_theme_dark_primary,
	onPrimary = night_md_theme_dark_onPrimary,
	primaryContainer = night_md_theme_dark_primaryContainer,
	onPrimaryContainer = night_md_theme_dark_onPrimaryContainer,
	secondary = night_md_theme_dark_secondary,
	onSecondary = night_md_theme_dark_onSecondary,
	secondaryContainer = night_md_theme_dark_secondaryContainer,
	onSecondaryContainer = night_md_theme_dark_onSecondaryContainer,
	tertiary = night_md_theme_dark_tertiary,
	onTertiary = night_md_theme_dark_onTertiary,
	tertiaryContainer = night_md_theme_dark_tertiaryContainer,
	onTertiaryContainer = night_md_theme_dark_onTertiaryContainer,
	error = night_md_theme_dark_error,
	errorContainer = night_md_theme_dark_errorContainer,
	onError = night_md_theme_dark_onError,
	onErrorContainer = night_md_theme_dark_onErrorContainer,
	background = night_md_theme_dark_background,
	onBackground = night_md_theme_dark_onBackground,
	surface = night_md_theme_dark_surface,
	onSurface = night_md_theme_dark_onSurface,
	surfaceVariant = night_md_theme_dark_surfaceVariant,
	onSurfaceVariant = night_md_theme_dark_onSurfaceVariant,
	outline = night_md_theme_dark_outline,
	inverseOnSurface = night_md_theme_dark_inverseOnSurface,
	inverseSurface = night_md_theme_dark_inverseSurface,
	inversePrimary = night_md_theme_dark_inversePrimary,
)

@RequiresApi(Build.VERSION_CODES.S)
fun dynamicDarkColors(
	context: Context
): ColorScheme = dynamicDarkColorScheme(context).copy(
	outline = Color(context.resources.getColor(R.color.system_neutral1_800, context.theme))
)