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

package org.breezyweather.sources.smhi

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.smhi.json.SmhiTimeSeries
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

class SmhiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "smhi"
    override val name = "SMHI (${context.currentLocale.getCountryName("SE")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl =
        "https://www.smhi.se/omsmhi/hantering-av-personuppgifter/hantering-av-personuppgifter-1.135429"

    private val mApi by lazy {
        client
            .baseUrl(SMHI_BASE_URL)
            .build()
            .create(SmhiApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to "SMHI (Creative commons Erkännande 4.0 SE)"
    )
    override val attributionLinks = mapOf(
        "SMHI" to "https://www.smhi.se/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("SE", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            location.longitude,
            location.latitude
        ).map {
            // If the API doesn’t return data, consider data as garbage and keep cached data
            if (it.timeSeries.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            WeatherWrapper(
                dailyForecast = getDailyForecast(location, it.timeSeries),
                hourlyForecast = getHourlyForecast(it.timeSeries)
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        forecastResult: List<SmhiTimeSeries>,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = forecastResult.groupBy {
            it.validTime.getIsoFormattedDate(location)
        }
        for (i in 0 until hourlyListByDay.entries.size - 1) {
            val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.timeZone)
            if (dayDate != null) {
                dailyList.add(
                    DailyWrapper(
                        date = dayDate
                    )
                )
            }
        }
        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        forecastResult: List<SmhiTimeSeries>,
    ): List<HourlyWrapper> {
        return forecastResult.map { result ->
            HourlyWrapper(
                date = result.validTime,
                weatherCode = getWeatherCode(
                    result.parameters.firstOrNull { it.name == "Wsymb2" }?.values?.getOrNull(0)
                ),
                temperature = TemperatureWrapper(
                    temperature = result.parameters.firstOrNull { it.name == "t" }?.values?.getOrNull(0)?.celsius
                ),
                precipitation = Precipitation(
                    total = result.parameters.firstOrNull { it.name == "pmean" }?.values?.getOrNull(0)?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    thunderstorm = result.parameters.firstOrNull { it.name == "tstm" }?.values?.getOrNull(0)?.percent
                ),
                wind = Wind(
                    degree = result.parameters.firstOrNull { it.name == "wd" }?.values?.getOrNull(0),
                    speed = result.parameters.firstOrNull { it.name == "ws" }?.values?.getOrNull(0)?.metersPerSecond,
                    gusts = result.parameters.firstOrNull { it.name == "gust" }?.values?.getOrNull(0)?.metersPerSecond
                ),
                relativeHumidity = result.parameters.firstOrNull { it.name == "r" }?.values?.getOrNull(0)?.percent,
                pressure = result.parameters.firstOrNull { it.name == "msl" }?.values?.getOrNull(0)?.hectopascals,
                visibility = result.parameters.firstOrNull { it.name == "vis" }?.values?.getOrNull(0)?.kilometers
            )
        }
    }

    private fun getWeatherCode(icon: Double?): WeatherCode? {
        if (icon == null) return null
        return when (icon.roundToInt()) {
            1, 2 -> WeatherCode.CLEAR
            3, 4 -> WeatherCode.PARTLY_CLOUDY
            5, 6 -> WeatherCode.CLOUDY
            7 -> WeatherCode.FOG
            8, 9, 10, 18, 19, 20 -> WeatherCode.RAIN
            12, 13, 14, 22, 23, 24 -> WeatherCode.SLEET
            15, 16, 17, 25, 26, 27 -> WeatherCode.SNOW
            11 -> WeatherCode.THUNDERSTORM
            21 -> WeatherCode.THUNDER
            else -> null
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val SMHI_BASE_URL = "https://opendata-download-metfcst.smhi.se/api/"
    }
}
