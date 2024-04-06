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

package org.breezyweather.remoteviews.presenters

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.getShortDescription
import org.breezyweather.domain.weather.model.getShortUVDescription
import org.breezyweather.domain.weather.model.getSummary
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.domain.weather.model.pollensWithConcentration
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.ConfigStore
import org.breezyweather.settings.SettingsManager
import java.text.NumberFormat
import java.util.Date

abstract class AbstractRemoteViewsPresenter {
    class WidgetConfig {
        var viewStyle: String? = null
        var cardStyle: String? = null
        var cardAlpha = 0
        var textColor: String? = null
        var textSize = 0
        var hideSubtitle = false
        var subtitleData: String? = null
        var clockFont: String? = null
        var hideLunar = false
        var alignEnd = false
    }

    class WidgetColor(val context: Context, cardStyle: String, textColor: String, dayTime: Boolean) {
        val backgroundType: WidgetBackgroundType
        val textType: NotificationTextColor

        val showCard: Boolean
        val isLightThemed: Boolean

        @ColorInt
        var textColor = 0
            private set

        init {
            backgroundType = WidgetBackgroundType.find(cardStyle)
            showCard = backgroundType != WidgetBackgroundType.NONE
            isLightThemed = when (backgroundType) {
                WidgetBackgroundType.LIGHT -> true
                WidgetBackgroundType.DARK -> false
                WidgetBackgroundType.AUTO -> MainThemeColorProvider.isLightTheme(context, dayTime)
                WidgetBackgroundType.NONE -> isLightWallpaper(context)
            }
            textType = when (backgroundType) {
                WidgetBackgroundType.NONE -> {
                    if(textColor == "dark" || textColor == "auto" && isLightWallpaper(context)) {
                        NotificationTextColor.DARK
                    } else NotificationTextColor.LIGHT
                }
                else -> {
                    if (isLightThemed) NotificationTextColor.DARK else NotificationTextColor.LIGHT
                }
            }
            this.textColor = ContextCompat.getColor(
                context,
                if (textType == NotificationTextColor.DARK) R.color.colorTextDark else R.color.colorTextLight
            )
        }

        val minimalIconColor: NotificationTextColor
            get() =
                when (backgroundType) {
                    WidgetBackgroundType.AUTO -> NotificationTextColor.GREY
                    else -> textType
                }

        enum class WidgetBackgroundType(val id: String) {
            LIGHT("light"),
            DARK("dark"),
            AUTO("auto"),
            NONE("none");

            companion object {
                fun find(id: String): WidgetBackgroundType = entries.find { it.id == id }
                    ?: throw Exception("Invalid WidgetBackgroundType id: $id")
            }
        }
    }

    companion object {
        private const val SUBTITLE_DAILY_ITEM_LENGTH = 5

        fun getWidgetConfig(context: Context, configStoreName: String): WidgetConfig {
            val widgetConfig = WidgetConfig()
            val configStore = ConfigStore(context, configStoreName)
            widgetConfig.viewStyle = configStore.getString(
                context.getString(R.string.key_view_type),
                "rectangle"
            )
            widgetConfig.cardStyle = configStore.getString(
                context.getString(R.string.key_card_style),
                "none"
            )
            widgetConfig.cardAlpha = configStore.getInt(
                context.getString(R.string.key_card_alpha),
                100
            )
            widgetConfig.textColor = configStore.getString(
                context.getString(R.string.key_text_color),
                "light"
            )
            widgetConfig.textSize = configStore.getInt(
                context.getString(R.string.key_text_size),
                100
            )
            widgetConfig.hideSubtitle = configStore.getBoolean(
                context.getString(R.string.key_hide_subtitle),
                false
            )
            widgetConfig.subtitleData = configStore.getString(
                context.getString(R.string.key_subtitle_data),
                "time"
            )
            widgetConfig.clockFont = configStore.getString(
                context.getString(R.string.key_clock_font),
                "light"
            )
            widgetConfig.hideLunar = configStore.getBoolean(
                context.getString(R.string.key_hide_lunar),
                false
            )
            widgetConfig.alignEnd = configStore.getBoolean(
                context.getString(R.string.key_align_end),
                false
            )
            return widgetConfig
        }

        @SuppressLint("MissingPermission")
        fun isLightWallpaper(context: Context): Boolean {
            return try {
                val manager = WallpaperManager.getInstance(context) ?: return false
                if (!context.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) return false
                val drawable = manager.drawable
                if (drawable !is BitmapDrawable) {
                    false
                } else ColorUtils.isLightColor(
                    ColorUtils.bitmapToColorInt(drawable.bitmap)
                )
            } catch (ignore: Exception) {
                false
            }
        }

        @DrawableRes
        fun getCardBackgroundId(color: WidgetColor): Int {
            return when (color.backgroundType) {
                WidgetColor.WidgetBackgroundType.NONE -> {
                    throw IllegalArgumentException("Trying to get widget background when background type is NONE")
                }
                else -> {
                    if (color.isLightThemed) R.drawable.widget_card_light
                    else R.drawable.widget_card_dark
                }
            }
        }

        fun isWeekIconDaytime(mode: WidgetWeekIconMode?, daytime: Boolean): Boolean {
            return when (mode) {
                WidgetWeekIconMode.DAY -> true
                WidgetWeekIconMode.NIGHT -> false
                else -> daytime
            }
        }

        @SuppressLint("InlinedApi")
        fun getWeatherPendingIntent(context: Context, location: Location?, requestCode: Int): PendingIntent {
            return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityIntent(location),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        @SuppressLint("InlinedApi")
        fun getDailyForecastPendingIntent(
            context: Context, location: Location?, index: Int, requestCode: Int
        ): PendingIntent {
            return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityShowDailyForecastIntent(location, index),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        @SuppressLint("InlinedApi")
        fun getAlarmPendingIntent(context: Context, requestCode: Int): PendingIntent {
            return PendingIntent.getActivity(
                context,
                requestCode,
                Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        @SuppressLint("InlinedApi")
        fun getCalendarPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val builder = CalendarContract.CONTENT_URI.buildUpon()
            builder.appendPath("time")
            ContentUris.appendId(builder, System.currentTimeMillis())
            return PendingIntent.getActivity(
                context,
                requestCode,
                Intent(Intent.ACTION_VIEW).setData(builder.build()),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun getCustomSubtitle(
            context: Context, subtitleP: String?, location: Location, weather: Weather
        ): String {
            if (subtitleP.isNullOrEmpty()) return ""
            val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
            //val precipitationUnit = getInstance(context).precipitationUnit
            val pressureUnit = SettingsManager.getInstance(context).pressureUnit
            val distanceUnit = SettingsManager.getInstance(context).distanceUnit
            val speedUnit = SettingsManager.getInstance(context).speedUnit
            var subtitle = subtitleP
                .replace(
                    "\$cw$",
                    weather.current?.weatherText
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$ct$",
                    weather.current?.temperature?.temperature?.let {
                        temperatureUnit.getValueText(context, it, 0)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$ctd$",
                    weather.current?.temperature?.temperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$at$",
                    weather.current?.temperature?.feelsLikeTemperature?.let {
                        temperatureUnit.getValueText(context, it, 0)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$atd$",
                    weather.current?.temperature?.feelsLikeTemperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cwd$",
                    weather.current?.wind?.getShortDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$caqi$",
                    if (weather.current?.airQuality?.isIndexValid == true) {
                        weather.current!!.airQuality!!.getIndex().toString() + " (" +
                                weather.current!!.airQuality!!.getName(context) + ")"
                    } else context.getString(R.string.null_data_text)
                ).replace(
                    "\$cuv$",
                    weather.current?.uV?.getShortUVDescription(context)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$ch$",
                    weather.current?.relativeHumidity?.let {
                        NumberFormat.getPercentInstance(context.currentLocale).apply {
                            maximumFractionDigits = 0
                        }.format(it.div(100.0))
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cps$",
                    weather.current?.pressure?.let {
                        pressureUnit.getValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cv$",
                    weather.current?.visibility?.let {
                        distanceUnit.getValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cdp$",
                    weather.current?.dewPoint?.let {
                        temperatureUnit.getValueText(context, it, 0)
                    } ?: context.getString(R.string.null_data_text)
                ).replace("\$l$", location.getPlace(context))
                .replace("\$lat$", location.latitude.toString())
                .replace("\$lon$", location.longitude.toString())
                .replace("\$ut$", weather.base.refreshTime?.getFormattedTime(
                    location, context, context.is12Hour
                ) ?: context.getString(R.string.null_data_text))
                .replace(
                    "\$d$",
                    Date().getFormattedMediumDayAndMonth(location, context)
                ).replace(
                    "\$lc$",
                    Date().getFormattedMediumDayAndMonthInAdditionalCalendar(location, context) ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$w$",
                    Date().getFormattedDate("EEEE", location, context)
                ).replace(
                    "\$ws$",
                    Date().getFormattedDate("EEE", location, context)
                ).replace(
                    "\$dd$",
                    weather.current?.dailyForecast
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$hd$",
                    weather.current?.hourlyForecast
                        ?: context.getString(R.string.null_data_text)
                ).replace("\$enter$", "\n")
            subtitle = replaceAlerts(context, subtitle, location, weather)
            subtitle = replaceDailyWeatherSubtitle(
                context, subtitle, location, weather, temperatureUnit, speedUnit
            )
            return subtitle
        }

        private fun replaceAlerts(
            context: Context, subtitle: String, location: Location, weather: Weather
        ): String {
            val defaultBuilder = StringBuilder()
            val shortBuilder = StringBuilder()
            val currentAlertList = weather.currentAlertList
            currentAlertList.forEach { currentAlert ->
                if (defaultBuilder.toString().isNotEmpty()) {
                    defaultBuilder.append("\n")
                }
                if (shortBuilder.toString().isNotEmpty()) {
                    shortBuilder.append("\n")
                }
                defaultBuilder.append(
                    currentAlert.headline?.ifEmpty {
                        context.getString(R.string.alert)
                    } ?: context.getString(R.string.alert)
                )
                currentAlert.startDate?.let { startDate ->
                    val startDateDay = startDate.getFormattedMediumDayAndMonth(location, context)
                    defaultBuilder.append(context.getString(R.string.comma_separator))
                        .append(startDateDay)
                        .append(context.getString(R.string.comma_separator))
                        .append(startDate.getFormattedTime(location, context, context.is12Hour))
                    currentAlert.endDate?.let { endDate ->
                        defaultBuilder.append("-")
                        val endDateDay = endDate.getFormattedMediumDayAndMonth(location, context)
                        if (startDateDay != endDateDay) {
                            defaultBuilder.append(endDateDay)
                                .append(context.getString(R.string.comma_separator))
                        }
                        defaultBuilder.append(
                            endDate.getFormattedTime(location, context, context.is12Hour)
                        )
                    }
                }
                shortBuilder.append(
                    currentAlert.headline?.ifEmpty {
                        context.getString(R.string.alert)
                    } ?: context.getString(R.string.alert)
                )
            }
            return subtitle.replace("\$al$", defaultBuilder.toString())
                .replace("\$als$", shortBuilder.toString())
        }

        private fun replaceDailyWeatherSubtitle(
            context: Context, subtitleP: String, location: Location, weather: Weather,
            temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit
        ): String {
            var subtitle = subtitleP
            for (i in 0 until SUBTITLE_DAILY_ITEM_LENGTH) {
                subtitle = subtitle.replace(
                    "$" + i + "dw$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.weatherText
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nw$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.weatherText
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dt$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.temperature?.temperature?.let {
                        temperatureUnit.getValueText(context, it, 0)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nt$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.temperature?.temperature?.let {
                        temperatureUnit.getValueText(context, it, 0)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dtd$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.temperature?.temperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ntd$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.temperature?.temperature?.let {
                        temperatureUnit.getShortValueText(context, it)
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dp$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.precipitationProbability?.total?.let {
                        NumberFormat.getPercentInstance(context.currentLocale).apply {
                            maximumFractionDigits = 0
                        }.format(it.div(100.0))
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "np$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.precipitationProbability?.total?.let {
                        NumberFormat.getPercentInstance(context.currentLocale).apply {
                            maximumFractionDigits = 0
                        }.format(it.div(100.0))
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dwd$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.day?.wind?.getShortDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nwd$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.night?.wind?.getShortDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "aqi$",
                    if (weather.dailyForecastStartingToday.getOrNull(i)?.airQuality?.isIndexValid == true) {
                        weather.dailyForecastStartingToday[i].airQuality!!.getIndex().toString() + " (" +
                            weather.dailyForecastStartingToday[i].airQuality!!.getName(context) + ")"
                    } else context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "pis$",
                    if (weather.dailyForecastStartingToday.getOrNull(i)?.pollen?.pollensWithConcentration?.isNotEmpty() == true) {
                        // TODO #782: Tricky as we need to handle PollenIndexSource when applicable
                        // TODO #782: Add to AbstractWidgetConfigActivity::subtitleCustomKeywords
                        weather.dailyForecastStartingToday[i].pollen!!.getSummary(context)
                    } else context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "sr$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.sun?.riseDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ss$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.sun?.setDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "mr$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.moon?.riseDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ms$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.moon?.setDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "mp$",
                    weather.dailyForecastStartingToday.getOrNull(i)?.moonPhase?.getDescription(context)
                        ?: context.getString(R.string.null_data_text)
                )
            }
            return subtitle
        }
    }
}
