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

package org.breezyweather.theme.compose.night

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme

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
    inversePrimary = night_md_theme_dark_inversePrimary
)

@RequiresApi(Build.VERSION_CODES.S)
fun dynamicDarkColors(
    context: Context,
): ColorScheme = dynamicDarkColorScheme(context).copy(
    outline = night_md_theme_dark_outline
)
