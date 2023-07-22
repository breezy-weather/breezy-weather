package org.breezyweather.common.basic.models.options.appearance

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options.unit.CloudCoverUnit
import org.breezyweather.common.basic.models.options.unit.RelativeHumidityUnit
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.settings.SettingsManager

enum class DetailDisplay(
    override val id: String,
    @StringRes private val nameId: Int,
    @DrawableRes val iconId: Int
): BaseEnum {

    DETAIL_FEELS_LIKE("feels_like", R.string.temperature_feels_like, R.drawable.ic_device_thermostat),
    DETAIL_WIND("wind", R.string.wind, R.drawable.ic_wind),
    DETAIL_UV_INDEX("uv_index", R.string.uv_index, R.drawable.ic_uv),
    DETAIL_HUMIDITY("humidity", R.string.humidity, R.drawable.ic_humidity_percentage),
    DETAIL_DEW_POINT("dew_point", R.string.dew_point, R.drawable.ic_dew_point),
    DETAIL_PRESSURE("pressure", R.string.pressure, R.drawable.ic_gauge),
    DETAIL_VISIBILITY("visibility", R.string.visibility, R.drawable.ic_eye),
    DETAIL_CLOUD_COVER("cloud_cover", R.string.cloud_cover, R.drawable.ic_cloud),
    DETAIL_CEILING("ceiling", R.string.ceiling, R.drawable.ic_top);

    companion object {

        fun toDetailDisplayList(
            value: String?
        ) = if (value.isNullOrEmpty()) {
            ArrayList()
        } else try {
            val details = value.split("&").toTypedArray()
            val list = ArrayList<DetailDisplay>()
            for (detail in details) {
                when (detail) {
                    "feels_like" -> list.add(DETAIL_FEELS_LIKE)
                    "wind" -> list.add(DETAIL_WIND)
                    "uv_index" -> list.add(DETAIL_UV_INDEX)
                    "humidity" -> list.add(DETAIL_HUMIDITY)
                    "dew_point" -> list.add(DETAIL_DEW_POINT)
                    "pressure" -> list.add(DETAIL_PRESSURE)
                    "visibility" -> list.add(DETAIL_VISIBILITY)
                    "cloud_cover" -> list.add(DETAIL_CLOUD_COVER)
                    "ceiling" -> list.add(DETAIL_CEILING)
                }
            }

            list
        } catch (e: Exception) {
            emptyList()
        }

        fun toDetailDisplayUnlisted(
            value: String?
        ) = if (value.isNullOrEmpty()) {
            DetailDisplay.entries.toMutableList()
        } else try {
            val list = DetailDisplay.entries.toMutableList()
            val details = value.split("&").toTypedArray()
            for (detail in details) {
                when (detail) {
                    "feels_like" -> list.remove(DETAIL_FEELS_LIKE)
                    "wind" -> list.remove(DETAIL_WIND)
                    "uv_index" -> list.remove(DETAIL_UV_INDEX)
                    "humidity" -> list.remove(DETAIL_HUMIDITY)
                    "dew_point" -> list.remove(DETAIL_DEW_POINT)
                    "pressure" -> list.remove(DETAIL_PRESSURE)
                    "visibility" -> list.remove(DETAIL_VISIBILITY)
                    "cloud_cover" -> list.remove(DETAIL_CLOUD_COVER)
                    "ceiling" -> list.remove(DETAIL_CEILING)
                }
            }

            list
        } catch (e: Exception) {
            DetailDisplay.entries.toMutableList()
        }

        fun toValue(list: List<DetailDisplay>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        fun getSummary(context: Context, list: List<DetailDisplay>): String {
            val builder = StringBuilder()
            for (item in list) {
                builder.append(",").append(item.getName(context))
            }
            if (builder.isNotEmpty() && builder[0] == ',') {
                builder.deleteCharAt(0)
            }
            return builder.toString().replace(",", ", ")
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)

    fun getCurrentValue(context: Context, current: Current, isDaylight: Boolean = true): String? = when(id) {
        "feels_like" -> if (current.temperature?.feelsLikeTemperature != null) current.temperature.getFeelsLikeTemperature(context, SettingsManager.getInstance(context).temperatureUnit, 0) else null
        "wind" -> if (!current.wind?.getShortWindDescription(context, SettingsManager.getInstance(context).speedUnit).isNullOrEmpty()) current.wind?.getShortWindDescription(context, SettingsManager.getInstance(context).speedUnit) else null
        "uv_index" -> if (current.uV?.index != null && (isDaylight || current.uV.index > 0)) current.uV.getShortUVDescription(context) else null
        "humidity" -> if (current.relativeHumidity != null) RelativeHumidityUnit.PERCENT.getValueText(
            context, current.relativeHumidity.toInt()
        ) else null
        "dew_point" -> if (current.dewPoint != null) SettingsManager.getInstance(context).temperatureUnit.getValueText(
            context, current.dewPoint, 0
        ) else null
        "pressure" -> if (current.pressure != null) SettingsManager.getInstance(context).pressureUnit.getValueText(
            context, current.pressure
        ) else null
        "visibility" -> if (current.visibility != null) SettingsManager.getInstance(context).distanceUnit.getValueText(
            context, current.visibility
        ) else null
        "cloud_cover" -> if (current.cloudCover != null) CloudCoverUnit.PERCENT.getValueText(
            context,
            current.cloudCover
        ) else null
        "ceiling" -> if (current.ceiling != null) SettingsManager.getInstance(context).distanceUnit.getValueText(
            context,
            current.ceiling
        ) else null
        else -> null
    }
}