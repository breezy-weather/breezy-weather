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

package org.breezyweather.common.basic.models.weather

import android.annotation.SuppressLint
import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Hourly.
 */
data class Hourly(
    val date: Date,
    val isDaylight: Boolean = true,
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val wind: Wind? = null,
    val airQuality: AirQuality? = null,
    val uV: UV? = null,
    val relativeHumidity: Float? = null,
    val dewPoint: Float? = null,
    /**
     * Pressure at sea level
     */
    val pressure: Float? = null,
    val cloudCover: Int? = null,
    val visibility: Float? = null
) : Serializable {

    fun getHourIn24Format(timeZone: TimeZone): Int {
        val calendar = date.toCalendarWithTimeZone(timeZone)
        return calendar[Calendar.HOUR_OF_DAY]
    }

    fun getHour(context: Context, timeZone: TimeZone): String {
        return getHour(context, timeZone, context.is12Hour)
    }

    @SuppressLint("DefaultLocale")
    private fun getHour(context: Context, timeZone: TimeZone, twelveHour: Boolean): String {
        return date.getFormattedDate(timeZone, if (twelveHour) "h aa" else "H") +
                if (!twelveHour) context.getString(R.string.of_clock) else ""
    }

    fun toHourlyWrapper() = HourlyWrapper(
        date = this.date,
        isDaylight = this.isDaylight,
        weatherText = this.weatherText,
        weatherCode = this.weatherCode,
        temperature = this.temperature,
        precipitation = this.precipitation,
        precipitationProbability = this.precipitationProbability,
        wind = this.wind,
        airQuality = this.airQuality,
        uV = this.uV,
        relativeHumidity = this.relativeHumidity,
        dewPoint = this.dewPoint,
        pressure = this.pressure,
        cloudCover = this.cloudCover,
        visibility = this.visibility
    )
}