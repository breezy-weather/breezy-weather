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

package org.breezyweather.sources.ims

import android.content.Context
import androidx.core.graphics.toColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.sources.RefreshHelper
import org.breezyweather.sources.ims.json.ImsLocation
import org.breezyweather.sources.ims.json.ImsWeatherData
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

/**
 * Israel Meteorological Service
 */
class ImsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "ims"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                else -> "IMS (${context.currentLocale.getCountryName("IL")})"
            }
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("he") || startsWith("iw") -> "https://ims.gov.il/he/termOfuse"
                else -> "https://ims.gov.il/en/termOfuse"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(IMS_BASE_URL)
            .build()
            .create(ImsApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                else -> "Israel Meteorological Service"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://ims.gov.il/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        // Israel + West Bank + Gaza Strip
        return location.countryCode.equals("IL", ignoreCase = true) ||
            location.countryCode.equals("PS", ignoreCase = true)
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
        val locationId = location.parameters.getOrElse(id) { null }?.getOrElse("locationId") { null }

        if (locationId.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }

        return mApi.getWeather(languageCode, locationId).map {
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, it.data)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, it.data)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, it.data)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(context, it.data)
                } else {
                    null
                }
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        data: ImsWeatherData?,
    ): List<DailyWrapper>? {
        return data?.forecastData?.keys?.mapNotNull {
            it.toDateNoHour(location.timeZone)?.let { dayDate ->
                DailyWrapper(
                    date = dayDate,
                    uV = data.forecastData[it]!!.daily?.maximumUVI?.toDoubleOrNull()?.let { uvi ->
                        UV(uvi)
                    }
                )
            }
        }
    }

    private fun getHourlyForecast(
        context: Context,
        location: Location,
        data: ImsWeatherData?,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        var previousWeatherCode = ""
        var currentWeatherCode = ""
        data?.forecastData?.keys?.forEach {
            it.toDateNoHour(location.timeZone)?.let { dayDate ->
                data.forecastData[it]!!.hourly?.forEach { hourlyResult ->
                    val hourlyDate = dayDate.toCalendarWithTimeZone(location.timeZone).apply {
                        set(Calendar.HOUR_OF_DAY, hourlyResult.value.hour.toInt())
                    }.time
                    if (!hourlyResult.value.weatherCode.isNullOrEmpty()) {
                        currentWeatherCode = hourlyResult.value.weatherCode!!
                        previousWeatherCode = hourlyResult.value.weatherCode!!
                    } else {
                        currentWeatherCode = previousWeatherCode
                    }

                    hourlyList.add(
                        HourlyWrapper(
                            date = hourlyDate,
                            weatherText = getWeatherText(context, currentWeatherCode),
                            weatherCode = getWeatherCode(currentWeatherCode),
                            temperature = TemperatureWrapper(
                                temperature = hourlyResult.value.preciseTemperature?.toDoubleOrNull(),
                                feelsLike = hourlyResult.value.windChill?.toDoubleOrNull()
                            ),
                            precipitationProbability = PrecipitationProbability(
                                total = hourlyResult.value.rainChance?.toDoubleOrNull()
                            ),
                            wind = hourlyResult.value.windSpeed?.let { windSpeed ->
                                Wind(
                                    speed = windSpeed.kilometersPerHour,
                                    degree = hourlyResult.value.windDirectionId?.let { windDirId ->
                                        data.windDirections?.getOrElse(windDirId) { null }?.direction?.toDoubleOrNull()
                                    }
                                )
                            },
                            uV = hourlyResult.value.uvIndex?.toDoubleOrNull()?.let { uvi ->
                                UV(uvi)
                            },
                            relativeHumidity = hourlyResult.value.relativeHumidity?.toDoubleOrNull()
                        )
                    )
                }
            }
        }
        return hourlyList
    }

    private fun getCurrent(
        context: Context,
        data: ImsWeatherData?,
    ): CurrentWrapper? {
        if (data?.analysis == null) return null
        val firstDay = data.forecastData?.keys?.minOrNull()
        val dailyForecast = firstDay?.let { data.forecastData.getOrElse(it) { null }?.country?.description }

        return CurrentWrapper(
            weatherText = getWeatherText(context, data.analysis.weatherCode),
            weatherCode = getWeatherCode(data.analysis.weatherCode),
            temperature = data.analysis.temperature?.toDoubleOrNull()?.let {
                TemperatureWrapper(
                    temperature = it,
                    feelsLike = data.analysis.feelsLike?.toDoubleOrNull()
                )
            },
            wind = data.analysis.windSpeed?.let { windSpeed ->
                Wind(
                    speed = windSpeed.kilometersPerHour,
                    degree = data.analysis.windDirectionId?.let {
                        data.windDirections?.getOrElse(it) { null }?.direction?.toDoubleOrNull()
                    }
                )
            },
            uV = data.analysis.uvIndex?.toDoubleOrNull()?.let { UV(it) },
            relativeHumidity = data.analysis.relativeHumidity?.toDoubleOrNull(),
            dewPoint = data.analysis.dewPointTemp?.toDoubleOrNull(),
            dailyForecast = dailyForecast
        )
    }

    private fun getAlerts(
        context: Context,
        data: ImsWeatherData?,
    ): List<Alert>? {
        return data?.allWarnings?.mapNotNull { warningEntry ->
            val severity = when (warningEntry.value.severityId) {
                "6" -> AlertSeverity.EXTREME
                "4", "5" -> AlertSeverity.SEVERE
                "2", "3" -> AlertSeverity.MODERATE
                "0", "1" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                alertId = warningEntry.value.alertId,
                startDate = warningEntry.value.validFromUnix?.let { Date(it.times(1000L)) },
                // TODO: endDate = warningEntry.value.validTo, // TimeZone-dependant
                headline = warningEntry.value.warningTypeId?.let {
                    data.warningsMetadata?.imsWarningType?.getOrElse(it) { null }?.name
                },
                description = warningEntry.value.text,
                source = with(context.currentLocale.code) {
                    when {
                        startsWith("ar") -> "خدمة الأرصاد الجوية الإسرائيلية"
                        startsWith("he") || startsWith("iw") -> "השירות המטאורולוגי הישראלי"
                        else -> "Israel Meteorological Service"
                    }
                },
                severity = severity,
                color = warningEntry.value.severityId?.let { severityId ->
                    data.warningsMetadata?.warningSeverity?.getOrElse(severityId) { null }?.color?.toColorInt()
                } ?: Alert.colorFromSeverity(severity)
            )
        }
    }

    // Source: https://ims.gov.il/en/weather_codes
    private fun getWeatherText(
        context: Context,
        weatherCode: String?,
    ): String? {
        return when (weatherCode) {
            "1250" -> context.getString(R.string.common_weather_text_clear_sky)
            "1220" -> context.getString(R.string.common_weather_text_partly_cloudy)
            "1230" -> context.getString(R.string.common_weather_text_cloudy)
            "1570" -> context.getString(R.string.common_weather_text_dust)
            "1010" -> context.getString(R.string.common_weather_text_sand_storm)
            "1160" -> context.getString(R.string.common_weather_text_fog)
            "1310", "1580" -> context.getString(R.string.common_weather_text_hot)
            "1270" -> context.getString(R.string.common_weather_text_humid)
            "1320", "1590" -> context.getString(R.string.common_weather_text_cold)
            "1300" -> context.getString(R.string.common_weather_text_frost)
            "1140", "1530", "1540" -> context.getString(R.string.common_weather_text_rain)
            "1560" -> context.getString(R.string.common_weather_text_rain_light)
            "1020" -> context.getString(R.string.weather_kind_thunderstorm)
            "1510" -> context.getString(R.string.common_weather_text_rain_heavy)
            "1260" -> context.getString(R.string.weather_kind_wind)
            "1080" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "1070" -> context.getString(R.string.common_weather_text_snow_light)
            "1060" -> context.getString(R.string.common_weather_text_snow)
            "1520" -> context.getString(R.string.common_weather_text_snow_heavy)
            else -> null
        }
    }

    // Source: https://ims.gov.il/en/weather_codes
    private fun getWeatherCode(
        weatherCode: String?,
    ): WeatherCode? {
        return when (weatherCode) {
            "1250" -> WeatherCode.CLEAR
            "1220" -> WeatherCode.PARTLY_CLOUDY
            "1230" -> WeatherCode.CLOUDY
            "1570" -> WeatherCode.HAZE
            "1010" -> WeatherCode.WIND
            "1160" -> WeatherCode.FOG
            // "1310", "1580" -> Hot
            // "1270" -> Humid
            // "1320", "1590" -> Cold
            "1300" -> WeatherCode.SNOW // Frost
            "1140", "1530", "1540" -> WeatherCode.RAIN
            "1560" -> WeatherCode.RAIN
            "1020" -> WeatherCode.THUNDERSTORM
            "1510" -> WeatherCode.RAIN
            "1260" -> WeatherCode.WIND
            "1080" -> WeatherCode.SLEET
            "1070" -> WeatherCode.SNOW
            "1060" -> WeatherCode.SNOW
            "1520" -> WeatherCode.SNOW
            else -> null
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }
        return mApi.getLocations(languageCode)
            .map { result ->
                if (result.data.isNullOrEmpty()) {
                    throw ReverseGeocodingException()
                }

                val nearestStation = result.data
                    .values
                    .filter { station ->
                        station.lat.toDoubleOrNull() != null && station.lon.toDoubleOrNull() != null
                    }
                    .minByOrNull { station ->
                        SphericalUtil.computeDistanceBetween(
                            LatLng(latitude, longitude),
                            LatLng(station.lat.toDouble(), station.lon.toDouble())
                        )
                    } ?: throw ReverseGeocodingException()

                listOf(convertLocation(nearestStation))
            }
    }

    private fun convertLocation(
        result: ImsLocation,
    ): LocationAddressInfo {
        // This will make locations in disputed areas appear as being in Israel, but I guess people using IMS as their
        // address lookup source wouldn't be surprised by this kind of behavior
        return LocationAddressInfo(
            timeZoneId = "Asia/Jerusalem",
            countryCode = "IL",
            city = result.name
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocationId = location.parameters.getOrElse(id) { null }?.getOrElse("locationId") { null }

        return currentLocationId.isNullOrEmpty()
    }

    // TODO: Redundant with reverse geocoding
    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val languageCode = when (context.currentLocale.code) {
            "ar" -> "ar"
            "he", "iw" -> "he"
            else -> "en"
        }
        return mApi.getLocations(languageCode)
            .map { result ->
                if (result.data.isNullOrEmpty()) {
                    throw ReverseGeocodingException()
                }

                val nearestStation = result.data
                    .values
                    .filter { station ->
                        station.lat.toDoubleOrNull() != null && station.lon.toDoubleOrNull() != null
                    }
                    .minByOrNull { station ->
                        SphericalUtil.computeDistanceBetween(
                            LatLng(location.latitude, location.longitude),
                            LatLng(station.lat.toDouble(), station.lon.toDouble())
                        )
                    } ?: throw ReverseGeocodingException()

                val distanceWithNearestStation = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(nearestStation.lat.toDouble(), nearestStation.lon.toDouble())
                )

                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "${nearestStation.name}: $distanceWithNearestStation meters")
                }

                // Only add if within a reasonable distance
                if (distanceWithNearestStation > RefreshHelper.REVERSE_GEOCODING_DISTANCE_LIMIT) {
                    throw InvalidLocationException()
                }

                mapOf("locationId" to nearestStation.lid)
            }
    }

    override val testingLocations: List<Location> = emptyList()

    override val knownAmbiguousCountryCodes: Array<String>? = arrayOf("IL")

    companion object {
        private const val IMS_BASE_URL = "https://ims.gov.il/"
    }
}
