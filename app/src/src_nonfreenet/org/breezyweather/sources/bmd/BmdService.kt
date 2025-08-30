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

package org.breezyweather.sources.bmd

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.bmd.json.BmdData
import org.breezyweather.sources.bmd.json.BmdForecastResult
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class BmdService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "bmd"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("bn")) {
            "বাংলাদেশ আবহাওয়া অধিদপ্তর"
        } else {
            "BMD (${context.currentLocale.getCountryName("BD")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(BMD_API_BASE_URL)
            .build()
            .create(BmdApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    private val weatherAttribution = if (context.currentLocale.code.startsWith("bn")) {
        "বাংলাদেশ আবহাওয়া অধিদপ্তর"
    } else {
        "Bangladesh Meteorological Department"
    }
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.bmd.gov.bd/"
    )
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("BD", ignoreCase = true)
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
        val upazila = location.parameters.getOrElse(id) { null }?.getOrElse("upazila") { null }
        if (upazila.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val daily = mApi.getDaily(
            pCode = upazila
        )
        val hourly = mApi.getHourly(
            pCode = upazila
        )

        return Observable.zip(daily, hourly) {
                dailyResult: BmdForecastResult,
                hourlyResult: BmdForecastResult,
            ->
            WeatherWrapper(
                dailyForecast = getDailyForecast(context, upazila, dailyResult),
                hourlyForecast = getHourlyForecast(context, upazila, hourlyResult)
            )
        }
    }

    private fun getDailyForecast(
        context: Context,
        upazila: String,
        dailyResult: BmdForecastResult,
    ): List<DailyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        val rfDayMap = mutableMapOf<String, Double?>()
        val rfNightMap = mutableMapOf<String, Double?>()
        val maxTMap = mutableMapOf<String, Double?>()
        val minTMap = mutableMapOf<String, Double?>()
        val wsMap = mutableMapOf<String, Double?>()
        val wdMap = mutableMapOf<String, Double?>()
        val wgMap = mutableMapOf<String, Double?>()
        val ccMap = mutableMapOf<String, Int?>()
        val ccDayMap = mutableMapOf<String, Int?>()
        val ccNightMap = mutableMapOf<String, Int?>()
        val rhMap = mutableMapOf<String, Double?>()
        dailyResult.data?.getOrElse(upazila) { null }?.forecastData?.let { forecast ->
            forecast.rf?.forEach {
                rfDayMap[it.stepStart] = it.valAvgDay?.times(12) // 12 hours
                rfNightMap[it.stepStart] = it.valAvgNight?.times(12) // 12 hours
            }
            forecast.temp?.forEachIndexed { index, temp ->
                maxTMap[temp.stepStart] = temp.valMax
                // Min temp of next day
                minTMap[temp.stepStart] = forecast.temp.getOrElse(index + 1) { null }?.valMin
            }
            forecast.windspd?.forEach {
                wsMap[it.stepStart] = it.valMax
            }
            forecast.winddir?.forEach {
                wdMap[it.stepStart] = getCorrectWindDirection(
                    min = it.valMin,
                    avg = it.valAvg,
                    max = it.valMax
                )
            }
            forecast.windgust?.forEach {
                wgMap[it.stepStart] = it.valMax
            }
            forecast.cldcvr?.forEach {
                ccMap[it.stepStart] = it.valAvg?.times(12.5)?.toInt()
                ccDayMap[it.stepStart] = it.valAvgDay?.times(12.5)?.toInt()
                ccNightMap[it.stepStart] = it.valAvgNight?.times(12.5)?.toInt()
            }
            forecast.rh?.forEach {
                rhMap[it.stepStart] = it.valAvg
            }
        }

        return wsMap.keys.sorted().map { key ->
            DailyWrapper(
                date = formatter.parse(key)!!,
                day = HalfDayWrapper(
                    weatherText = getWeatherText(
                        context = context,
                        cloudCover = ccDayMap.getOrElse(key) { null },
                        rainfall = rfDayMap.getOrElse(key) { null }
                    ),
                    weatherCode = getWeatherCode(
                        cloudCover = ccDayMap.getOrElse(key) { null },
                        rainfall = rfDayMap.getOrElse(key) { null }
                    ),
                    temperature = TemperatureWrapper(
                        temperature = maxTMap.getOrElse(key) { null }?.celsius
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null }?.kilometersPerHour,
                        gusts = wgMap.getOrElse(key) { null }?.kilometersPerHour
                    ),
                    precipitation = Precipitation(
                        total = rfDayMap.getOrElse(key) { null }?.millimeters
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(
                        context = context,
                        cloudCover = ccNightMap.getOrElse(key) { null },
                        rainfall = rfNightMap.getOrElse(key) { null }
                    ),
                    weatherCode = getWeatherCode(
                        cloudCover = ccNightMap.getOrElse(key) { null },
                        rainfall = rfNightMap.getOrElse(key) { null }
                    ),
                    temperature = TemperatureWrapper(
                        temperature = minTMap.getOrElse(key) { null }?.celsius
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null }?.kilometersPerHour,
                        gusts = wgMap.getOrElse(key) { null }?.kilometersPerHour
                    ),
                    precipitation = Precipitation(
                        total = rfNightMap.getOrElse(key) { null }?.millimeters
                    )
                ),
                relativeHumidity = rhMap.getOrElse(key) { null }?.let {
                    DailyRelativeHumidity(average = it.percent)
                },
                cloudCover = ccMap.getOrElse(key) { null }?.let {
                    DailyCloudCover(average = it.percent)
                }
            )
        }
    }

    private fun getHourlyForecast(
        context: Context,
        upazila: String,
        hourlyResult: BmdForecastResult,
    ): List<HourlyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T00:'HH:mm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
        val hourlyList = mutableListOf<HourlyWrapper>()
        val rfMap = mutableMapOf<String, Double?>()
        val tMap = mutableMapOf<String, Double?>()
        val rhMap = mutableMapOf<String, Double?>()
        val wsMap = mutableMapOf<String, Double?>()
        val wdMap = mutableMapOf<String, Double?>()
        val wgMap = mutableMapOf<String, Double?>()
        val ccMap = mutableMapOf<String, Int?>()

        hourlyResult.data?.getOrElse(upazila) { null }?.forecastData?.let { forecast ->
            forecast.rf?.forEach {
                rfMap[it.stepStart] = it.valMax
            }
            forecast.temp?.forEach {
                tMap[it.stepStart] = it.valAvg
            }
            forecast.rh?.forEach {
                rhMap[it.stepStart] = it.valAvg
            }
            forecast.windspd?.forEach {
                wsMap[it.stepStart] = it.valAvg
            }
            forecast.winddir?.forEach {
                wdMap[it.stepStart] = getCorrectWindDirection(
                    min = it.valMin,
                    avg = it.valAvg,
                    max = it.valMax
                )
            }
            forecast.windgust?.forEach {
                wgMap[it.stepStart] = it.valAvg
            }
            forecast.cldcvr?.forEach {
                ccMap[it.stepStart] = it.valAvg?.times(12.5)?.toInt()
            }
        }

        var date: Date
        rfMap.keys.sorted().forEach { key ->
            date = formatter.parse(key)!!
            hourlyList.add(
                HourlyWrapper(
                    date = date,
                    weatherText = getWeatherText(
                        context = context,
                        cloudCover = ccMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    weatherCode = getWeatherCode(
                        cloudCover = ccMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    temperature = TemperatureWrapper(
                        temperature = tMap.getOrElse(key) { null }?.celsius
                    ),
                    precipitation = Precipitation(
                        total = rfMap.getOrElse(key) { null }?.millimeters
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null }?.kilometersPerHour,
                        gusts = wgMap.getOrElse(key) { null }?.kilometersPerHour
                    ),
                    relativeHumidity = rhMap.getOrElse(key) { null }?.percent,
                    cloudCover = ccMap.getOrElse(key) { null }?.percent
                )
            )
        }
        return hourlyList
    }

    // The "average" wind direction may incorrectly point southward,
    // if the "minimum" direction is in the NE and the "maximum" direction is in the NW.
    // This function flips the wind direction back.
    private fun getCorrectWindDirection(
        min: Double?,
        avg: Double?,
        max: Double?,
    ): Double? {
        max?.let {
            min?.let {
                if ((max - min) > 180.0) {
                    return avg?.plus(180.0)?.mod(360.0)
                }
            }
        }
        return avg
    }

    // Using the same algorithm as https://bmd.bdservers.site/src/js/dashboard.js
    // These functions are needed because when Hourly only has 4 days, yet Daily has 10 days,
    // weather conditions are not automatically populated for the last 6 days in the Daily chart.
    private fun getWeatherText(
        context: Context,
        cloudCover: Int?,
        rainfall: Double?,
    ): String? {
        return cloudCover?.let {
            when {
                cloudCover <= 12 -> context.getString(R.string.common_weather_text_clear_sky)
                cloudCover <= 37 -> context.getString(R.string.common_weather_text_mostly_clear)
                (rainfall == null || rainfall < 1.0) -> when {
                    cloudCover <= 62 -> context.getString(R.string.common_weather_text_partly_cloudy)
                    cloudCover < 100 -> context.getString(R.string.common_weather_text_cloudy)
                    cloudCover == 100 -> context.getString(R.string.common_weather_text_overcast)
                    else -> null
                }
                rainfall <= 22.0 -> context.getString(R.string.common_weather_text_rain_light)
                rainfall <= 43.0 -> context.getString(R.string.common_weather_text_rain_moderate)
                else -> context.getString(R.string.common_weather_text_rain_heavy)
            }
        }
    }

    private fun getWeatherCode(
        cloudCover: Int?,
        rainfall: Double?,
    ): WeatherCode? {
        return cloudCover?.let {
            when {
                cloudCover <= 37 -> WeatherCode.CLEAR
                (rainfall == null || rainfall < 1.0) -> when {
                    cloudCover <= 62 -> WeatherCode.PARTLY_CLOUDY
                    cloudCover <= 100 -> WeatherCode.CLOUDY
                    else -> null
                }
                else -> WeatherCode.RAIN
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val upazila = getUpazila(latitude, longitude)
        return mApi.getDaily(
            pCode = upazila
        ).map { dailyResult ->
            val locationList = mutableListOf<LocationAddressInfo>()
            dailyResult.data?.getOrElse(upazila) { null }?.let {
                locationList.add(
                    convertLocation(it)
                )
            }
            locationList
        }
    }

    private fun convertLocation(
        data: BmdData,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = "Asia/Dhaka",
            countryCode = "BD",
            admin1 = data.divisionName,
            admin2 = data.districtName,
            city = data.upazilaName
        )
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val upazila = location.parameters.getOrElse(id) { null }?.getOrElse("upazila") { null }

        return upazila.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val upazila = getUpazila(location.latitude, location.longitude)
        return Observable.just(
            mapOf(
                "upazila" to upazila
            )
        )
    }

    private fun getUpazila(
        latitude: Double,
        longitude: Double,
    ): String {
        val url = "https://bmd.bdservers.site/Dashboard/getUpazilaByLatLon/$latitude/$longitude"
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().use { call ->
            if (call.isSuccessful) {
                call.body.string()
            } else {
                throw InvalidLocationException()
            }
        }
    }

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val BMD_API_BASE_URL = "https://api.bdservers.site/"
    }
}
