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

package org.breezyweather.sources.metno

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGH
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoAlertResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoForecastTimeseries
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

class MetNoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "metno"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "Meteorologisk institutt"
                else -> "MET Norway"
            }
        }
    }
    override val continent = SourceContinent.WORLDWIDE // The only exception here. It's a source commonly used worldwide
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "https://www.met.no/om-oss/personvern"
                else -> "https://www.met.no/en/About-us/privacy"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(METNO_BASE_URL)
            .build()
            .create(MetNoApi::class.java)
    }

    private val weatherAttribution = "MET Norway (NLOD / CC BY 4.0)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "MET Norway" to "https://www.met.no/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            // Nowcast only for Norway, Sweden, Finland and Denmark
            // Covered area is slightly larger as per https://api.met.no/doc/nowcast/datamodel
            // but safer to limit to guaranteed countries
            SourceFeature.CURRENT, SourceFeature.MINUTELY -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SE", "FI", "DK").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }

            // Air quality only for Norway
            SourceFeature.AIR_QUALITY -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("NO", ignoreCase = true)

            // Alerts only for Norway and Svalbard & Jan Mayen
            SourceFeature.ALERT -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SJ").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }

            SourceFeature.FORECAST -> true

            else -> false
        }
    }

    /**
     * Highest priority for Norway
     * High priority for Svalbard & Jan Mayen, Sweden, Finland, Denmark
     */
    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            location.countryCode.equals("NO", ignoreCase = true) -> PRIORITY_HIGHEST
            arrayOf("SJ", "SE", "FI", "DK").any { it.equals(location.countryCode, ignoreCase = true) } &&
                isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGH
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MetNoForecastResult())
            }
        } else {
            Observable.just(MetNoForecastResult())
        }

        val nowcast = if (SourceFeature.CURRENT in requestedFeatures ||
            SourceFeature.MINUTELY in requestedFeatures
        ) {
            mApi.getNowcast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                if (SourceFeature.MINUTELY in requestedFeatures) {
                    failedFeatures[SourceFeature.MINUTELY] = it
                }
                if (SourceFeature.CURRENT in requestedFeatures) {
                    failedFeatures[SourceFeature.CURRENT] = it
                }
                Observable.just(MetNoNowcastResult())
            }
        } else {
            Observable.just(MetNoNowcastResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(MetNoAirQualityResult())
            }
        } else {
            Observable.just(MetNoAirQualityResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                USER_AGENT,
                if (context.currentLocale.toString().lowercase().startsWith("no")) "no" else "en",
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(MetNoAlertResult())
            }
        } else {
            Observable.just(MetNoAlertResult())
        }

        return Observable.zip(forecast, nowcast, airQuality, alerts) {
                forecastResult: MetNoForecastResult,
                nowcastResult: MetNoNowcastResult,
                airQualityResult: MetNoAirQualityResult,
                metNoAlerts: MetNoAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        location,
                        forecastResult.properties?.timeseries
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        context,
                        forecastResult.properties?.timeseries
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(nowcastResult, context)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    val airQualityHourly: MutableMap<Date, AirQuality> = mutableMapOf()
                    airQualityResult.data?.time?.forEach {
                        airQualityHourly[it.from] = AirQuality(
                            pM25 = it.variables?.pm25Concentration?.value,
                            pM10 = it.variables?.pm10Concentration?.value,
                            sO2 = it.variables?.so2Concentration?.value,
                            nO2 = it.variables?.no2Concentration?.value,
                            o3 = it.variables?.o3Concentration?.value
                        )
                    }
                    AirQualityWrapper(hourlyForecast = airQualityHourly)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(nowcastResult.properties?.timeseries)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(metNoAlerts)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(nowcastResult: MetNoNowcastResult, context: Context): CurrentWrapper? {
        val currentTimeseries = nowcastResult.properties?.timeseries?.getOrNull(0)?.data
        return if (currentTimeseries != null) {
            CurrentWrapper(
                weatherText = getWeatherText(context, currentTimeseries.symbolCode),
                weatherCode = getWeatherCode(currentTimeseries.symbolCode),
                temperature = TemperatureWrapper(
                    temperature = currentTimeseries.instant?.details?.airTemperature
                ),
                wind = if (currentTimeseries.instant?.details != null) {
                    Wind(
                        degree = currentTimeseries.instant.details.windFromDirection,
                        speed = currentTimeseries.instant.details.windSpeed
                    )
                } else {
                    null
                },
                relativeHumidity = currentTimeseries.instant?.details?.relativeHumidity,
                dewPoint = currentTimeseries.instant?.details?.dewPointTemperature,
                pressure = currentTimeseries.instant?.details?.airPressureAtSeaLevel?.hectopascals,
                cloudCover = currentTimeseries.instant?.details?.cloudAreaFraction?.roundToInt()
            )
        } else {
            null
        }
    }

    private fun getHourlyList(
        context: Context,
        forecastTimeseries: List<MetNoForecastTimeseries>?,
    ): List<HourlyWrapper> {
        if (forecastTimeseries.isNullOrEmpty()) return emptyList()
        return forecastTimeseries.map { hourlyForecast ->
            HourlyWrapper(
                date = hourlyForecast.time,
                weatherText = getWeatherText(context, hourlyForecast.data?.symbolCode),
                weatherCode = getWeatherCode(hourlyForecast.data?.symbolCode),
                temperature = TemperatureWrapper(
                    temperature = hourlyForecast.data?.instant?.details?.airTemperature
                ),
                precipitation = Precipitation(
                    total = hourlyForecast.data?.next1Hours?.details?.precipitationAmount?.millimeters
                        ?: hourlyForecast.data?.next6Hours?.details?.precipitationAmount?.millimeters
                        ?: hourlyForecast.data?.next12Hours?.details?.precipitationAmount?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyForecast.data?.next1Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfPrecipitation,
                    thunderstorm = hourlyForecast.data?.next1Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfThunder
                ),
                wind = if (hourlyForecast.data?.instant?.details != null) {
                    Wind(
                        degree = hourlyForecast.data.instant.details.windFromDirection,
                        speed = hourlyForecast.data.instant.details.windSpeed
                    )
                } else {
                    null
                },
                uV = UV(index = hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky),
                relativeHumidity = hourlyForecast.data?.instant?.details?.relativeHumidity,
                dewPoint = hourlyForecast.data?.instant?.details?.dewPointTemperature,
                pressure = hourlyForecast.data?.instant?.details?.airPressureAtSeaLevel?.hectopascals,
                cloudCover = hourlyForecast.data?.instant?.details?.cloudAreaFraction?.roundToInt()
            )
        }
    }

    private fun getDailyList(
        location: Location,
        forecastTimeseries: List<MetNoForecastTimeseries>?,
    ): List<DailyWrapper> {
        if (forecastTimeseries.isNullOrEmpty()) return emptyList()
        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = forecastTimeseries.groupBy {
            it.time.getIsoFormattedDate(location)
        }
        for (i in 0 until hourlyListByDay.entries.size - 1) {
            hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.timeZone)?.let { dayDate ->
                dailyList.add(
                    DailyWrapper(date = dayDate)
                )
            }
        }
        return dailyList
    }

    private fun getMinutelyList(nowcastTimeseries: List<MetNoForecastTimeseries>?): List<Minutely> {
        val minutelyList: MutableList<Minutely> = arrayListOf()
        if (nowcastTimeseries.isNullOrEmpty() || nowcastTimeseries.size < 2) return minutelyList

        nowcastTimeseries.forEachIndexed { i, nowcastForecast ->
            minutelyList.add(
                Minutely(
                    date = nowcastForecast.time,
                    minuteInterval = if (i < nowcastTimeseries.size - 1) {
                        (nowcastTimeseries[i + 1].time.time - nowcastForecast.time.time)
                            .div(1.minutes.inWholeMilliseconds)
                            .toDouble().roundToInt()
                    } else {
                        (nowcastForecast.time.time - nowcastTimeseries[i - 1].time.time)
                            .div(1.minutes.inWholeMilliseconds)
                            .toDouble().roundToInt()
                    },
                    precipitationIntensity = nowcastForecast.data?.instant?.details?.precipitationRate?.millimeters
                )
            )
        }

        return minutelyList
    }

    private fun getAlerts(metNoAlerts: MetNoAlertResult): List<Alert>? {
        return metNoAlerts.features?.mapNotNull { alert ->
            alert.properties?.let {
                val severity = when (it.severity?.lowercase()) {
                    "extreme" -> AlertSeverity.EXTREME
                    "severe" -> AlertSeverity.SEVERE
                    "moderate" -> AlertSeverity.MODERATE
                    "minor" -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                }
                Alert(
                    alertId = it.id,
                    startDate = alert.whenAlert?.interval?.getOrNull(0),
                    endDate = alert.whenAlert?.interval?.getOrNull(1) ?: it.eventEndingTime,
                    headline = it.eventAwarenessName,
                    description = it.description,
                    instruction = it.instruction,
                    source = "MET Norway",
                    severity = severity,
                    color = Alert.colorFromSeverity(severity)
                )
            }
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return if (icon == null) {
            null
        } else {
            when (icon.replace("_night", "").replace("_day", "")) {
                "clearsky", "fair" -> WeatherCode.CLEAR
                "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
                "cloudy" -> WeatherCode.CLOUDY
                "fog" -> WeatherCode.FOG
                "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" ->
                    WeatherCode.RAIN
                "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder",
                "heavysleetshowersandthunder", "heavysnowandthunder", "heavysnowshowersandthunder",
                "lightrainandthunder", "lightrainshowersandthunder", "lightsleetandthunder",
                "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
                "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder",
                "snowandthunder", "snowshowersandthunder",
                -> WeatherCode.THUNDERSTORM
                "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" ->
                    WeatherCode.SNOW
                "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet", "sleetshowers" ->
                    WeatherCode.SLEET
                else -> null
            }
        }
    }

    private fun getWeatherText(context: Context, icon: String?): String? {
        if (icon == null) return null
        val weatherWithoutThunder = when (
            icon.replace("_night", "")
                .replace("_day", "")
                .replace("andthunder", "")
        ) {
            "clearsky" -> context.getString(R.string.metno_weather_text_clearsky)
            "cloudy" -> context.getString(R.string.metno_weather_text_cloudy)
            "fair" -> context.getString(R.string.metno_weather_text_fair)
            "fog" -> context.getString(R.string.metno_weather_text_fog)
            "heavyrain" -> context.getString(R.string.metno_weather_text_heavyrain)
            "heavyrainshowers" -> context.getString(R.string.metno_weather_text_heavyrainshowers)
            "heavysleet" -> context.getString(R.string.metno_weather_text_heavysleet)
            "heavysleetshowers" -> context.getString(R.string.metno_weather_text_heavysleetshowers)
            "heavysnow" -> context.getString(R.string.metno_weather_text_heavysnow)
            "heavysnowshowers" -> context.getString(R.string.metno_weather_text_heavysnowshowers)
            "lightrain" -> context.getString(R.string.metno_weather_text_lightrain)
            "lightrainshowers" -> context.getString(R.string.metno_weather_text_lightrainshowers)
            "lightsleet" -> context.getString(R.string.metno_weather_text_lightsleet)
            "lightsleetshowers" -> context.getString(R.string.metno_weather_text_lightsleetshowers)
            "lightsnow" -> context.getString(R.string.metno_weather_text_lightsnow)
            "lightsnowshowers" -> context.getString(R.string.metno_weather_text_lightsnowshowers)
            "partlycloudy" -> context.getString(R.string.metno_weather_text_partlycloudy)
            "rain" -> context.getString(R.string.metno_weather_text_rain)
            "rainshowers" -> context.getString(R.string.metno_weather_text_rainshowers)
            "sleet" -> context.getString(R.string.metno_weather_text_sleet)
            "sleetshowers" -> context.getString(R.string.metno_weather_text_sleetshowers)
            "snow" -> context.getString(R.string.metno_weather_text_snow)
            "snowshowers" -> context.getString(R.string.metno_weather_text_snowshowers)
            else -> null
        }

        return if (icon.contains("andthunder")) {
            context.getString(
                R.string.metno_weather_text_andthunder,
                weatherWithoutThunder ?: context.getString(R.string.null_data_text)
            )
        } else {
            weatherWithoutThunder
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val METNO_BASE_URL = "https://api.met.no/weatherapi/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
