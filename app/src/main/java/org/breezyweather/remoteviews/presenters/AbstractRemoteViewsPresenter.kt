package org.breezyweather.remoteviews.presenters

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.NotificationTextColor
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit
import org.breezyweather.common.basic.models.options.unit.RelativeHumidityUnit
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.hasPermission
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.LunarHelper
import org.breezyweather.settings.ConfigStore
import org.breezyweather.settings.SettingsManager
import java.util.*

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

    class WidgetColor(context: Context, cardStyle: String, textColor: String) {
        val showCard: Boolean
        val cardColor: ColorType

        @ColorInt
        var textColor = 0
        var darkText = false

        init {
            showCard = cardStyle != "none"
            cardColor = if (cardStyle == "auto") ColorType.AUTO else if (cardStyle == "light") ColorType.LIGHT else ColorType.DARK
            if (showCard) {
                when (cardColor) {
                    ColorType.AUTO -> {
                        this.textColor = Color.TRANSPARENT
                        darkText = false
                    }
                    ColorType.LIGHT -> {
                        this.textColor = ContextCompat.getColor(context, R.color.colorTextDark)
                        darkText = true
                    }
                    else -> {
                        this.textColor = ContextCompat.getColor(context, R.color.colorTextLight)
                        darkText = false
                    }
                }
            } else if (textColor == "dark" || textColor == "auto" && isLightWallpaper(context)) {
                this.textColor = ContextCompat.getColor(context, R.color.colorTextDark)
                darkText = true
            } else {
                this.textColor = ContextCompat.getColor(context, R.color.colorTextLight)
                darkText = false
            }
        }

        val minimalIconColor: NotificationTextColor
            get() = if (showCard) {
                when (cardColor) {
                    ColorType.AUTO -> NotificationTextColor.GREY
                    ColorType.LIGHT -> NotificationTextColor.DARK
                    else -> NotificationTextColor.LIGHT
                }
            } else if (darkText) {
                NotificationTextColor.DARK
            } else {
                NotificationTextColor.LIGHT
            }

        enum class ColorType {
            LIGHT,
            DARK,
            AUTO
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
        fun getCardBackgroundId(cardColor: WidgetColor.ColorType?): Int {
            return when (cardColor) {
                WidgetColor.ColorType.AUTO -> R.drawable.widget_card_follow_system
                WidgetColor.ColorType.LIGHT -> R.drawable.widget_card_light
                else -> R.drawable.widget_card_dark
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
                    weather.current?.temperature?.getTemperature(context, temperatureUnit, 0)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$ctd$",
                    weather.current?.temperature?.getShortTemperature(context, temperatureUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$at$",
                    weather.current?.temperature?.getFeelsLikeTemperature(context, temperatureUnit, 0)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$atd$",
                    weather.current?.temperature?.getShortFeelsLikeTemperature(context, temperatureUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cwd$",
                    weather.current?.wind?.getShortWindDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$cuv$",
                    weather.current?.uV?.shortUVDescription
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$ch$",
                    weather.current?.relativeHumidity?.let {
                        RelativeHumidityUnit.PERCENT.getValueText(context, it.toInt())
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
                ).replace("\$l$", location.getCityName(context))
                .replace("\$lat$", location.latitude.toString())
                .replace("\$lon$", location.longitude.toString())
                .replace("\$ut$", weather.base.updateDate.getFormattedTime(location.timeZone, context.is12Hour))
                .replace(
                    "\$d$",
                    Date().getFormattedDate(location.timeZone, context.getString(R.string.date_format_long))
                ).replace(
                    "\$lc$",
                    LunarHelper.getLunarDate(Date()) ?: "N/A"
                ).replace(
                    "\$w$",
                    Date().getFormattedDate(location.timeZone, "EEEE")
                ).replace(
                    "\$ws$",
                    Date().getFormattedDate(location.timeZone, "EEE")
                ).replace(
                    "\$dd$",
                    weather.current?.dailyForecast
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "\$hd$",
                    weather.current?.hourlyForecast
                        ?: context.getString(R.string.null_data_text)
                ).replace("\$enter$", "\n")
            subtitle = replaceAlerts(context, subtitle, weather, location.timeZone)
            subtitle = replaceDailyWeatherSubtitle(
                context, subtitle, weather, location.timeZone, temperatureUnit, speedUnit
            )
            return subtitle
        }

        private fun replaceAlerts(
            context: Context, subtitle: String, weather: Weather, timeZone: TimeZone
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
                defaultBuilder.append(currentAlert.description)
                if (currentAlert.startDate != null) {
                    val startDateDay = currentAlert.startDate.getFormattedDate(
                        timeZone, context.getString(R.string.date_format_long)
                    )
                    defaultBuilder.append(", ")
                        .append(startDateDay)
                        .append(", ")
                        .append(currentAlert.startDate.getFormattedTime(timeZone, context.is12Hour))
                    if (currentAlert.endDate != null) {
                        defaultBuilder.append("-")
                        val endDateDay = currentAlert.endDate.getFormattedDate(
                            timeZone, context.getString(R.string.date_format_long)
                        )
                        if (startDateDay != endDateDay) {
                            defaultBuilder.append(endDateDay).append(", ")
                        }
                        defaultBuilder.append(currentAlert.endDate.getFormattedTime(timeZone, context.is12Hour))
                    }
                }
                shortBuilder.append(currentAlert.description)
            }
            return subtitle.replace("\$al$", defaultBuilder.toString())
                .replace("\$als$", shortBuilder.toString())
        }

        private fun replaceDailyWeatherSubtitle(
            context: Context, subtitleP: String, weather: Weather, timeZone: TimeZone,
            temperatureUnit: TemperatureUnit, speedUnit: SpeedUnit
        ): String {
            var subtitle = subtitleP
            for (i in 0..<SUBTITLE_DAILY_ITEM_LENGTH) {
                subtitle = subtitle.replace(
                    "$" + i + "dw$",
                    weather.dailyForecast.getOrNull(i)?.day?.weatherText
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nw$",
                    weather.dailyForecast.getOrNull(i)?.night?.weatherText
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dt$",
                    weather.dailyForecast.getOrNull(i)?.day?.temperature?.getTemperature(context, temperatureUnit, 0)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nt$",
                    weather.dailyForecast.getOrNull(i)?.night?.temperature?.getTemperature(context, temperatureUnit, 0)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dtd$",
                    weather.dailyForecast.getOrNull(i)?.day?.temperature?.getShortTemperature(context, temperatureUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ntd$",
                    weather.dailyForecast.getOrNull(i)?.night?.temperature?.getShortTemperature(context, temperatureUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dp$",
                    weather.dailyForecast.getOrNull(i)?.day?.precipitationProbability?.total?.let {
                        ProbabilityUnit.PERCENT.getValueText(context, it.toInt())
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "np$",
                    weather.dailyForecast.getOrNull(i)?.night?.precipitationProbability?.total?.let {
                        ProbabilityUnit.PERCENT.getValueText(context, it.toInt())
                    } ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "dwd$",
                    weather.dailyForecast.getOrNull(i)?.day?.wind?.getShortWindDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "nwd$",
                    weather.dailyForecast.getOrNull(i)?.night?.wind?.getShortWindDescription(context, speedUnit)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "sr$",
                    weather.dailyForecast.getOrNull(i)?.sun?.riseDate?.getFormattedTime(timeZone, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ss$",
                    weather.dailyForecast.getOrNull(i)?.sun?.setDate?.getFormattedTime(timeZone, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "mr$",
                    weather.dailyForecast.getOrNull(i)?.moon?.riseDate?.getFormattedTime(timeZone, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "ms$",
                    weather.dailyForecast.getOrNull(i)?.moon?.setDate?.getFormattedTime(timeZone, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                ).replace(
                    "$" + i + "mp$",
                    weather.dailyForecast.getOrNull(i)?.moonPhase?.getMoonPhase(context)
                        ?: context.getString(R.string.null_data_text)
                )
            }
            return subtitle
        }
    }
}
