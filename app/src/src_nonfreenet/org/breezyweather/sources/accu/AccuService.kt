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

package org.breezyweather.sources.accu

import android.content.Context
import android.graphics.Color
import androidx.annotation.DrawableRes
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.accu.json.AccuAirQualityData
import org.breezyweather.sources.accu.json.AccuAirQualityResult
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuClimoSummaryResult
import org.breezyweather.sources.accu.json.AccuCurrentResult
import org.breezyweather.sources.accu.json.AccuForecastAirAndPollen
import org.breezyweather.sources.accu.json.AccuForecastDailyForecast
import org.breezyweather.sources.accu.json.AccuForecastDailyResult
import org.breezyweather.sources.accu.json.AccuForecastHourlyResult
import org.breezyweather.sources.accu.json.AccuLocationResult
import org.breezyweather.sources.accu.json.AccuMinutelyResult
import org.breezyweather.sources.accu.json.AccuValue
import org.breezyweather.sources.accu.preferences.AccuDaysPreference
import org.breezyweather.sources.accu.preferences.AccuHoursPreference
import org.breezyweather.sources.accu.preferences.AccuPortalPreference
import org.breezyweather.sources.openmeteo.OpenMeteoService.Companion.COPERNICUS_POLLEN_BBOX
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AccuService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(),
    WeatherSource,
    LocationSearchSource,
    ReverseGeocodingSource,
    ConfigurableSource,
    LocationParametersSource {

    override val id = "accu"
    override val name = "AccuWeather"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://www.accuweather.com/en/privacy"

    private val mDeveloperApi by lazy {
        client
            .baseUrl(ACCU_DEVELOPER_BASE_URL)
            .build()
            .create(AccuDeveloperApi::class.java)
    }
    private val mEnterpriseApi by lazy {
        client
            .baseUrl(ACCU_ENTERPRISE_BASE_URL)
            .build()
            .create(AccuEnterpriseApi::class.java)
    }

    private val weatherAttribution = "AccuWeather"
    override val locationSearchAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.POLLEN to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.accuweather.com/"
    )

    @DrawableRes
    override fun getAttributionIcon(): Int {
        return R.drawable.accu_icon
    }

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.POLLEN -> COPERNICUS_POLLEN_BBOX.contains(LatLng(location.latitude, location.longitude)) || (
                location.countryCode.equals("US", ignoreCase = true) &&
                    CONTIGUOUS_US_STATES_BBOX.contains(LatLng(location.latitude, location.longitude))
                ) || (location.countryCode.equals("CA", ignoreCase = true))
            else ->
                portal == AccuPortalPreference.ENTERPRISE ||
                    feature == SourceFeature.FORECAST ||
                    feature == SourceFeature.CURRENT ||
                    feature == SourceFeature.ALERT ||
                    feature == SourceFeature.REVERSE_GEOCODING
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        val locationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }
        if (
            (
                SourceFeature.FORECAST in requestedFeatures ||
                    SourceFeature.CURRENT in requestedFeatures ||
                    SourceFeature.AIR_QUALITY in requestedFeatures ||
                    SourceFeature.POLLEN in requestedFeatures ||
                    SourceFeature.NORMALS in requestedFeatures ||
                    (SourceFeature.ALERT in requestedFeatures && mApi is AccuEnterpriseApi)
                ) &&
            locationKey.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val metric = SettingsManager.getInstance(context).getPrecipitationUnit(context) != PrecipitationUnit.INCH
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                locationKey!!,
                apiKey,
                languageCode,
                details = true
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val daily = if (SourceFeature.FORECAST in requestedFeatures || SourceFeature.POLLEN in requestedFeatures) {
            mApi.getDaily(
                days.id,
                locationKey!!,
                apiKey,
                languageCode,
                details = true,
                metric = metric
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(AccuForecastDailyResult())
            }
        } else {
            Observable.just(AccuForecastDailyResult())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(
                hours.id,
                locationKey!!,
                apiKey,
                languageCode,
                details = true,
                metric = metric
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val minute = if (SourceFeature.MINUTELY in requestedFeatures && mApi is AccuEnterpriseApi) {
            mApi.getMinutely(
                minutes = 1,
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(AccuMinutelyResult())
            }
        } else {
            Observable.just(AccuMinutelyResult())
        }
        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            if (mApi is AccuEnterpriseApi) {
                mApi.getAlertsByPosition(
                    apiKey,
                    location.latitude.toString() + "," + location.longitude,
                    languageCode,
                    details = true
                ).onErrorResumeNext {
                    mApi.getAlertsByCityKey(
                        locationKey!!,
                        apiKey,
                        languageCode,
                        details = true
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.ALERT] = it
                        Observable.just(emptyList())
                    }
                }
            } else {
                mApi.getAlertsByCityKey(
                    locationKey!!,
                    apiKey,
                    languageCode,
                    details = true
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(emptyList())
                }
            }
        } else {
            Observable.just(emptyList())
        }
        val airQuality = if (
            SourceFeature.AIR_QUALITY in requestedFeatures &&
            mApi is AccuEnterpriseApi
        ) {
            mApi.getAirQuality(
                locationKey!!,
                apiKey,
                pollutants = true,
                languageCode
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(AccuAirQualityResult())
            }
        } else {
            Observable.just(AccuAirQualityResult())
        }
        val cal = Date().toCalendarWithTimeZone(location.timeZone)
        val climoSummary = if (
            SourceFeature.NORMALS in requestedFeatures &&
            mApi is AccuEnterpriseApi
        ) {
            mApi.getClimoSummary(
                cal[Calendar.YEAR],
                cal[Calendar.MONTH] + 1,
                locationKey!!,
                apiKey,
                languageCode,
                details = false
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(AccuClimoSummaryResult())
            }
        } else {
            Observable.just(AccuClimoSummaryResult())
        }
        return Observable.zip(
            current,
            daily,
            hourly,
            minute,
            alert,
            airQuality,
            climoSummary
        ) {
                accuRealtimeResults: List<AccuCurrentResult>,
                accuDailyResult: AccuForecastDailyResult,
                accuHourlyResults: List<AccuForecastHourlyResult>,
                accuMinutelyResult: AccuMinutelyResult,
                accuAlertResults: List<AccuAlertResult>,
                accuAirQualityResult: AccuAirQualityResult,
                accuClimoResult: AccuClimoSummaryResult,
            ->
            WeatherWrapper(
                /*base = Base(
                    publishDate = currentResult.EpochTime.seconds.inWholeMilliseconds.toDate(),
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(accuDailyResult.DailyForecasts, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(accuHourlyResults)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(accuRealtimeResults.getOrNull(0), accuDailyResult, accuMinutelyResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    getAirQualityWrapper(accuAirQualityResult.data)
                } else {
                    null
                },
                pollen = if (SourceFeature.POLLEN in requestedFeatures) {
                    getPollenWrapper(accuDailyResult.DailyForecasts, location)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(accuMinutelyResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(accuAlertResults)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures &&
                    accuClimoResult.Normals?.Temperatures != null
                ) {
                    mapOf(
                        Month.fromCalendarMonth(cal[Calendar.MONTH]) to Normals(
                            daytimeTemperature = accuClimoResult.Normals.Temperatures.Maximum.Metric?.Value,
                            nighttimeTemperature = accuClimoResult.Normals.Temperatures.Minimum.Metric?.Value
                        )
                    )
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        currentResult: AccuCurrentResult?,
        dailyResult: AccuForecastDailyResult? = null,
        minuteResult: AccuMinutelyResult? = null,
    ): CurrentWrapper? {
        if (currentResult == null) return null

        return CurrentWrapper(
            weatherText = currentResult.WeatherText,
            weatherCode = getWeatherCode(currentResult.WeatherIcon),
            temperature = TemperatureWrapper(
                temperature = currentResult.Temperature?.Metric?.Value,
                feelsLike = currentResult.RealFeelTemperature?.Metric?.Value
            ),
            wind = Wind(
                degree = currentResult.Wind?.Direction?.Degrees?.toDouble(),
                speed = currentResult.Wind?.Speed?.Metric?.Value?.div(3.6),
                gusts = currentResult.WindGust?.Speed?.Metric?.Value?.div(3.6)
            ),
            uV = UV(index = currentResult.UVIndex?.toDouble()),
            relativeHumidity = currentResult.RelativeHumidity?.toDouble(),
            dewPoint = currentResult.DewPoint?.Metric?.Value,
            pressure = currentResult.Pressure?.Metric?.Value,
            cloudCover = currentResult.CloudCover,
            visibility = currentResult.Visibility?.Metric?.Value?.times(1000),
            ceiling = currentResult.Ceiling?.Metric?.Value,
            dailyForecast = dailyResult?.Headline?.Text,
            hourlyForecast = minuteResult?.Summary?.LongPhrase
        )
    }

    private fun getDailyList(
        dailyForecasts: List<AccuForecastDailyForecast>?,
        location: Location,
    ): List<DailyWrapper>? {
        return dailyForecasts?.map { forecasts ->
            DailyWrapper(
                date = forecasts.EpochDate.seconds.inWholeMilliseconds.toDate().toTimezoneNoHour(location.timeZone),
                day = HalfDayWrapper(
                    weatherText = forecasts.Day?.ShortPhrase,
                    weatherSummary = forecasts.Day?.LongPhrase,
                    weatherCode = getWeatherCode(forecasts.Day?.Icon),
                    temperature = TemperatureWrapper(
                        temperature = getTemperatureInCelsius(forecasts.Temperature?.Maximum),
                        feelsLike = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Maximum)
                    ),
                    precipitation = Precipitation(
                        total = getQuantityInMillimeters(forecasts.Day?.TotalLiquid),
                        rain = getQuantityInMillimeters(forecasts.Day?.Rain),
                        snow = getQuantityInMillimeters(forecasts.Day?.Snow),
                        ice = getQuantityInMillimeters(forecasts.Day?.Ice)
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = forecasts.Day?.PrecipitationProbability?.toDouble(),
                        thunderstorm = forecasts.Day?.ThunderstormProbability?.toDouble(),
                        rain = forecasts.Day?.RainProbability?.toDouble(),
                        snow = forecasts.Day?.SnowProbability?.toDouble(),
                        ice = forecasts.Day?.IceProbability?.toDouble()
                    ),
                    precipitationDuration = PrecipitationDuration(
                        total = forecasts.Day?.HoursOfPrecipitation,
                        rain = forecasts.Day?.HoursOfRain,
                        snow = forecasts.Day?.HoursOfSnow,
                        ice = forecasts.Day?.HoursOfIce
                    ),
                    wind = Wind(
                        degree = forecasts.Day?.Wind?.Direction?.Degrees?.toDouble(),
                        speed = getSpeedInMetersPerSecond(forecasts.Day?.Wind?.Speed),
                        gusts = getSpeedInMetersPerSecond(forecasts.Day?.WindGust?.Speed)
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = forecasts.Night?.ShortPhrase,
                    weatherSummary = forecasts.Night?.LongPhrase,
                    weatherCode = getWeatherCode(forecasts.Night?.Icon),
                    temperature = TemperatureWrapper(
                        temperature = getTemperatureInCelsius(forecasts.Temperature?.Minimum),
                        feelsLike = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Minimum)
                    ),
                    precipitation = Precipitation(
                        total = getQuantityInMillimeters(forecasts.Night?.TotalLiquid),
                        rain = getQuantityInMillimeters(forecasts.Night?.Rain),
                        snow = getQuantityInMillimeters(forecasts.Night?.Snow),
                        ice = getQuantityInMillimeters(forecasts.Night?.Ice)
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = forecasts.Night?.PrecipitationProbability?.toDouble(),
                        thunderstorm = forecasts.Night?.ThunderstormProbability?.toDouble(),
                        rain = forecasts.Night?.RainProbability?.toDouble(),
                        snow = forecasts.Night?.SnowProbability?.toDouble(),
                        ice = forecasts.Night?.IceProbability?.toDouble()
                    ),
                    precipitationDuration = PrecipitationDuration(
                        total = forecasts.Night?.HoursOfPrecipitation,
                        rain = forecasts.Night?.HoursOfRain,
                        snow = forecasts.Night?.HoursOfSnow,
                        ice = forecasts.Night?.HoursOfIce
                    ),
                    wind = Wind(
                        degree = forecasts.Night?.Wind?.Direction?.Degrees?.toDouble(),
                        speed = getSpeedInMetersPerSecond(forecasts.Night?.Wind?.Speed),
                        gusts = getSpeedInMetersPerSecond(forecasts.Night?.WindGust?.Speed)
                    )
                ),
                degreeDay = DegreeDay(
                    heating = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Heating),
                    cooling = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Cooling)
                ),
                uV = getDailyUV(forecasts.AirAndPollen),
                sunshineDuration = forecasts.HoursOfSun
            )
        }
    }

    /**
     * Accu returns 0 / m³ for all days if they don’t measure it, instead of null values
     * This function will tell us if they measured at least one pollen or mold during the 15-day period
     * to make the difference between a 0 and a null
     */
    private fun supportsPollen(dailyForecasts: List<AccuForecastDailyForecast>): Boolean {
        dailyForecasts.forEach { daily ->
            val pollens = listOf(
                daily.AirAndPollen?.firstOrNull { it.Name == "Tree" },
                daily.AirAndPollen?.firstOrNull { it.Name == "Grass" },
                daily.AirAndPollen?.firstOrNull { it.Name == "Ragweed" },
                daily.AirAndPollen?.firstOrNull { it.Name == "Mold" }
            ).filter { it?.Value != null && it.Value > 0 }
            if (pollens.isNotEmpty()) return true
        }
        return false
    }

    private fun getDailyPollen(list: List<AccuForecastAirAndPollen>?): Pollen? {
        if (list == null) return null

        val grass = list.firstOrNull { it.Name == "Grass" }
        val mold = list.firstOrNull { it.Name == "Mold" }
        val ragweed = list.firstOrNull { it.Name == "Ragweed" }
        val tree = list.firstOrNull { it.Name == "Tree" }
        return Pollen(
            grass = grass?.Value,
            mold = mold?.Value,
            ragweed = ragweed?.Value,
            tree = tree?.Value
        )
    }

    private fun getDailyUV(
        list: List<AccuForecastAirAndPollen>?,
    ): UV? {
        if (list == null) return null

        val uv = list.firstOrNull { it.Name == "UVIndex" }
        return UV(index = uv?.Value?.toDouble())
    }

    private fun getHourlyList(
        resultList: List<AccuForecastHourlyResult>,
    ): List<HourlyWrapper> {
        return resultList.map { result ->
            HourlyWrapper(
                date = result.EpochDateTime.seconds.inWholeMilliseconds.toDate(),
                isDaylight = result.IsDaylight,
                weatherText = result.IconPhrase,
                weatherCode = getWeatherCode(result.WeatherIcon),
                temperature = TemperatureWrapper(
                    temperature = getTemperatureInCelsius(result.Temperature),
                    feelsLike = getTemperatureInCelsius(result.RealFeelTemperature)
                ),
                precipitation = Precipitation(
                    total = getQuantityInMillimeters(result.TotalLiquid),
                    rain = getQuantityInMillimeters(result.Rain),
                    snow = getQuantityInMillimeters(result.Snow),
                    ice = getQuantityInMillimeters(result.Ice)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.PrecipitationProbability?.toDouble(),
                    thunderstorm = result.ThunderstormProbability?.toDouble(),
                    rain = result.RainProbability?.toDouble(),
                    snow = result.SnowProbability?.toDouble(),
                    ice = result.IceProbability?.toDouble()
                ),
                wind = Wind(
                    degree = result.Wind?.Direction?.Degrees?.toDouble(),
                    speed = getSpeedInMetersPerSecond(result.Wind?.Speed),
                    gusts = getSpeedInMetersPerSecond(result.WindGust?.Speed)
                ),
                uV = UV(index = result.UVIndex?.toDouble()),
                relativeHumidity = result.RelativeHumidity?.toDouble(),
                dewPoint = getTemperatureInCelsius(result.DewPoint),
                cloudCover = result.CloudCover,
                visibility = getDistanceInMeters(result.Visibility)
            )
        }
    }

    private fun getAirQualityWrapper(airQualityHourlyResult: List<AccuAirQualityData>?): AirQualityWrapper? {
        if (airQualityHourlyResult.isNullOrEmpty()) return null

        val airQualityHourly = mutableMapOf<Date, AirQuality>()
        airQualityHourlyResult
            .forEach {
                var pm25: Double? = null
                var pm10: Double? = null
                var so2: Double? = null
                var no2: Double? = null
                var o3: Double? = null
                var co: Double? = null
                it.pollutants?.forEach { p ->
                    when (p.type) {
                        "O3" -> o3 = p.concentration.value
                        "NO2" -> no2 = p.concentration.value
                        "PM2_5" -> pm25 = p.concentration.value
                        "PM10" -> pm10 = p.concentration.value
                        "SO2" -> so2 = p.concentration.value
                        "CO" -> co = p.concentration.value?.div(1000.0)
                    }
                }
                val airQuality = if (pm25 != null ||
                    pm10 != null ||
                    so2 != null ||
                    no2 != null ||
                    o3 != null ||
                    co != null
                ) {
                    AirQuality(
                        pM25 = pm25,
                        pM10 = pm10,
                        sO2 = so2,
                        nO2 = no2,
                        o3 = o3,
                        cO = co
                    )
                } else {
                    null
                }
                if (airQuality != null) {
                    airQualityHourly[it.epochDate.seconds.inWholeMilliseconds.toDate()] = airQuality
                }
            }

        return AirQualityWrapper(
            hourlyForecast = airQualityHourly
        )
    }

    /**
     * Used from secondary
     */
    private fun getPollenWrapper(
        dailyPollenResult: List<AccuForecastDailyForecast>?,
        location: Location,
    ): PollenWrapper? {
        if (dailyPollenResult.isNullOrEmpty()) return null
        if (!supportsPollen(dailyPollenResult)) return null

        val pollenDaily = mutableMapOf<Date, Pollen>()
        dailyPollenResult
            .forEach {
                val dailyPollen = getDailyPollen(it.AirAndPollen)
                if (dailyPollen != null) {
                    pollenDaily[
                        it.EpochDate.seconds.inWholeMilliseconds.toDate().toTimezoneNoHour(location.timeZone)
                    ] = dailyPollen
                }
            }

        return PollenWrapper(
            dailyForecast = pollenDaily
        )
    }

    private fun getMinutelyList(
        minuteResult: AccuMinutelyResult?,
    ): List<Minutely>? {
        if (minuteResult == null) return null
        if (minuteResult.Intervals.isNullOrEmpty()) return emptyList()
        return minuteResult.Intervals.mapIndexed { i, interval ->
            Minutely(
                date = Date(interval.StartEpochDateTime),
                minuteInterval = if (i < minuteResult.Intervals.size - 1) {
                    (
                        (minuteResult.Intervals[i + 1].StartEpochDateTime - interval.StartEpochDateTime) /
                            1.minutes.inWholeMilliseconds
                        ).toDouble().roundToInt()
                } else {
                    (
                        (interval.StartEpochDateTime - minuteResult.Intervals[i - 1].StartEpochDateTime) /
                            1.minutes.inWholeMilliseconds
                        ).toDouble().roundToInt()
                },
                precipitationIntensity = Minutely.dbzToPrecipitationIntensity(interval.Dbz)
            )
        }
    }

    private fun getAlertList(
        resultList: List<AccuAlertResult>?,
    ): List<Alert>? {
        if (resultList == null) return null
        return resultList.map { result ->
            val severity = when (result.Priority) {
                1 -> AlertSeverity.EXTREME
                2 -> AlertSeverity.SEVERE
                3 -> AlertSeverity.MODERATE
                4, 5 -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                alertId = result.AlertID.toString(),
                startDate = result.Area?.getOrNull(0)?.let { area ->
                    area.EpochStartTime?.seconds?.inWholeMilliseconds?.toDate()
                },
                endDate = result.Area?.getOrNull(0)?.let { area ->
                    area.EpochEndTime?.seconds?.inWholeMilliseconds?.toDate()
                },
                headline = result.Description?.Localized,
                description = result.Area?.getOrNull(0)?.Text,
                source = result.Source,
                severity = severity,
                color = result.Color?.let {
                    Color.rgb(it.Red, it.Green, it.Blue)
                } ?: Alert.colorFromSeverity(severity)
            )
        }
    }

    private fun getWeatherCode(icon: Int?): WeatherCode? {
        return when (icon) {
            null -> null
            1, 2, 30, 33, 34 -> WeatherCode.CLEAR
            3, 4, 6, 35, 36, 38 -> WeatherCode.PARTLY_CLOUDY
            5, 37 -> WeatherCode.HAZE
            7, 8 -> WeatherCode.CLOUDY
            11 -> WeatherCode.FOG
            12, 13, 14, 18, 39, 40 -> WeatherCode.RAIN
            15, 16, 17, 41, 42 -> WeatherCode.THUNDERSTORM
            19, 20, 21, 22, 23, 24, 31, 43, 44 -> WeatherCode.SNOW
            25 -> WeatherCode.HAIL
            26, 29 -> WeatherCode.SLEET
            32 -> WeatherCode.WIND
            else -> null
        }
    }

    private fun getTemperatureInCelsius(value: AccuValue?): Double? {
        return if (value?.UnitType == 18) { // F
            value.Value?.minus(32)?.div(1.8)
        } else {
            value?.Value
        }
    }

    private fun getDegreeDayInCelsius(value: AccuValue?): Double? {
        return if (value?.UnitType == 18) { // F
            value.Value?.div(1.8)
        } else {
            value?.Value
        }
    }

    private fun getSpeedInMetersPerSecond(value: AccuValue?): Double? {
        return if (value?.UnitType == 9) { // mi/h
            value.Value?.div(2.23694)
        } else {
            value?.Value?.div(3.6)
        }
    }

    private fun getDistanceInMeters(value: AccuValue?): Double? {
        return when (value?.UnitType) {
            2 -> value.Value?.times(1609.344) // mi
            0 -> value.Value?.div(3.28084) // ft
            6 -> value.Value?.times(1000) // km
            else -> value?.Value // m
        }
    }

    private fun getQuantityInMillimeters(value: AccuValue?): Double? {
        return when (value?.UnitType) {
            1 -> value.Value?.times(25.4) // in
            4 -> value.Value?.times(10) // cm
            else -> value?.Value // mm
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocation(
            apiKey,
            query,
            languageCode,
            details = false,
            alias = "Always"
        ).map { results ->
            results.map {
                convertLocation(it)
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            "$latitude,$longitude"
        ).map {
            listOf(convertLocation(it))
        }
    }

    private fun convertLocation(
        result: AccuLocationResult,
    ): LocationAddressInfo {
        if (result.Country.ID.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        return LocationAddressInfo(
            latitude = result.GeoPosition.Latitude,
            longitude = result.GeoPosition.Longitude,
            timeZoneId = result.TimeZone.Name,
            country = result.Country.LocalizedName.ifEmpty { result.Country.EnglishName },
            countryCode = result.Country.ID,
            admin2 = result.AdministrativeArea?.LocalizedName?.ifEmpty {
                result.AdministrativeArea.EnglishName
            },
            admin2Code = result.AdministrativeArea?.ID,
            city = result.LocalizedName?.ifEmpty { result.EnglishName } ?: "",
            cityCode = result.Key
        )
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private var portal: AccuPortalPreference
        set(value) {
            config.edit().putString("portal", value.id).apply()
        }
        get() = AccuPortalPreference.getInstance(
            if (apikey.isEmpty() || apikey == BuildConfig.ACCU_WEATHER_KEY) {
                // Force portal to make sure a user didn’t select a portal incompatible with the default key
                "enterprise"
            } else {
                config.getString("portal", null) ?: "enterprise"
            }
        )

    private var days: AccuDaysPreference
        set(value) {
            config.edit().putString("days", value.id).apply()
        }
        get() = AccuDaysPreference.getInstance(
            config.getString("days", null) ?: "15"
        )

    private var hours: AccuHoursPreference
        set(value) {
            config.edit().putString("hours", value.id).apply()
        }
        get() = AccuHoursPreference.getInstance(
            (config.getString("hours", null) ?: "240").let {
                if (portal != AccuPortalPreference.ENTERPRISE && it == "240") {
                    "120" // 120 hours is the max on developer portal
                } else {
                    it
                }
            }
        )

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.ACCU_WEATHER_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            ListPreference(
                titleId = R.string.settings_weather_source_portal,
                selectedKey = portal.id,
                valueArrayId = R.array.accu_preference_portal_values,
                nameArrayId = R.array.accu_preference_portal,
                onValueChanged = {
                    portal = AccuPortalPreference.getInstance(it)
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_accu_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            ),
            ListPreference(
                titleId = R.string.setting_weather_source_accu_days,
                selectedKey = days.id,
                valueArrayId = R.array.accu_preference_day_values,
                nameArrayId = R.array.accu_preference_days,
                onValueChanged = {
                    days = AccuDaysPreference.getInstance(it)
                }
            ),
            ListPreference(
                titleId = R.string.setting_weather_source_accu_hours,
                selectedKey = hours.id,
                valueArrayId = R.array.accu_preference_hour_values,
                nameArrayId = R.array.accu_preference_hours,
                onValueChanged = {
                    hours = AccuHoursPreference.getInstance(it)
                }
            )
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        return if (SourceFeature.FORECAST in features ||
            SourceFeature.CURRENT in features ||
            SourceFeature.AIR_QUALITY in features ||
            SourceFeature.POLLEN in features ||
            SourceFeature.NORMALS in features ||
            SourceFeature.ALERT in features
        ) {
            val currentLocationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }
            currentLocationKey.isNullOrEmpty()
        } else {
            false
        } // If we request alerts or minutely, we don't need locationKey
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).map {
            mapOf(
                "locationKey" to it.Key
            )
        }
    }

    override val testingLocations = listOf(
        Location(
            city = "State College",
            latitude = 40.79339,
            longitude = -77.86,
            timeZone = TimeZone.getTimeZone("America/New_York"),
            countryCode = "US",
            forecastSource = id,
            currentSource = id,
            airQualitySource = id,
            pollenSource = id,
            minutelySource = id,
            alertSource = id,
            normalsSource = id
        )
    )

    companion object {
        private const val ACCU_DEVELOPER_BASE_URL = "https://dataservice.accuweather.com/"
        private const val ACCU_ENTERPRISE_BASE_URL = "https://api.accuweather.com/"

        // Accuweather's pollen forecast is only available in the 48 contiguous U.S. states + D.C., Canada,
        // and European coverage area of Copernicus. We will limit Pollen Source to these areas.

        // 48 contiguous states boundary taken from Natural Earth Data, extended by 1° in each direction.
        // Source: https://www.naturalearthdata.com/
        private val CONTIGUOUS_US_STATES_BBOX = LatLngBounds(
            LatLng(23.542547919, -125.734607238),
            LatLng(50.369494121, -65.977324999)
        )

        // Extracted from: https://developer.accuweather.com/localizations-by-language
        // Leads to failure to refresh otherwise
        private val supportedLanguages = setOf(
            "ar", "ar-dz", "ar-bh", "ar-eg", "ar-iq", "ar-jo", "ar-kw", "ar-lb", "ar-ly", "ar-ma",
            "ar-om", "ar-qa", "ar-sa", "ar-sd", "ar-sy", "ar-tn", "ar-ae", "ar-ye",
            "az", "az-latn", "az-latn-az",
            "bn", "bn-bd", "bn-in",
            "bs", "bs-ba",
            "bg", "bg-bg",
            "ca", "ca-es",
            "zh", "zh-hk", "zh-mo", "zh-cn", "zh-hans", "zh-hans-cn", "zh-hans-hk", "zh-hans-mo",
            "zh-hans-sg", "zh-sg", "zh-tw", "zh-hant", "zh-hant-hk", "zh-hant-mo", "zh-hant-tw",
            "hr", "hr-hr",
            "cs", "cs-cz",
            "da", "da-dk",
            "nl", "nl-aw", "nl-be", "nl-cw", "nl-nl", "nl-sx",
            "en", "en-as", "en-us", "en-au", "en-bb", "en-be", "en-bz", "en-bm", "en-bw", "en-cm",
            "en-ca", "en-gh", "en-gu", "en-gy", "en-hk", "en-in", "en-ie", "en-jm", "en-ke",
            "en-mw", "en-my", "en-mt", "en-mh", "en-mu", "en-na", "en-nz", "en-ng", "en-mp",
            "en-pk", "en-ph", "en-rw", "en-sg", "en-za", "en-tz", "en-th", "en-tt", "en-um",
            "en-vi", "en-ug", "en-gb", "en-zm", "en-zw",
            "et", "et-ee",
            "fa", "fa-af", "fa-ir",
            "fil", "fil-ph",
            "fi", "fi-fi",
            "fr", "fr-dz", "fr-be", "fr-bj", "fr-bf", "fr-bi", "fr-cm", "fr-ca", "fr-cf", "fr-td",
            "fr-km", "fr-cg", "fr-cd", "fr-ci", "fr-dj", "fr-gq", "fr-fr", "fr-gf", "fr-ga",
            "fr-gp", "fr-gn", "fr-lu", "fr-mg", "fr-ml", "fr-mq", "fr-mu", "fr-yt", "fr-mc",
            "fr-ma", "fr-ne", "fr-re", "fr-rw", "fr-bl", "fr-mf", "fr-sn", "fr-sc", "fr-ch",
            "fr-tg", "fr-tn",
            "de", "de-at", "de-be", "de-de", "de-li", "de-lu", "de-ch",
            "el", "el-cy", "el-gr",
            "gu",
            "he", "he-il",
            "hi", "hi-in",
            "hu", "hu-hu",
            "is", "is-is",
            "id", "id-id",
            "it", "it-it", "it-ch",
            "ja", "ja-jp",
            "kn",
            "kk", "kk-kz",
            "ko", "ko-kr",
            "lv", "lv-lv",
            "lt", "lt-lt",
            "mk", "mk-mk",
            "ms", "ms-bn", "ms-my",
            "mr",
            "nb",
            "pl", "pl-pl",
            "pt", "pt-ao", "pt-br", "pt-cv", "pt-gw", "pt-mz", "pt-pt", "pt-st",
            "pa", "pa-in",
            "ro", "ro-md", "ro-mo", "ro-ro",
            "ru", "ru-md", "ru-mo", "ru-ru", "ru-ua",
            "sr", "sr-latn", "sr-latn-ba", "sr-me", "sr-rs",
            "sk", "sk-sk",
            "sl", "sl-sl",
            "es", "es-ar", "es-bo", "es-cl", "es-co", "es-cr", "es-do", "es-ec", "es-sv", "es-gq",
            "es-gt", "es-hn", "es-419", "es-mx", "es-ni", "es-pa", "es-py", "es-pe", "es-pr",
            "es-es", "es-us", "es-uy", "es-ve",
            "sw", "sw-cd", "sw-ke", "sw-tz", "sw-ug",
            "sv", "sv-fi", "sv-se",
            "tl",
            "ta", "ta-in", "ta-lk",
            "te", "te-in",
            "th", "th-th",
            "tr", "tr-tr",
            "uk", "uk-ua",
            "ur", "ur-bd", "ur-in", "ur-np", "ur-pk",
            "uz", "uz-latn", "uz-latn-uz",
            "vi", "vi-vn"
        )
    }
}
