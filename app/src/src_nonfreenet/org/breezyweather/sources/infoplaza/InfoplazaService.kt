/*
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

package org.breezyweather.sources.infoplaza

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyDewPoint
import breezyweather.domain.weather.model.DailyPressure
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.infoplaza.json.InfoplazaClimate
import org.breezyweather.sources.infoplaza.json.InfoplazaClimateResult
import org.breezyweather.sources.infoplaza.json.InfoplazaCurrently
import org.breezyweather.sources.infoplaza.json.InfoplazaDaily
import org.breezyweather.sources.infoplaza.json.InfoplazaForecastResult
import org.breezyweather.sources.infoplaza.json.InfoplazaHourly
import org.breezyweather.sources.infoplaza.json.InfoplazaMinutely
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class InfoplazaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : InfoplazaServiceStub() {
    private val mApi by lazy {
        client
            .baseUrl(INFOPLAZA_BASE_URL)
            .build()
            .create(InfoplazaApi::class.java)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures ||
            SourceFeature.CURRENT in requestedFeatures ||
            SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getForecast(location.latitude, location.longitude, apiKey)
        } else {
            Observable.just(InfoplazaForecastResult(null, null, null, null))
        }

        val climate = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getClimate(location.latitude, location.longitude, apiKey)
        } else {
            Observable.just(InfoplazaClimateResult(null, null))
        }

        return Observable.zip(forecast, climate) {
            forecastResult, climateResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(forecastResult.daily)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(forecastResult.hourly)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(forecastResult.currently)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyForecast(forecastResult.minutely)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures && climateResult.period == "month") {
                    getNormals(climateResult.climate)
                } else {
                    null
                }
            )
        }
    }

    private fun getCurrent(
        result: InfoplazaCurrently?,
    ): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherCode = getWeatherCode(result.icon),
            temperature = TemperatureWrapper(
                temperature = result.temperature?.celsius,
                feelsLike = result.apparentTemperature?.celsius
            ),
            wind = Wind(
                degree = result.windBearing,
                speed = result.windSpeed?.metersPerSecond,
                gusts = result.windGust?.metersPerSecond
            ),
            relativeHumidity = result.humidity?.fraction,
            dewPoint = result.dewPoint?.celsius,
            pressure = result.pressure?.hectopascals,
            cloudCover = result.cloudCover?.fraction,
            visibility = result.visibility?.meters
        )
    }

    private fun getDailyForecast(
        dailyResult: List<InfoplazaDaily>?,
    ): List<DailyWrapper>? {
        if (dailyResult == null || dailyResult.isEmpty()) return null
        return listOf(
            // Prepend empty day for today because the source
            // only provides data for upcoming days
            DailyWrapper(
                date = (dailyResult.first().time.seconds - 1.days).inWholeMilliseconds.toDate()
            )
        ) + dailyResult.map { result ->
            DailyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                day = HalfDayWrapper(
                    weatherCode = getWeatherCode(result.icon),
                    temperature = TemperatureWrapper(
                        temperature = result.temperatureHigh?.celsius,
                        feelsLike = result.apparentTemperature?.celsius
                    )
                ),
                night = HalfDayWrapper(
                    weatherCode = getWeatherCode(result.icon),
                    temperature = TemperatureWrapper(
                        temperature = result.temperatureLow?.celsius
                    )
                ),
                uV = UV(index = result.uvIndex),
                relativeHumidity = DailyRelativeHumidity(average = result.humidity?.fraction),
                dewPoint = DailyDewPoint(average = result.dewPoint?.celsius),
                pressure = DailyPressure(average = result.pressure?.hectopascals),
                cloudCover = DailyCloudCover(average = result.cloudCover?.fraction)
            )
        }
    }

    private fun getHourlyForecast(
        hourlyResult: List<InfoplazaHourly>?,
    ): List<HourlyWrapper>? {
        return hourlyResult?.map { result ->
            HourlyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                weatherCode = getWeatherCode(result.icon),
                temperature = TemperatureWrapper(
                    temperature = result.temperature?.celsius,
                    feelsLike = result.apparentTemperature?.celsius
                ),
                precipitation = when (result.precipType) {
                    "rain" -> Precipitation(
                        rain = result.precipIntensity?.millimeters
                    )
                    "snow" -> Precipitation(
                        snow = result.precipIntensity?.millimeters
                    )
                    "ice" -> Precipitation(
                        ice = result.precipIntensity?.millimeters
                    )
                    else -> Precipitation(
                        total = result.precipIntensity?.millimeters
                    )
                },
                precipitationProbability = PrecipitationProbability(
                    total = result.precipProbability?.fraction
                ),
                wind = Wind(
                    degree = result.windBearing,
                    speed = result.windSpeed?.metersPerSecond,
                    gusts = result.windGust?.metersPerSecond
                ),
                relativeHumidity = result.humidity?.fraction,
                dewPoint = result.dewPoint?.celsius,
                pressure = result.pressure?.hectopascals,
                cloudCover = result.cloudCover?.fraction,
                visibility = result.visibility?.meters
            )
        }
    }

    private fun getMinutelyForecast(minutelyResult: List<InfoplazaMinutely>?): List<Minutely>? {
        return minutelyResult?.map { result ->
            Minutely(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                minuteInterval = 5,
                precipitationIntensity = result.precipIntensity?.millimeters
            )
        }
    }

    private fun getNormals(climateResult: List<InfoplazaClimate>?): Map<Month, Normals>? {
        return climateResult?.filter { it.month != null }?.associate { result ->
            Month.of(result.month!!) to Normals(
                daytimeTemperature = result.temperatureHigh?.celsius,
                nighttimeTemperature = result.temperatureLow?.celsius
            )
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        // https://developer.infoplaza.com/docs/weather/forecast/assets
        return when {
            icon?.startsWith("A") == true -> WeatherCode.CLEAR
            icon?.startsWith("B") == true -> WeatherCode.PARTLY_CLOUDY
            icon?.startsWith("C") == true -> WeatherCode.CLOUDY
            icon?.startsWith("D") == true -> WeatherCode.FOG
            icon?.startsWith("E") == true -> WeatherCode.FOG
            icon?.startsWith("F") == true -> WeatherCode.RAIN
            icon?.startsWith("G") == true -> WeatherCode.THUNDERSTORM
            icon?.startsWith("H") == true -> WeatherCode.HAIL
            icon?.startsWith("I") == true -> WeatherCode.HAIL
            icon?.startsWith("J") == true -> WeatherCode.THUNDERSTORM
            icon?.startsWith("K") == true -> WeatherCode.THUNDERSTORM
            icon?.startsWith("L") == true -> WeatherCode.RAIN
            icon?.startsWith("M") == true -> WeatherCode.RAIN
            icon?.startsWith("O") == true -> WeatherCode.SNOW
            icon?.startsWith("P") == true -> WeatherCode.SNOW
            icon?.startsWith("Q") == true -> WeatherCode.SNOW
            icon?.startsWith("R") == true -> WeatherCode.SNOW
            icon?.startsWith("S") == true -> WeatherCode.PARTLY_CLOUDY
            icon?.startsWith("T") == true -> WeatherCode.WIND
            icon?.startsWith("U") == true -> WeatherCode.PARTLY_CLOUDY
            icon?.startsWith("V") == true -> WeatherCode.PARTLY_CLOUDY
            icon?.startsWith("W") == true -> WeatherCode.THUNDERSTORM
            icon?.startsWith("X") == true -> WeatherCode.THUNDERSTORM
            else -> null
        }
    }

    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.INFOPLAZA_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_infoplaza_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    companion object {
        private const val INFOPLAZA_BASE_URL = "https://api.infoplaza.com/weather/v1/"
    }
}
