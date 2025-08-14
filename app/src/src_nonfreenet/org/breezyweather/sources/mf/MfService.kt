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

package org.breezyweather.sources.mf

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.mf.json.MfCurrentResult
import org.breezyweather.sources.mf.json.MfForecastDaily
import org.breezyweather.sources.mf.json.MfForecastHourly
import org.breezyweather.sources.mf.json.MfForecastProbability
import org.breezyweather.sources.mf.json.MfForecastResult
import org.breezyweather.sources.mf.json.MfNormalsResult
import org.breezyweather.sources.mf.json.MfRainResult
import org.breezyweather.sources.mf.json.MfWarningDictionaryResult
import org.breezyweather.sources.mf.json.MfWarningsOverseasResult
import org.breezyweather.sources.mf.json.MfWarningsResult
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Date
import java.util.Objects
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Mf weather service.
 */
class MfService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "mf"
    private val countryName = context.currentLocale.getCountryName("FR")
    override val name = "Météo-France".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://meteofrance.com/application-meteo-france-politique-de-confidentialite"

    private val mApi by lazy {
        client
            .baseUrl(MF_BASE_URL)
            .build()
            .create(MfApi::class.java)
    }

    private val weatherAttribution = "Météo-France (Etalab)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Météo-France" to "https://meteofrance.com/"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.CURRENT -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.MINUTELY -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.ALERT -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
                    .any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.NORMALS -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "MC", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
                    .any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.FORECAST, SourceFeature.REVERSE_GEOCODING -> true // Main source available worldwide
            else -> false
        }
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(
                location,
                // Since forecast and reverse geocoding are available worldwide, use the same criteria as normals
                if (feature in arrayOf(SourceFeature.FORECAST, SourceFeature.REVERSE_GEOCODING)) {
                    SourceFeature.NORMALS
                } else {
                    feature
                }
            ) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val languageCode = context.currentLocale.code
        val token = getToken()
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(MfCurrentResult())
            }
        } else {
            Observable.just(MfCurrentResult())
        }

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                USER_AGENT,
                location.latitude,
                location.longitude,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MfForecastResult())
            }
        } else {
            Observable.just(MfForecastResult())
        }

        val rain = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getRain(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(MfRainResult())
            }
        } else {
            Observable.just(MfRainResult())
        }

        val domain = location.parameters.getOrElse(id) { null }?.getOrElse("domain") { null }
        if (SourceFeature.ALERT in requestedFeatures && domain.isNullOrEmpty()) {
            failedFeatures[SourceFeature.ALERT] = InvalidLocationException()
        }

        val warningsJ0 = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && !domain.startsWith("VIGI")) {
                mApi.getWarnings(
                    USER_AGENT,
                    domain,
                    "J0",
                    "iso",
                    token
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningsResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for oversea territories
                Observable.just(MfWarningsResult())
            }
        } else {
            Observable.just(MfWarningsResult())
        }
        val warningsJ1 = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && !domain.startsWith("VIGI")) {
                mApi.getWarnings(
                    USER_AGENT,
                    domain,
                    "J1",
                    "iso",
                    token
                ).onErrorResumeNext {
                    // Don't fail on tomorrow. At 00:00, J1 alerts are not yet generated and fails
                    Observable.just(MfWarningsResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for oversea territories
                Observable.just(MfWarningsResult())
            }
        } else {
            Observable.just(MfWarningsResult())
        }

        val warningsOverseasDictionary = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && domain.startsWith("VIGI")) {
                mApi.getOverseasWarningsDictionary(
                    USER_AGENT,
                    domain,
                    token
                ).onErrorResumeNext {
                    it.printStackTrace()
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningDictionaryResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for metropolitan area
                Observable.just(MfWarningDictionaryResult())
            }
        } else {
            Observable.just(MfWarningDictionaryResult())
        }

        val warningsOverseas = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && domain.startsWith("VIGI")) {
                mApi.getOverseasWarnings(
                    USER_AGENT,
                    domain,
                    if (domain == "VIGI974") "vigilance4colors" else null,
                    "iso",
                    token
                ).onErrorResumeNext {
                    it.printStackTrace()
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningsOverseasResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for metropolitan area
                Observable.just(MfWarningsOverseasResult())
            }
        } else {
            Observable.just(MfWarningsOverseasResult())
        }

        // TODO: Only call once a month, unless it’s current position
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                USER_AGENT,
                location.latitude,
                location.longitude,
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(MfNormalsResult())
            }
        } else {
            Observable.just(MfNormalsResult())
        }

        return Observable.zip(
            current,
            forecast,
            rain,
            warningsJ0,
            warningsJ1,
            warningsOverseasDictionary,
            warningsOverseas,
            normals
        ) {
                currentResult: MfCurrentResult,
                forecastResult: MfForecastResult,
                rainResult: MfRainResult,
                warningsJ0Result: MfWarningsResult,
                warningsJ1Result: MfWarningsResult,
                warningsDictionaryResult: MfWarningDictionaryResult,
                warningsOverseasResult: MfWarningsOverseasResult,
                normalsResult: MfNormalsResult,
            ->
            WeatherWrapper(
                /*base = Base(
                    publishDate = forecastResult.updateTime ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        location,
                        forecastResult.properties?.dailyForecast
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        forecastResult.properties?.forecast,
                        forecastResult.properties?.probabilityForecast
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(rainResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    if (!domain.isNullOrEmpty()) {
                        if (domain.startsWith("VIGI")) {
                            getOverseasWarningsList(warningsDictionaryResult, warningsOverseasResult)
                        } else {
                            getWarningsList(warningsJ0Result, warningsJ1Result)
                        }
                    } else {
                        null
                    }
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(currentResult: MfCurrentResult): CurrentWrapper? {
        if (currentResult.properties?.gridded == null) {
            return null
        }

        return CurrentWrapper(
            weatherText = currentResult.properties.gridded.weatherDescription,
            weatherCode = getWeatherCode(currentResult.properties.gridded.weatherIcon),
            temperature = TemperatureWrapper(
                temperature = currentResult.properties.gridded.temperature
            ),
            wind = Wind(
                degree = currentResult.properties.gridded.windDirection?.toDouble(),
                speed = currentResult.properties.gridded.windSpeed
            )
        )
    }

    private fun getDailyList(
        location: Location,
        dailyForecasts: List<MfForecastDaily>?,
    ): List<DailyWrapper> {
        if (dailyForecasts.isNullOrEmpty()) return emptyList()
        val dailyList: MutableList<DailyWrapper> = ArrayList(dailyForecasts.size)
        for (i in 0 until dailyForecasts.size - 1) {
            val dailyForecast = dailyForecasts[i]
            // Given as UTC, we need to convert in the correct timezone at 00:00
            val dayInUTCCalendar = dailyForecast.time.toCalendarWithTimeZone(TimeZone.getTimeZone("UTC"))
            val dayInLocalCalendar = Calendar.getInstance(location.timeZone).apply {
                set(Calendar.YEAR, dayInUTCCalendar[Calendar.YEAR])
                set(Calendar.MONTH, dayInUTCCalendar[Calendar.MONTH])
                set(Calendar.DAY_OF_MONTH, dayInUTCCalendar[Calendar.DAY_OF_MONTH])
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val theDayInLocal = dayInLocalCalendar.time
            dailyList.add(
                DailyWrapper(
                    date = theDayInLocal,
                    day = HalfDayWrapper(
                        // Too complicated to get weather from hourly, so let's just use daily info for both day and night
                        weatherText = dailyForecast.dailyWeatherDescription,
                        weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                        temperature = TemperatureWrapper(temperature = dailyForecast.tMax)
                    ),
                    night = HalfDayWrapper(
                        weatherText = dailyForecast.dailyWeatherDescription,
                        weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                        // tMin is for current day, so it actually takes the previous night,
                        // so we try to get tMin from next day if available
                        temperature = TemperatureWrapper(temperature = dailyForecasts.getOrNull(i + 1)?.tMin)
                    ),
                    uV = UV(index = dailyForecast.uvIndex?.toDouble()),
                    relativeHumidity = DailyRelativeHumidity(
                        min = dailyForecast.relativeHumidityMin?.toDouble(),
                        max = dailyForecast.relativeHumidityMax?.toDouble()
                    )
                )
            )
        }
        return dailyList
    }

    private fun getHourlyList(
        hourlyForecastList: List<MfForecastHourly>?,
        probabilityForecastResult: List<MfForecastProbability>?,
    ): List<HourlyWrapper>? {
        return hourlyForecastList?.map { hourlyForecast ->
            HourlyWrapper(
                date = hourlyForecast.time,
                weatherText = hourlyForecast.weatherDescription,
                weatherCode = getWeatherCode(hourlyForecast.weatherIcon),
                temperature = TemperatureWrapper(
                    temperature = hourlyForecast.t,
                    feelsLike = hourlyForecast.tWindchill
                ),
                precipitation = getHourlyPrecipitation(hourlyForecast),
                precipitationProbability = getHourlyPrecipitationProbability(
                    probabilityForecastResult,
                    hourlyForecast.time
                ),
                wind = Wind(
                    degree = hourlyForecast.windDirection?.toDouble(),
                    speed = hourlyForecast.windSpeed?.toDouble(),
                    // Seems to be always 0? Or not available in low wind speeds maybe
                    gusts = hourlyForecast.windSpeedGust?.toDouble()
                ),
                relativeHumidity = hourlyForecast.relativeHumidity?.toDouble(),
                pressure = hourlyForecast.pSea?.hectopascals,
                cloudCover = hourlyForecast.totalCloudCover
            )
        }
    }

    private fun getHourlyPrecipitation(hourlyForecast: MfForecastHourly): Precipitation {
        val rainCumul = with(hourlyForecast) {
            rain1h ?: rain3h ?: rain6h ?: rain12h ?: rain24h
        }
        val snowCumul = with(hourlyForecast) {
            snow1h ?: snow3h ?: snow6h ?: snow12h ?: snow24h
        }
        return Precipitation(
            total = (rainCumul + snowCumul)?.millimeters,
            rain = rainCumul?.millimeters,
            snow = snowCumul?.millimeters
        )
    }

    /**
     * TODO: Needs to be reviewed
     */
    private fun getHourlyPrecipitationProbability(
        probabilityForecastResult: List<MfForecastProbability>?,
        dt: Date,
    ): PrecipitationProbability? {
        if (probabilityForecastResult.isNullOrEmpty()) return null

        var rainProbability: Double? = null
        var snowProbability: Double? = null
        var iceProbability: Double? = null
        for (probabilityForecast in probabilityForecastResult) {
            /*
             * Probablity are given every 3 hours, sometimes every 6 hours.
             * Sometimes every 3 hour-schedule give 3 hours probability AND 6 hours probability,
             * sometimes only one of them
             * It's not very clear, but we take all hours in order.
             */
            if (probabilityForecast.time.time == dt.time ||
                probabilityForecast.time.time + 1.hours.inWholeMilliseconds == dt.time ||
                probabilityForecast.time.time + 2.hours.inWholeMilliseconds == dt.time
            ) {
                if (probabilityForecast.rainHazard3h != null) {
                    rainProbability = probabilityForecast.rainHazard3h.toDouble()
                } else if (probabilityForecast.rainHazard6h != null) {
                    rainProbability = probabilityForecast.rainHazard6h.toDouble()
                }
                if (probabilityForecast.snowHazard3h != null) {
                    snowProbability = probabilityForecast.snowHazard3h.toDouble()
                } else if (probabilityForecast.snowHazard6h != null) {
                    snowProbability = probabilityForecast.snowHazard6h.toDouble()
                }
                if (probabilityForecast.freezingHazard != null) {
                    iceProbability = probabilityForecast.freezingHazard.toDouble()
                }
            }

            /*
             * If it's found as part of the "6 hour schedule" and we find later a "3 hour schedule"
             * the "3 hour schedule" will overwrite the "6 hour schedule" below with the above
             */
            if (probabilityForecast.time.time + 3.hours.inWholeMilliseconds == dt.time ||
                probabilityForecast.time.time + 4.hours.inWholeMilliseconds == dt.time ||
                probabilityForecast.time.time + 5.hours.inWholeMilliseconds == dt.time
            ) {
                if (probabilityForecast.rainHazard6h != null) {
                    rainProbability = probabilityForecast.rainHazard6h.toDouble()
                }
                if (probabilityForecast.snowHazard6h != null) {
                    snowProbability = probabilityForecast.snowHazard6h.toDouble()
                }
                if (probabilityForecast.freezingHazard != null) {
                    iceProbability = probabilityForecast.freezingHazard.toDouble()
                }
            }
        }
        return PrecipitationProbability(
            maxOf(rainProbability ?: 0.0, snowProbability ?: 0.0, iceProbability ?: 0.0),
            null,
            rainProbability,
            snowProbability,
            iceProbability
        )
    }

    private fun getMinutelyList(rainResult: MfRainResult?): List<Minutely> {
        val minutelyList: MutableList<Minutely> = arrayListOf()
        rainResult?.properties?.rainForecasts?.forEachIndexed { i, rainForecast ->
            minutelyList.add(
                Minutely(
                    date = rainForecast.time,
                    minuteInterval = if (i < rainResult.properties.rainForecasts.size - 1) {
                        (rainResult.properties.rainForecasts[i + 1].time.time - rainForecast.time.time)
                            .div(1.minutes.inWholeMilliseconds)
                            .toDouble().roundToInt()
                    } else {
                        (rainForecast.time.time - rainResult.properties.rainForecasts[i - 1].time.time)
                            .div(1.minutes.inWholeMilliseconds)
                            .toDouble().roundToInt()
                    },
                    precipitationIntensity = if (rainForecast.rainIntensity != null) {
                        getPrecipitationIntensity(rainForecast.rainIntensity).millimeters
                    } else {
                        null
                    }
                )
            )
        }
        return minutelyList
    }

    private fun getOverseasWarningsList(
        warningsDictionaryResult: MfWarningDictionaryResult,
        warningsResult: MfWarningsOverseasResult,
    ): List<Alert> {
        val alertList: MutableList<Alert> = arrayListOf()
        warningsResult.text?.let {
            if (!it.textBlocItems.isNullOrEmpty()) {
                val title = "Bulletin de Vigilance météo"
                val content = StringBuilder()
                it.textBlocItems.forEach { textBlocItem ->
                    if (content.toString().isNotEmpty()) {
                        content.append("\n\n")
                    }
                    textBlocItem.title?.forEach { t ->
                        content.append("<h2>$t</h2>\n")
                    }
                    textBlocItem.text?.forEach { txt ->
                        content.append("$txt\n")
                    }
                }
                alertList.add(
                    Alert(
                        // Create unique ID from: alert type ID, alert level, start time
                        alertId = Objects.hash(title, warningsResult.colorMax, it.beginTime).toString(),
                        startDate = it.beginTime,
                        endDate = it.endTime,
                        headline = title,
                        description = content.toString(),
                        source = "Météo-France",
                        severity = AlertSeverity.EXTREME, // Let’s put it on top
                        color = warningsDictionaryResult.colors?.firstOrNull { c -> c.id == warningsResult.colorMax }
                            ?.hexaCode?.toColorInt()
                            ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                    )
                )
            }
        }
        warningsResult.timelaps?.forEach { timelaps ->
            timelaps.timelapsItems
                ?.filter { it.colorId > 1 }
                ?.forEach { timelapsItem ->
                    val consequences = warningsResult.consequences
                        ?.firstOrNull { it.phenomenonId == timelaps.phenomenonId }
                        ?.textConsequence
                    val advices = warningsResult.advices
                        ?.firstOrNull { it.phenomenonId == timelaps.phenomenonId }
                        ?.textAdvice

                    val content = StringBuilder()
                    if (!consequences.isNullOrEmpty()) {
                        if (content.toString().isNotEmpty()) {
                            content.append("\n\n")
                        }
                        // TODO: Move to non-translatable en/fr strings
                        content
                            .append("<h2>Conséquences possibles</h2>\n")
                            .append(consequences)
                    }
                    if (!advices.isNullOrEmpty()) {
                        if (content.toString().isNotEmpty()) {
                            content.append("\n\n")
                        }
                        // TODO: Move to non-translatable en/fr strings
                        content
                            .append("<h2>Conseils de comportement</h2>\n")
                            .append(advices)
                    }

                    alertList.add(
                        Alert(
                            // Create unique ID from: alert type ID, alert level, start time
                            alertId = Objects.hash(
                                timelaps.phenomenonId,
                                timelapsItem.colorId,
                                timelapsItem.beginTime.time
                            ).toString(),
                            startDate = timelapsItem.beginTime,
                            endDate = timelapsItem.endTime,
                            headline = warningsDictionaryResult.phenomenons
                                ?.firstOrNull { c -> c.id == timelaps.phenomenonId }
                                ?.name
                                ?: getWarningType(timelaps.phenomenonId.toString()),
                            description = content.toString(),
                            source = "Météo-France",
                            severity = warningsDictionaryResult.colors
                                ?.firstOrNull { c -> c.id == timelapsItem.colorId }
                                ?.name?.let { h ->
                                    with(h) {
                                        when {
                                            contains("rouge") || contains("violet") -> AlertSeverity.EXTREME
                                            contains("orange") -> AlertSeverity.SEVERE
                                            contains("jaune") || contains("blanc") -> AlertSeverity.MODERATE
                                            contains("vert") || contains("bleu") -> AlertSeverity.MINOR
                                            else -> AlertSeverity.UNKNOWN
                                        }
                                    }
                                } ?: AlertSeverity.UNKNOWN,
                            color = warningsDictionaryResult.colors?.firstOrNull { c -> c.id == timelapsItem.colorId }
                                ?.hexaCode?.toColorInt()
                                ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                        )
                    )
                }
        }
        return alertList
    }

    private fun getWarningsList(warningsJ0Result: MfWarningsResult, warningsJ1Result: MfWarningsResult): List<Alert> {
        return getMergedBulletinWarning(warningsJ0Result, warningsJ1Result) +
            getWarningsList(warningsJ0Result) +
            getWarningsList(warningsJ1Result)
    }

    private fun getMergedBulletinWarning(
        warningsJ0Result: MfWarningsResult,
        warningsJ1Result: MfWarningsResult,
    ): List<Alert> {
        if (warningsJ0Result.text == null && warningsJ1Result.text == null) return emptyList()

        val warningBulletinJ0 = getBulletinWarning(warningsJ0Result)
        val warningBulletinJ1 = getBulletinWarning(warningsJ1Result)

        return if (warningBulletinJ0 != null && warningBulletinJ1 != null) {
            if (warningBulletinJ0.headline == warningBulletinJ1.headline &&
                warningBulletinJ0.startDate == warningBulletinJ1.startDate &&
                warningBulletinJ0.color == warningBulletinJ1.color &&
                warningBulletinJ0.description == warningBulletinJ1.description
            ) {
                // In case bulletins are identical, let's show the one from J1 which has a later validity end date
                listOf(warningBulletinJ1)
            } else {
                listOf(warningBulletinJ0, warningBulletinJ1)
            }
        } else if (warningBulletinJ0 != null) {
            listOf(warningBulletinJ0)
        } else if (warningBulletinJ1 != null) {
            listOf(warningBulletinJ1)
        } else {
            emptyList()
        }
    }

    fun getBulletinWarning(warningsResult: MfWarningsResult): Alert? {
        return warningsResult.text?.let {
            if (warningsResult.updateTime != null) {
                val textBlocs = it.textBlocItems?.filter { textBlocItem ->
                    textBlocItem.textItems?.any { textItem -> textItem.hazardCode == null } == true
                }
                if (!textBlocs.isNullOrEmpty()) {
                    val colors = mutableListOf<String>()
                    textBlocs.forEach { textBlocItem ->
                        textBlocItem.textItems?.forEach { textItem ->
                            if (textItem.hazardCode == null) {
                                textItem.termItems?.forEach { termItem ->
                                    if (!termItem.riskName.isNullOrEmpty()) {
                                        colors.add(termItem.riskName)
                                    }
                                }
                            }
                        }
                    }
                    val color = getWarningColor(colors)
                    val title = it.blocTitle ?: "Bulletin de Vigilance météo"
                    Alert(
                        // Create unique ID from: alert type ID, alert level, start time
                        alertId = Objects.hash(title, color, warningsResult.updateTime).toString(),
                        startDate = warningsResult.updateTime,
                        endDate = warningsResult.endValidityTime,
                        headline = title,
                        description = getWarningContent(null, warningsResult),
                        source = "Météo-France",
                        severity = AlertSeverity.EXTREME, // Let’s put it on top
                        color = color
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun getWarningsList(warningsResult: MfWarningsResult): List<Alert> {
        val alertList: MutableList<Alert> = arrayListOf()
        warningsResult.timelaps?.forEach { timelaps ->
            timelaps.timelapsItems
                ?.filter { it.colorId > 1 }
                ?.forEach { timelapsItem ->
                    alertList.add(
                        Alert(
                            // Create unique ID from: alert type ID, alert level, start time
                            alertId = Objects.hash(
                                timelaps.phenomenonId,
                                timelapsItem.colorId,
                                timelapsItem.beginTime.time
                            ).toString(),
                            startDate = timelapsItem.beginTime,
                            endDate = timelapsItem.endTime,
                            headline = getWarningType(timelaps.phenomenonId) +
                                " — " +
                                getWarningText(timelapsItem.colorId),
                            description = if (timelapsItem.colorId >= 3) {
                                getWarningContent(timelaps.phenomenonId, warningsResult)
                            } else {
                                null
                            },
                            source = "Météo-France",
                            severity = AlertSeverity.getInstance(timelapsItem.colorId),
                            color = getWarningColor(timelapsItem.colorId)
                        )
                    )
                }
        }
        return alertList
    }

    private fun getNormals(normalsResult: MfNormalsResult): Map<Month, Normals>? {
        val normalsStats = normalsResult.properties?.stats
        return if (!normalsStats.isNullOrEmpty()) {
            Month.entries.associateWith { month ->
                normalsStats.getOrElse(month.value - 1) { null }?.let {
                    Normals(
                        daytimeTemperature = it.tMax,
                        nighttimeTemperature = it.tMin
                    )
                }
            }.filter { it.value != null } as Map<Month, Normals>
        } else {
            null
        }
    }

    private fun getPrecipitationIntensity(rain: Int): Double = when (rain) {
        4 -> Precipitation.PRECIPITATION_HOURLY_HEAVY
        3 -> Precipitation.PRECIPITATION_HOURLY_MEDIUM
        2 -> Precipitation.PRECIPITATION_HOURLY_LIGHT
        else -> 0.0
    }

    // TODO: Move to non-translatable en/fr strings
    private fun getWarningType(phemononId: String): String = when (phemononId) {
        "1" -> "Vent"
        "2" -> "Pluie-inondation"
        "3" -> "Orages"
        "4" -> "Crues"
        "5" -> "Neige-verglas"
        "6" -> "Canicule"
        "7" -> "Grand froid"
        "8" -> "Avalanches"
        "9" -> "Vagues-submersion"
        else -> "Divers"
    }

    // TODO: Move to non-translatable en/fr strings
    private fun getWarningText(colorId: Int): String = when (colorId) {
        4 -> "Vigilance absolue"
        3 -> "Soyez très vigilant"
        2 -> "Soyez attentif"
        else -> "Pas de vigilance particulière"
    }

    @ColorInt
    private fun getWarningColor(colors: List<String>): Int = when {
        colors.contains("Rouge") -> Color.rgb(204, 0, 0)
        colors.contains("Orange") -> Color.rgb(255, 184, 43)
        colors.contains("Jaune") -> Color.rgb(255, 246, 0)
        colors.contains("Vert") -> Color.rgb(49, 170, 53)
        else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
    }

    @ColorInt
    private fun getWarningColor(colorId: Int): Int = when (colorId) {
        4 -> Color.rgb(204, 0, 0)
        3 -> Color.rgb(255, 184, 43)
        2 -> Color.rgb(255, 246, 0)
        1 -> Color.rgb(49, 170, 53)
        else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
    }

    private fun getWarningContent(phenomenonId: String?, warningsResult: MfWarningsResult): String? {
        val textBlocs = warningsResult.text?.textBlocItems?.filter { textBlocItem ->
            textBlocItem.textItems?.any { it.hazardCode == phenomenonId } == true
        }
        val consequences = warningsResult.consequences
            ?.firstOrNull { it.phenomenonId == phenomenonId }
            ?.textConsequence
        val advices = warningsResult.advices
            ?.firstOrNull { it.phenomenonId == phenomenonId }
            ?.textAdvice

        val content = StringBuilder()
        if (!textBlocs.isNullOrEmpty()) {
            textBlocs.forEach { textBlocItem ->
                if (content.toString().isNotEmpty()) {
                    content.append("\n\n")
                }
                if (!textBlocItem.typeName.isNullOrEmpty()) {
                    content.append("<h2>${textBlocItem.typeName}</h2>\n")
                }
                textBlocItem.textItems?.filter { it.hazardCode == phenomenonId }?.forEach { textItem ->
                    textItem.termItems?.forEachIndexed { termItemIndex, termItem ->
                        termItem.subdivisionTexts?.forEachIndexed { subdivisionIndex, subdivisionText ->
                            if (!subdivisionText.underlineText.isNullOrEmpty()) {
                                content.append("<u>${subdivisionText.underlineText}</u> ")
                            }
                            if (!subdivisionText.boldText.isNullOrEmpty()) {
                                content.append("<b>${subdivisionText.boldText}</b> ")
                            }
                            subdivisionText.text?.let {
                                content.append(it.joinToString("\n"))
                            }
                            if (subdivisionIndex != termItem.subdivisionTexts.lastIndex) {
                                content.append("\n\n")
                            }
                        }
                        if (termItemIndex != textItem.termItems.lastIndex) {
                            content.append("\n\n")
                        }
                    }
                }
            }
        }
        if (!consequences.isNullOrEmpty()) {
            if (content.toString().isNotEmpty()) {
                content.append("\n\n")
            }
            // TODO: Move to non-translatable en/fr strings
            content
                .append("<h2>Conséquences possibles</h2>\n")
                .append(consequences)
        }
        if (!advices.isNullOrEmpty()) {
            if (content.toString().isNotEmpty()) {
                content.append("\n\n")
            }
            // TODO: Move to non-translatable en/fr strings
            content
                .append("<h2>Conseils de comportement</h2>\n")
                .append(advices)
        }

        return content.toString().ifEmpty { null }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return if (icon == null) {
            null
        } else {
            with(icon) {
                when {
                    // We need to take care of two-digits first
                    startsWith("p32") ||
                        startsWith("p33") ||
                        startsWith("p34") -> WeatherCode.WIND
                    startsWith("p31") -> null // What is this?
                    startsWith("p26") ||
                        startsWith("p27") ||
                        startsWith("p28") ||
                        startsWith("p29") -> WeatherCode.THUNDER
                    startsWith("p26") ||
                        startsWith("p27") ||
                        startsWith("p28") ||
                        startsWith("p29") -> WeatherCode.THUNDER
                    startsWith("p21") ||
                        startsWith("p22") ||
                        startsWith("p23") -> WeatherCode.SNOW
                    startsWith("p19") ||
                        startsWith("p20") -> WeatherCode.HAIL
                    startsWith("p17") ||
                        startsWith("p18") -> WeatherCode.SLEET
                    startsWith("p16") ||
                        startsWith("p24") ||
                        startsWith("p25") ||
                        startsWith("p30") -> WeatherCode.THUNDERSTORM
                    startsWith("p9") ||
                        startsWith("p10") ||
                        startsWith("p11") ||
                        startsWith("p12") ||
                        startsWith("p13") ||
                        startsWith("p14") ||
                        startsWith("p15") -> WeatherCode.RAIN
                    startsWith("p6") ||
                        startsWith("p7") ||
                        startsWith("p8") -> WeatherCode.FOG
                    startsWith("p4") ||
                        startsWith("p5") -> WeatherCode.HAZE
                    startsWith("p3") -> WeatherCode.CLOUDY
                    startsWith("p2") -> WeatherCode.PARTLY_CLOUDY
                    startsWith("p1") -> WeatherCode.CLEAR
                    else -> null
                }
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getForecast(
            USER_AGENT,
            latitude,
            longitude,
            "iso",
            getToken()
        ).map {
            buildList {
                convertLocation(it)?.let { loc ->
                    add(loc)
                }
            }
        }
    }

    private fun convertLocation(
        result: MfForecastResult,
    ): LocationAddressInfo? {
        if (result.properties == null) return null

        return LocationAddressInfo(
            timeZoneId = result.properties.timezone,
            country = result.properties.country,
            countryCode = result.properties.country.substring(0, 2),
            admin2 = if (!result.properties.frenchDepartment.isNullOrEmpty()) {
                frenchDepartments.getOrElse(result.properties.frenchDepartment) { null }
            } else {
                null
            }, // Département
            admin2Code = result.properties.frenchDepartment, // Département
            city = result.properties.name
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        /*
         * FIXME: Empty doesn't always include alerts
         * Just to be safe we query it when Meteo-France is the main source
         * See also #1497
         */
        if (SourceFeature.ALERT !in features) return false

        if (coordinatesChanged) return true

        return location.parameters.getOrElse(id) { null }?.getOrElse("domain") { null }.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getForecast(
            USER_AGENT,
            location.latitude,
            location.longitude,
            "iso",
            getToken()
        ).map {
            if (it.properties?.frenchDepartment.isNullOrEmpty()) {
                throw InvalidLocationException()
            }

            val domain = if (frenchDepartments.containsKey(it.properties!!.frenchDepartment!!)) {
                it.properties.frenchDepartment!!
            } else if (overseaTerritories.containsKey(it.properties.frenchDepartment)) {
                if (it.properties.frenchDepartment in arrayOf("977", "978")) {
                    "VIGI978-977"
                } else {
                    "VIGI${it.properties.frenchDepartment}"
                }
            } else {
                null
            }

            if (domain == null) {
                throw InvalidLocationException()
            }

            mapOf(
                "domain" to domain
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var wsftKey: String
        set(value) {
            config.edit().putString("wsft_key", value).apply()
        }
        get() = config.getString("wsft_key", null) ?: ""

    private fun getWsftKeyOrDefault(): String {
        return wsftKey.ifEmpty { BuildConfig.MF_WSFT_KEY }
    }

    override val isConfigured
        get() = getToken().isNotEmpty()

    override val isRestricted = false

    private fun getToken(): String {
        return if (getWsftKeyOrDefault() != BuildConfig.MF_WSFT_KEY) {
            // If default key was changed, we want to use it
            getWsftKeyOrDefault()
        } else {
            // Otherwise, we try first a JWT key, otherwise fallback on regular API key
            try {
                Jwts.builder().apply {
                    header().add("typ", "JWT")
                    claims().empty().add("class", "mobile")
                    issuedAt(Date())
                    id(UUID.randomUUID().toString())
                    signWith(
                        Keys.hmacShaKeyFor(
                            BuildConfig.MF_WSFT_JWT_KEY.toByteArray(
                                StandardCharsets.UTF_8
                            )
                        ),
                        Jwts.SIG.HS256
                    )
                }.compact()
            } catch (ignored: Exception) {
                BuildConfig.MF_WSFT_KEY
            }
        }
    }

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_mf_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = wsftKey,
                onValueChanged = {
                    wsftKey = it
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    // TODO: At least FR is known to be ambiguous
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val MF_BASE_URL = "https://webservice.meteofrance.com/"
        private const val USER_AGENT = "okhttp/4.9.2"

        private val frenchDepartments: Map<String, String> = mapOf(
            "01" to "Ain",
            "02" to "Aisne",
            "03" to "Allier",
            "04" to "Alpes de Hautes-Provence",
            "05" to "Hautes-Alpes",
            "06" to "Alpes-Maritimes",
            "07" to "Ardèche",
            "08" to "Ardennes",
            "09" to "Ariège",
            "10" to "Aube",
            "11" to "Aude",
            "12" to "Aveyron",
            "13" to "Bouches-du-Rhône",
            "14" to "Calvados",
            "15" to "Cantal",
            "16" to "Charente",
            "17" to "Charente-Maritime",
            "18" to "Cher",
            "19" to "Corrèze",
            "21" to "Côte-d'Or",
            "22" to "Côtes d'Armor",
            "23" to "Creuse",
            "24" to "Dordogne",
            "25" to "Doubs",
            "26" to "Drôme",
            "27" to "Eure",
            "28" to "Eure-et-Loir",
            "29" to "Finistère",
            "2A" to "Corse-du-Sud",
            "2B" to "Haute-Corse",
            "30" to "Gard",
            "31" to "Haute-Garonne",
            "32" to "Gers",
            "33" to "Gironde",
            "34" to "Hérault",
            "35" to "Ille-et-Vilaine",
            "36" to "Indre",
            "37" to "Indre-et-Loire",
            "38" to "Isère",
            "39" to "Jura",
            "40" to "Landes",
            "41" to "Loir-et-Cher",
            "42" to "Loire",
            "43" to "Haute-Loire",
            "44" to "Loire-Atlantique",
            "45" to "Loiret",
            "46" to "Lot",
            "47" to "Lot-et-Garonne",
            "48" to "Lozère",
            "49" to "Maine-et-Loire",
            "50" to "Manche",
            "51" to "Marne",
            "52" to "Haute-Marne",
            "53" to "Mayenne",
            "54" to "Meurthe-et-Moselle",
            "55" to "Meuse",
            "56" to "Morbihan",
            "57" to "Moselle",
            "58" to "Nièvre",
            "59" to "Nord",
            "60" to "Oise",
            "61" to "Orne",
            "62" to "Pas-de-Calais",
            "63" to "Puy-de-Dôme",
            "64" to "Pyrénées-Atlantiques",
            "65" to "Hautes-Pyrénées",
            "66" to "Pyrénées-Orientales",
            "67" to "Bas-Rhin",
            "68" to "Haut-Rhin",
            "69" to "Rhône",
            "70" to "Haute-Saône",
            "71" to "Saône-et-Loire",
            "72" to "Sarthe",
            "73" to "Savoie",
            "74" to "Haute-Savoie",
            "75" to "Paris",
            "76" to "Seine-Maritime",
            "77" to "Seine-et-Marne",
            "78" to "Yvelines",
            "79" to "Deux-Sèvres",
            "80" to "Somme",
            "81" to "Tarn",
            "82" to "Tarn-et-Garonne",
            "83" to "Var",
            "84" to "Vaucluse",
            "85" to "Vendée",
            "86" to "Vienne",
            "87" to "Haute-Vienne",
            "88" to "Vosges",
            "89" to "Yonne",
            "90" to "Territoire-de-Belfort",
            "91" to "Essonne",
            "92" to "Hauts-de-Seine",
            "93" to "Seine-Saint-Denis",
            "94" to "Val-de-Marne",
            "95" to "Val-d'Oise",
            "99" to "Andorre"
        )

        private val overseaTerritories: Map<String, String> = mapOf(
            "971" to "Guadeloupe",
            "972" to "Martinique",
            "973" to "Guyane",
            "974" to "La Réunion",
            "975" to "Saint-Pierre-et-Miquelon",
            "976" to "Mayotte",
            "977" to "Saint-Barthélemy",
            "978" to "Saint-Martin",
            "986" to "Wallis-et-Futuna",
            "987" to "Polynésie française",
            "988" to "Nouvelle-Calédonie"
        )
    }
}
