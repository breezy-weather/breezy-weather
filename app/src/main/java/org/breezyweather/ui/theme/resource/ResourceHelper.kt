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

package org.breezyweather.ui.theme.resource

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.options.NotificationTextColor
import org.breezyweather.ui.theme.resource.providers.DefaultResourceProvider
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.TemperatureUnit

object ResourceHelper {
    fun getWeatherIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
    ): Drawable {
        return provider.getWeatherIcon(code, dayTime)
    }

    @Size(3)
    fun getWeatherIcons(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
    ): Array<Drawable?> {
        return provider.getWeatherIcons(code, dayTime)
    }

    @Size(3)
    fun getWeatherAnimators(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
    ): Array<Animator?> {
        return provider.getWeatherAnimators(code, dayTime)
    }

    fun getWidgetNotificationIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
        minimal: Boolean,
        textColor: String?,
    ): Drawable {
        if (minimal) {
            return when (textColor) {
                "light" -> provider.getMinimalLightIcon(code, dayTime)
                "dark" -> provider.getMinimalDarkIcon(code, dayTime)
                else -> provider.getMinimalGreyIcon(code, dayTime)
            }
        }
        return provider.getWeatherIcon(code, dayTime)
    }

    fun getWidgetNotificationIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
        minimal: Boolean,
        darkText: Boolean,
    ): Drawable {
        return getWidgetNotificationIcon(provider, code, dayTime, minimal, if (darkText) "dark" else "light")
    }

    fun getWidgetNotificationIconUri(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
        minimal: Boolean,
        textColor: NotificationTextColor?,
    ): Uri {
        if (minimal) {
            return when (textColor) {
                NotificationTextColor.LIGHT -> provider.getMinimalLightIconUri(code, dayTime)
                NotificationTextColor.DARK -> provider.getMinimalDarkIconUri(code, dayTime)
                else -> provider.getMinimalGreyIconUri(code, dayTime)
            }
        }
        return provider.getWeatherIconUri(code, dayTime)
    }

    fun getMinimalXmlIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        daytime: Boolean,
    ): Drawable {
        return provider.getMinimalXmlIcon(code, daytime)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getMinimalIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        daytime: Boolean,
    ): Icon {
        return provider.getMinimalIcon(code, daytime)
    }

    @DrawableRes
    fun getDefaultMinimalXmlIconId(
        code: WeatherCode?,
        daytime: Boolean,
    ): Int {
        if (code == null) {
            return R.drawable.weather_clear_day_mini_xml
        }
        val id = DefaultResourceProvider().getMinimalXmlIconId(code, daytime)
        return if (id == 0) R.drawable.weather_clear_day_mini_xml else id
    }

    fun getShortcutsIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
    ): Drawable {
        return provider.getShortcutsIcon(code, dayTime)
    }

    fun getShortcutsForegroundIcon(
        provider: ResourceProvider,
        code: WeatherCode,
        dayTime: Boolean,
    ): Drawable {
        return provider.getShortcutsForegroundIcon(code, dayTime)
    }

    fun getSunDrawable(provider: ResourceProvider): Drawable {
        return provider.sunDrawable
    }

    fun getMoonDrawable(provider: ResourceProvider): Drawable {
        return provider.moonDrawable
    }

    fun createTempBitmap(context: Context, temp: Temperature, temperatureUnit: TemperatureUnit): Bitmap {
        val temperatureFormatted = temp.formatMeasure(
            context,
            temperatureUnit,
            valueWidth = UnitWidth.NARROW,
            unitWidth = UnitWidth.NARROW
        )

        val iconSize = 72
        val bounds = Rect()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = iconSize.toFloat()
            typeface = ResourcesCompat.getFont(context, R.font.asap_condensed_bold)
            getTextBounds(temperatureFormatted, 0, temperatureFormatted.length, bounds)
        }

        // Consider leading whitespace and descenders to properly center the text.
        // Thanks to https://stackoverflow.com/a/32081250.
        val bitmap = createBitmap(bounds.right, iconSize)
        val canvas = Canvas(bitmap)
        canvas.drawText(
            temperatureFormatted,
            (bitmap.getWidth() - bounds.width()) / 2f - bounds.left,
            (bitmap.getHeight() + bounds.height()) / 2f - bounds.bottom,
            paint
        )

        return bitmap
    }
}
