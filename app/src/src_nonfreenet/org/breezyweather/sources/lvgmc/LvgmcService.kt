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

package org.breezyweather.sources.lvgmc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityLocationResult
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityResult
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentLocation
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentResult
import org.breezyweather.sources.lvgmc.json.LvgmcForecastResult
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours

class LvgmcService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "lvgmc"
    override val name = "LVĢMC (${context.currentLocale.getCountryName("LV")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client.baseUrl(LVGMC_BASE_URL)
            .build()
            .create(LvgmcApi::class.java)
    }

    private val weatherAttribution = "Latvijas Vides, ģeoloģijas un meteoroloģijas centrs"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to LVGMC_BASE_URL
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("LV", ignoreCase = true)
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
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        if (currentLocation.isNullOrEmpty() || forecastLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                scope = "daily",
                punkts = forecastLocation
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                scope = "hourly",
                punkts = forecastLocation
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val fromDate = formatter.format(Date(Date().time - 24.hours.inWholeMilliseconds))
        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality(
                station = airQualityLocation,
                fromDate = fromDate
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, airQuality) {
                currentResult: List<LvgmcCurrentResult>,
                dailyResult: List<LvgmcForecastResult>,
                hourlyResult: List<LvgmcForecastResult>,
                aq: List<LvgmcAirQualityResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(location, currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = AirQuality(
                            pM25 = aq.filter { it.code == "PM2.5_60min" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.microgramsPerCubicMeter,
                            pM10 = aq.filter { it.code == "PM10_60min" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.microgramsPerCubicMeter,
                            sO2 = aq.filter { it.code == "SO2" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.microgramsPerCubicMeter,
                            nO2 = aq.filter { it.code == "NO2" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.microgramsPerCubicMeter,
                            o3 = aq.filter { it.code == "O3" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.microgramsPerCubicMeter,
                            cO = aq.filter { it.code == "CO" }.sortedByDescending { it.time }
                                .firstOrNull()?.value?.milligramsPerCubicMeter
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
        location: Location,
        currentResult: List<LvgmcCurrentResult>,
    ): CurrentWrapper? {
        val id = "lvgmc"
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        if (currentLocation.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        return currentResult
            .filter { it.stationCode == currentLocation }
            .sortedByDescending { it.time }
            .firstOrNull()
            ?.let {
                CurrentWrapper(
                    temperature = TemperatureWrapper(
                        it.temperature?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.toDoubleOrNull(),
                        speed = it.windSpeed?.toDoubleOrNull(),
                        gusts = it.windGusts?.toDoubleOrNull()
                    ),
                    uV = UV(
                        index = it.uvIndex?.toDoubleOrNull()
                    ),
                    relativeHumidity = it.relativeHumidity?.toDoubleOrNull(),
                    pressure = it.pressure?.toDoubleOrNull()?.hectopascals,
                    visibility = it.visibility?.toDoubleOrNull()?.meters
                )
            }
    }

    private fun getDailyForecast(
        context: Context,
        dailyResult: List<LvgmcForecastResult>,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val dayParts = mutableMapOf<Long, HalfDayWrapper>()
        val nightParts = mutableMapOf<Long, HalfDayWrapper>()
        val uviMap = mutableMapOf<Long, Double?>()
        var time: Long
        dailyResult
            .filter { it.time != null && Regex("""^\d{12}$""").matches(it.time) }
            .forEach {
                if (it.time!!.substring(8, 10) == "00") {
                    // night part of previous day:
                    // Subtracting 23 hours will keep it within the previous day
                    // during daylight saving time switchover
                    time = formatter.parse(it.time.substring(0, 8))!!.time - 23.hours.inWholeMilliseconds
                    nightParts[time] = HalfDayWrapper(
                        weatherText = getWeatherText(context, it.icon),
                        weatherCode = getWeatherCode(it.icon),
                        temperature = TemperatureWrapper(
                            temperature = it.temperature?.toDoubleOrNull()
                        ),
                        precipitation = Precipitation(
                            total = it.precipitation12h?.toDoubleOrNull()?.millimeters
                        ),
                        wind = Wind(
                            degree = it.windDirection?.toDoubleOrNull(),
                            speed = it.windSpeed?.toDoubleOrNull(),
                            gusts = it.windGusts?.toDoubleOrNull()
                        )
                    )
                }
                if (it.time.substring(8, 10) == "12") {
                    // day part of current day:
                    time = formatter.parse(it.time.substring(0, 8))!!.time
                    dayParts[time] = HalfDayWrapper(
                        weatherText = getWeatherText(context, it.icon),
                        weatherCode = getWeatherCode(it.icon),
                        temperature = TemperatureWrapper(
                            temperature = it.temperature?.toDoubleOrNull()
                        ),
                        precipitation = Precipitation(
                            total = it.precipitation12h?.toDoubleOrNull()?.millimeters
                        ),
                        wind = Wind(
                            degree = it.windDirection?.toDoubleOrNull(),
                            speed = it.windSpeed?.toDoubleOrNull(),
                            gusts = it.windGusts?.toDoubleOrNull()
                        )
                    )
                    uviMap[time] = it.uvIndex?.toDoubleOrNull()
                }
            }
        nightParts.keys.sorted().forEach { key ->
            dailyList.add(
                DailyWrapper(
                    date = Date(key),
                    day = dayParts.getOrElse(key) { null },
                    night = nightParts.getOrElse(key) { null },
                    uV = UV(
                        index = uviMap.getOrElse(key) { null }
                    )
                )
            )
        }
        if (dayParts.keys.maxOf { it } != nightParts.keys.maxOf { it }) {
            val lastKey = dayParts.keys.maxOf { it }
            dailyList.add(
                DailyWrapper(
                    date = Date(lastKey),
                    day = dayParts.getOrElse(lastKey) { null },
                    uV = UV(
                        index = uviMap.getOrElse(lastKey) { null }
                    )
                )
            )
        }
        return dailyList
    }

    private fun getHourlyForecast(
        context: Context,
        hourlyResult: List<LvgmcForecastResult>,
    ): List<HourlyWrapper> {
        val formatter = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")

        return hourlyResult
            .filter { it.time != null && Regex("""^\d{12}$""").matches(it.time) }
            .map {
                HourlyWrapper(
                    date = formatter.parse(it.time!!)!!,
                    weatherText = getWeatherText(context, it.icon),
                    weatherCode = getWeatherCode(it.icon),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.toDoubleOrNull(),
                        feelsLike = it.apparentTemperature?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation1h?.toDoubleOrNull()?.millimeters,
                        snow = it.snow?.toDoubleOrNull()?.millimeters
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = it.precipitationProbability?.toDoubleOrNull(),
                        thunderstorm = it.thunderstormProbability?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.toDoubleOrNull(),
                        speed = it.windSpeed?.toDoubleOrNull(),
                        gusts = it.windGusts?.toDoubleOrNull()
                    ),
                    uV = UV(
                        index = it.uvIndex?.toDoubleOrNull()
                    ),
                    relativeHumidity = it.relativeHumidity?.toDoubleOrNull(),
                    pressure = it.pressure?.toDoubleOrNull()?.hectopascals,
                    cloudCover = it.cloudCover?.toIntOrNull()
                )
            }
    }

    private fun getWeatherText(
        context: Context,
        icon: String?,
    ): String? {
        return when (icon?.substring(1, 4)) {
            "101" -> context.getString(R.string.common_weather_text_clear_sky)
            "102", "103" -> context.getString(R.string.common_weather_text_partly_cloudy)
            "104" -> context.getString(R.string.common_weather_text_cloudy)
            "105" -> context.getString(R.string.common_weather_text_overcast)
            "201", "202", "203" -> context.getString(R.string.common_weather_text_drizzle_freezing)
            "204", "205", "206" -> context.getString(R.string.common_weather_text_rain_freezing)
            "207", "208" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "301", "302", "303", "304", "305", "306" -> context.getString(R.string.weather_kind_thunderstorm)
            "307", "308", "309", "310", "311", "312" -> context.getString(R.string.weather_kind_hail)
            "313", "314", "315", "316" -> context.getString(R.string.weather_kind_thunderstorm)
            "317", "318", "319", "320", "321", "322" -> context.getString(R.string.weather_kind_hail)
            "323", "324", "325" -> context.getString(R.string.weather_kind_thunderstorm)
            "401", "402", "403", "404" -> context.getString(R.string.common_weather_text_fog)
            "405", "406" -> context.getString(R.string.common_weather_text_drizzle)
            "407", "408" -> context.getString(R.string.common_weather_text_rain)
            // TODO: Migrate string
            "409", "410", "411", "412" -> context.getString(R.string.openmeteo_weather_text_depositing_rime_fog)
            "413", "414" -> context.getString(R.string.common_weather_text_snow)
            "415", "416" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "501", "502", "503" -> context.getString(R.string.common_weather_text_drizzle)
            "504", "505", "506" -> context.getString(R.string.common_weather_text_rain)
            "507", "508", "509" -> context.getString(R.string.common_weather_text_rain_heavy)
            "510", "511", "512" -> context.getString(R.string.common_weather_text_rain_freezing)
            "601", "602", "603", "607", "608" -> context.getString(R.string.common_weather_text_snow)
            "604", "605", "606", "609", "610" -> context.getString(R.string.common_weather_text_snow_heavy)
            "611", "612", "613" -> context.getString(R.string.common_weather_text_snow_heavy)
            "614", "615", "616" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "617", "618", "619" -> context.getString(R.string.common_weather_text_drizzle)
            "701" -> context.getString(R.string.common_weather_text_sand_storm)
            "702" -> context.getString(R.string.weather_kind_haze)
            "703" -> context.getString(R.string.common_weather_text_squall)
            else -> null
        }
    }

    private fun getWeatherCode(
        icon: String?,
    ): WeatherCode? {
        return when (icon?.substring(1, 4)) {
            "101" -> WeatherCode.CLEAR
            "102", "103" -> WeatherCode.PARTLY_CLOUDY
            "104", "105" -> WeatherCode.CLOUDY
            "201", "202", "203" -> WeatherCode.SLEET
            "204", "205", "206" -> WeatherCode.SLEET
            "207", "208" -> WeatherCode.SLEET
            "301", "302", "303", "304", "305", "306" -> WeatherCode.THUNDERSTORM
            "307", "308", "309", "310", "311", "312" -> WeatherCode.HAIL
            "313", "314", "315", "316" -> WeatherCode.THUNDERSTORM
            "317", "318", "319", "320", "321", "322" -> WeatherCode.HAIL
            "323", "324", "325" -> WeatherCode.THUNDERSTORM
            "401", "402", "403", "404" -> WeatherCode.FOG
            "405", "406" -> WeatherCode.RAIN
            "407", "408" -> WeatherCode.RAIN
            "409", "410", "411", "412" -> WeatherCode.FOG
            "413", "414" -> WeatherCode.SNOW
            "415", "416" -> WeatherCode.SLEET
            "501", "502", "503" -> WeatherCode.RAIN
            "504", "505", "506" -> WeatherCode.RAIN
            "507", "508", "509" -> WeatherCode.RAIN
            "510", "511", "512" -> WeatherCode.SLEET
            "601", "602", "603", "607", "608" -> WeatherCode.SNOW
            "604", "605", "606", "609", "610" -> WeatherCode.SNOW
            "611", "612", "613" -> WeatherCode.SNOW
            "614", "615", "616" -> WeatherCode.SLEET
            "617", "618", "619" -> WeatherCode.RAIN
            "701" -> WeatherCode.WIND
            "702" -> WeatherCode.HAZE
            "703" -> WeatherCode.WIND
            else -> null
        }
    }

    // REVERSE GEOCODING
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val laiks = formatter.format(Date()) + "00"
        val bounds = getBoundingBox(latitude, longitude)
        return mApi.getForecastLocations(
            laiks = laiks,
            bounds = bounds
        ).map {
            convertLocation(latitude, longitude, it)
        }
    }

    private fun convertLocation(
        latitude: Double,
        longitude: Double,
        forecastLocationsResult: List<LvgmcForecastResult>,
    ): List<LocationAddressInfo> {
        val locationList = mutableListOf<LocationAddressInfo>()
        val forecastLocations = forecastLocationsResult.filter {
            it.point != null && it.latitude != null && it.longitude != null
        }.associate {
            it.point!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
        }
        val forecastLocation = LatLng(latitude, longitude).getNearestLocation(forecastLocations)

        forecastLocationsResult.firstOrNull { it.point == forecastLocation }?.let {
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = "Europe/Riga",
                    countryCode = "LV",
                    admin1 = it.municipality,
                    city = it.name
                )
            )
        }
        return locationList
    }

    // LOCATION PARAMETERS
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        return currentLocation.isNullOrEmpty() || forecastLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val laiks = formatter.format(Date()) + "00"
        val bounds = getBoundingBox(location.latitude, location.longitude)
        val forecastLocations = mApi.getForecastLocations(
            laiks = laiks,
            bounds = bounds
        )
        val currentLocations = mApi.getCurrentLocations()
        val airQualityLocations = mApi.getAirQualityLocations()

        return Observable.zip(currentLocations, forecastLocations, airQualityLocations) {
                currentLocationsResult: List<LvgmcCurrentLocation>,
                forecastLocationsResult: List<LvgmcForecastResult>,
                airQualityLocationsResult: List<LvgmcAirQualityLocationResult>,
            ->
            convertLocationParameters(
                location,
                currentLocationsResult,
                forecastLocationsResult,
                airQualityLocationsResult
            )
        }
    }

    // location parameters
    private fun convertLocationParameters(
        location: Location,
        currentLocationsResult: List<LvgmcCurrentLocation>,
        forecastLocationsResult: List<LvgmcForecastResult>,
        airQualityLocationsResult: List<LvgmcAirQualityLocationResult>,
    ): Map<String, String> {
        val forecastLocations = forecastLocationsResult.filter {
            it.point != null && it.latitude != null && it.longitude != null
        }.associate {
            it.point!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
        }

        val currentLocations = currentLocationsResult.filter {
            it.code != null && it.latitude != null && it.longitude != null
        }.associate {
            it.code!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
        }

        val airQualityLocations = airQualityLocationsResult.filter {
            it.id != null &&
                it.latitude != null &&
                it.longitude != null &&
                it.group == "Atmosfēras gaisa novērojumu stacija" &&
                it.isActive == true
        }.associate {
            it.id.toString() to LatLng(it.latitude!!, it.longitude!!)
        }

        val forecastLocation = LatLng(location.latitude, location.longitude).getNearestLocation(forecastLocations)
        val currentLocation = LatLng(location.latitude, location.longitude).getNearestLocation(currentLocations)
        val airQualityLocation = LatLng(location.latitude, location.longitude).getNearestLocation(airQualityLocations)

        if (forecastLocation.isNullOrEmpty() || currentLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        return mapOf(
            "forecastLocation" to forecastLocation,
            "currentLocation" to currentLocation,
            "airQualityLocation" to airQualityLocation
        )
    }

    private fun getBoundingBox(
        latitude: Double,
        longitude: Double,
    ): String {
        return "POLYGON((" +
            "${longitude - 0.1} ${latitude - 0.1}, " +
            "${longitude + 0.1} ${latitude - 0.1}, " +
            "${longitude + 0.1} ${latitude + 0.1}, " +
            "${longitude - 0.1} ${latitude + 0.1}, " +
            "${longitude - 0.1} ${latitude - 0.1}))"
    }

    override val testingLocations: List<Location> = emptyList()

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val LVGMC_BASE_URL = "https://videscentrs.lvgmc.lv/"
    }
}
