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

package org.breezyweather.theme.compose.day

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme

val LightThemeColors = lightColorScheme(
    primary = day_md_theme_light_primary,
    onPrimary = day_md_theme_light_onPrimary,
    primaryContainer = day_md_theme_light_primaryContainer,
    onPrimaryContainer = day_md_theme_light_onPrimaryContainer,
    secondary = day_md_theme_light_secondary,
    onSecondary = day_md_theme_light_onSecondary,
    secondaryContainer = day_md_theme_light_secondaryContainer,
    onSecondaryContainer = day_md_theme_light_onSecondaryContainer,
    tertiary = day_md_theme_light_tertiary,
    onTertiary = day_md_theme_light_onTertiary,
    tertiaryContainer = day_md_theme_light_tertiaryContainer,
    onTertiaryContainer = day_md_theme_light_onTertiaryContainer,
    error = day_md_theme_light_error,
    errorContainer = day_md_theme_light_errorContainer,
    onError = day_md_theme_light_onError,
    onErrorContainer = day_md_theme_light_onErrorContainer,
    background = day_md_theme_light_background,
    onBackground = day_md_theme_light_onBackground,
    surface = day_md_theme_light_surface,
    onSurface = day_md_theme_light_onSurface,
    surfaceVariant = day_md_theme_light_surfaceVariant,
    onSurfaceVariant = day_md_theme_light_onSurfaceVariant,
    outline = day_md_theme_light_outline,
    inverseOnSurface = day_md_theme_light_inverseOnSurface,
    inverseSurface = day_md_theme_light_inverseSurface,
    inversePrimary = day_md_theme_light_inversePrimary
)

@RequiresApi(Build.VERSION_CODES.S)
fun dynamicLightColors(
    context: Context,
): ColorScheme = dynamicLightColorScheme(context).copy(
    outline = day_md_theme_light_outline
)
