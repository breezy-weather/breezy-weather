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

package org.breezyweather.common.basic.models.options.appearance

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import breezyweather.domain.location.model.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum

enum class DetailScreen(
    override val id: String,
    @StringRes val nameId: Int,
    @DrawableRes val iconId: Int,
) : BaseEnum {

    TAG_CONDITIONS("conditions", R.string.conditions, R.drawable.ic_device_thermostat),
    TAG_FEELS_LIKE("feels_like", R.string.tag_feels_like, R.drawable.ic_device_thermostat),
    TAG_WIND("wind", R.string.wind, R.drawable.ic_wind),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality, R.drawable.weather_haze_mini_xml),
    TAG_POLLEN("pollen", R.string.pollen, R.drawable.ic_allergy),
    TAG_UV_INDEX("uv_index", R.string.uv_index, R.drawable.ic_uv),
    TAG_PRECIPITATION("precipitation", R.string.precipitation, R.drawable.ic_precipitation),
    TAG_HUMIDITY("humidity", R.string.humidity, R.drawable.ic_humidity_percentage),
    TAG_PRESSURE("pressure", R.string.pressure, R.drawable.ic_gauge),
    TAG_CLOUD_COVER("cloud_cover", R.string.cloud_cover, R.drawable.ic_cloud),
    TAG_VISIBILITY("visibility", R.string.visibility, R.drawable.ic_eye),
    TAG_SUN_MOON("sun_moon", R.string.ephemeris, R.drawable.weather_clear_night_mini_xml),
    ;

    companion object {

        const val CHART_MIN_COUNT = 2

        fun toDetailScreenList(
            location: Location,
        ): ImmutableList<DetailScreen> {
            return DetailScreen.entries
                .filter { detailScreen ->
                    when (detailScreen) {
                        TAG_CONDITIONS -> true // Always displayed
                        TAG_FEELS_LIKE -> false // never displayed, it’s actually a sub menu of TAG_CONDITIONS
                        TAG_PRECIPITATION -> true // Too many conditions
                        TAG_WIND -> location.weather?.dailyForecast?.any {
                            (it.day?.wind?.speed ?: 0.0) > 0.0 || (it.night?.wind?.speed ?: 0.0) > 0.0
                        } == true ||
                            location.weather?.hourlyForecast?.any {
                                (it.wind?.speed ?: 0.0) > 0.0
                            } == true
                        TAG_AIR_QUALITY -> !location.airQualitySource.isNullOrEmpty()
                        TAG_POLLEN -> !location.pollenSource.isNullOrEmpty()
                        TAG_UV_INDEX -> location.weather?.dailyForecast?.any {
                            (it.uV?.index ?: 0.0) > 0.0
                        } == true ||
                            location.weather?.hourlyForecast?.any {
                                (it.uV?.index ?: 0.0) > 0.0
                            } == true
                        TAG_HUMIDITY -> location.weather?.hourlyForecast?.any {
                            (it.relativeHumidity ?: 0.0) > 0.0 || (it.dewPoint ?: 0.0) != 0.0
                        } == true
                        TAG_PRESSURE -> location.weather?.hourlyForecast?.any {
                            (it.pressure ?: 0.0) > 0.0
                        } == true
                        TAG_CLOUD_COVER -> location.weather?.hourlyForecast?.any {
                            (it.cloudCover ?: 0) > 0
                        } == true
                        TAG_VISIBILITY -> location.weather?.hourlyForecast?.any {
                            (it.visibility ?: 0.0) > 0.0
                        } == true
                        TAG_SUN_MOON -> true // Should always be computed, no need to check
                    }
                }.toImmutableList()
        }

        fun toValue(list: List<DetailScreen>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        fun getSummary(context: Context, list: List<DetailScreen>): String {
            val builder = StringBuilder()
            for (item in list) {
                builder.append(",").append(item.getName(context))
            }
            if (builder.isNotEmpty() && builder[0] == ',') {
                builder.deleteCharAt(0)
            }
            return builder.toString().replace(",", context.getString(R.string.comma_separator))
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}
