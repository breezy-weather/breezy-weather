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

package org.breezyweather.sources.namem

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.sources.namem.json.NamemAirQualityResult
import org.breezyweather.sources.namem.json.NamemCurrentResult
import org.breezyweather.sources.namem.json.NamemDailyResult
import org.breezyweather.sources.namem.json.NamemHourlyResult
import org.breezyweather.sources.namem.json.NamemNormalsResult
import org.breezyweather.sources.namem.json.NamemStation
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class NamemService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "namem"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "NAMEM (${context.currentLocale.getCountryName("MN")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(NAMEM_BASE_URL)
            .build()
            .create(NamemApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "National Agency for Meteorology and Environmental Monitoring"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks
        get() = mapOf(
            weatherAttribution to NAMEM_BASE_URL
        )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("MN", ignoreCase = true)
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
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }?.toLongOrNull()
        if (stationId == null) {
            return Observable.error(InvalidLocationException())
        }
        val body = """{"sid":$stationId}"""

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(NamemCurrentResult())
            }
        } else {
            Observable.just(NamemCurrentResult())
        }

        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(NamemHourlyResult())
            }
        } else {
            Observable.just(NamemHourlyResult())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(NamemDailyResult())
            }
        } else {
            Observable.just(NamemDailyResult())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(NamemNormalsResult())
            }
        } else {
            Observable.just(NamemNormalsResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality().onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(NamemAirQualityResult())
            }
        } else {
            Observable.just(NamemAirQualityResult())
        }

        return Observable.zip(current, daily, hourly, normals, airQuality) {
                currentResult: NamemCurrentResult,
                dailyResult: NamemDailyResult,
                hourlyResult: NamemHourlyResult,
                normalsResult: NamemNormalsResult,
                airQualityResult: NamemAirQualityResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(location, airQualityResult)
                    )
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)?.let {
                        mapOf(
                            Date().getCalendarMonth(location) to it
                        )
                    }
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        context: Context,
        currentResult: NamemCurrentResult,
    ): CurrentWrapper {
        val current = currentResult.aws?.getOrNull(0)
        return CurrentWrapper(
            weatherText = getWeatherText(context, current?.nh),
            weatherCode = getWeatherCode(current?.nh),
            temperature = TemperatureWrapper(
                temperature = current?.ttt,
                feelsLike = current?.tttFeels
            ),
            wind = Wind(
                degree = current?.windDir,
                speed = current?.windSpeed
            ),
            relativeHumidity = current?.ff,
            pressure = current?.pslp
        )
    }

    private fun getAirQuality(
        location: Location,
        airQualityResult: NamemAirQualityResult,
    ): AirQuality {
        val stationMap = airQualityResult.data?.filter {
            it.lat != null && it.lon != null && (it.sid != null || it.id != null)
        }?.associate {
            it.sid.toString() to LatLng(it.lat!!, it.lon!!)
        }
        val station = airQualityResult.data?.firstOrNull {
            it.sid.toString() == LatLng(location.latitude, location.longitude).getNearestLocation(stationMap, 50000.0)
        }
        var pM25: Double? = null
        var pM10: Double? = null
        var sO2: Double? = null
        var nO2: Double? = null
        var o3: Double? = null
        var cO: Double? = null
        station?.elementList?.forEach {
            if (it.unit == "АЧИ") {
                when (it.id) {
                    "pm25" -> pM25 = convertAqi(PollutantIndex.PM25, it.current)
                    "pm10" -> pM10 = convertAqi(PollutantIndex.PM10, it.current)
                    "so2" -> sO2 = convertAqi(PollutantIndex.SO2, it.current)
                    "no2" -> nO2 = convertAqi(PollutantIndex.NO2, it.current)
                    "o3" -> o3 = convertAqi(PollutantIndex.O3, it.current)
                    "co" -> cO = convertAqi(PollutantIndex.CO, it.current)
                }
            }
        }
        return AirQuality(
            pM25 = pM25,
            pM10 = pM10,
            sO2 = sO2,
            nO2 = nO2,
            o3 = o3,
            cO = cO
        )
    }

    private fun getNormals(
        normalsResult: NamemNormalsResult,
    ): Normals? {
        return normalsResult.foreMonthly?.lastOrNull { it.obsDate != null && it.obsDate < Date() }?.let {
            Normals(
                daytimeTemperature = it.ttMaxAve,
                nighttimeTemperature = it.ttMinAve
            )
        }
    }

    private fun getDailyForecast(
        context: Context,
        dailyResult: NamemDailyResult,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        var date: Date
        dailyResult.fore5Day?.forEachIndexed { i, forecast ->
            // account for western provinces which are 1 hour behind
            date = Date(forecast.foreDate!!.time.plus(3600000))
            if (i == 0) {
                dailyList.add(
                    DailyWrapper(
                        date = Date(date.time.minus(86400000)),
                        night = HalfDayWrapper(
                            weatherText = getWeatherText(context, forecast.wwN),
                            weatherCode = getWeatherCode(forecast.wwN),
                            temperature = TemperatureWrapper(
                                temperature = forecast.temN,
                                feelsLike = forecast.temNFeel
                            ),
                            precipitationProbability = PrecipitationProbability(
                                total = forecast.wwNPer
                            ),
                            wind = Wind(
                                speed = forecast.wndN
                            )
                        )
                    )
                )
            }
            dailyList.add(
                DailyWrapper(
                    date = date,
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, forecast.wwD),
                        weatherCode = getWeatherCode(forecast.wwD),
                        temperature = TemperatureWrapper(
                            temperature = forecast.temD,
                            feelsLike = forecast.temDFeel
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = forecast.wwDPer
                        ),
                        wind = Wind(
                            speed = forecast.wndD
                        )
                    ),
                    night = dailyResult.fore5Day.getOrNull(i + 1)?.let {
                        HalfDayWrapper(
                            weatherText = getWeatherText(context, it.wwN),
                            weatherCode = getWeatherCode(it.wwN),
                            temperature = TemperatureWrapper(
                                temperature = it.temN,
                                feelsLike = it.temNFeel
                            ),
                            precipitationProbability = PrecipitationProbability(
                                total = it.wwNPer
                            ),
                            wind = Wind(
                                speed = it.wndN
                            )
                        )
                    }
                )
            )
        }
        return dailyList
    }

    private fun getHourlyForecast(
        hourlyResult: NamemHourlyResult,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        hourlyResult.fore3Hours?.forEach {
            if (it.fdate !== null) {
                hourlyList.add(
                    HourlyWrapper(
                        date = it.fdate,
                        temperature = TemperatureWrapper(
                            temperature = it.tem?.toDoubleOrNull()
                        ),
                        precipitation = Precipitation(
                            total = it.pre?.toDoubleOrNull()
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = it.preProb?.toDoubleOrNull()
                        ),
                        wind = Wind(
                            speed = it.wnd?.toDoubleOrNull()
                        )
                    )
                )
            }
        }
        return hourlyList
    }

    private fun getWeatherText(
        context: Context,
        weather: Int?,
    ): String? {
        // TODO: Check with a Mongolian speaker (???) for correct interpretation of the text
        return when (weather) {
            2 -> context.getString(R.string.common_weather_text_clear_sky) // "Цэлмэг"
            3 -> context.getString(R.string.common_weather_text_overcast) // "Үүлэрхэг"
            5, 7 -> context.getString(R.string.common_weather_text_mostly_clear) // "Багавтар үүлтэй"
            9, 10 -> context.getString(R.string.common_weather_text_cloudy) // "Үүлшинэ"
            20 -> context.getString(R.string.common_weather_text_mostly_clear) // "Үүл багасна"
            23, 24 -> context.getString(R.string.common_weather_text_snow_light) // "Ялимгүй цас"
            // "Ялимгүй хур тунадас":
            27, 28 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_light)
            60 -> context.getString(R.string.common_weather_text_rain_light) // "Бага зэргийн бороо"
            61 -> context.getString(R.string.common_weather_text_rain) // "Бороо"
            63 -> context.getString(R.string.common_weather_text_rain_heavy) // "Их бороо"
            65 -> context.getString(R.string.common_weather_text_rain_snow_mixed) // "Хур тунадас"
            66 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy) // "Их хур тунадас"
            67 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy) // "Аадар их хур тунадас"
            68 -> context.getString(R.string.common_weather_text_rain_heavy) // "Их усархаг бороо"
            71 -> context.getString(R.string.common_weather_text_snow) // "Цас"
            73 -> context.getString(R.string.common_weather_text_snow_heavy) // "Их цас"
            75 -> context.getString(R.string.common_weather_text_snow_heavy) // "Аадар их цас"
            80, 81 -> context.getString(R.string.common_weather_text_rain_showers_light) // "Бага зэргийн аадар"
            82, 83 -> context.getString(R.string.common_weather_text_rain_showers) // "Аадар бороо"
            84, 85 -> context.getString(R.string.common_weather_text_rain_showers_heavy) // "Усархаг аадар бороо"
            90, 91 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах бага зэргийн аадар бороо"
            92, 93 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах аадар бороо"
            94, 95 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах усархаг аадар бороо"
            else -> null
        }
    }

    private fun getWeatherCode(
        weather: Int?,
    ): WeatherCode? {
        return when (weather) {
            2 -> WeatherCode.CLEAR // "Цэлмэг"
            3 -> WeatherCode.CLOUDY // "Үүлэрхэг"
            5, 7 -> WeatherCode.PARTLY_CLOUDY // "Багавтар үүлтэй"
            9, 10 -> WeatherCode.CLOUDY // "Үүлшинэ"
            20 -> WeatherCode.PARTLY_CLOUDY // "Үүл багасна"
            23, 24 -> WeatherCode.SNOW // "Ялимгүй цас"
            27, 28 -> WeatherCode.SLEET // "Ялимгүй хур тунадас"
            60 -> WeatherCode.RAIN // "Бага зэргийн бороо"
            61 -> WeatherCode.RAIN // "Бороо"
            63 -> WeatherCode.RAIN // "Их бороо"
            65 -> WeatherCode.SLEET // "Хур тунадас"
            66 -> WeatherCode.SLEET // "Их хур тунадас"
            67 -> WeatherCode.SLEET // "Аадар их хур тунадас"
            68 -> WeatherCode.RAIN // "Их усархаг бороо"
            71 -> WeatherCode.SNOW // "Цас"
            73 -> WeatherCode.SNOW // "Их цас"
            75 -> WeatherCode.SNOW // "Аадар их цас"
            80, 81 -> WeatherCode.RAIN // "Бага зэргийн аадар"
            82, 83 -> WeatherCode.RAIN // "Аадар бороо"
            84, 85 -> WeatherCode.RAIN // "Усархаг аадар бороо"
            90, 91 -> WeatherCode.THUNDERSTORM // "Дуу.Цах бага зэргийн аадар бороо"
            92, 93 -> WeatherCode.THUNDERSTORM // "Дуу.Цах аадар бороо"
            94, 95 -> WeatherCode.THUNDERSTORM // "Дуу.Цах усархаг аадар бороо"
            else -> null
        }
    }

    // Convert Mongolian AQI to pollutant concentration
// SO2, NO2, PM10, PM2.5, O3 in µg/m³
// CO in mg/m³
//
// Breakpoint source: http://agaar.mn/article-view/692
    private fun convertAqi(
        pollutant: PollutantIndex,
        aqi: Double?,
    ): Double? {
        if (aqi == null || aqi < 0.0) return null
        if (aqi == 0.0) return 0.0

        val thresholds = listOf(0.0, 50.0, 100.0, 200.0, 300.0, 400.0, 500.0)
        val breakpoints = mapOf<PollutantIndex, List<Double>>(
            PollutantIndex.SO2 to listOf(0.0, 100.0, 300.0, 800.0, 1600.0, 2100.0, 2620.0),
            PollutantIndex.NO2 to listOf(0.0, 100.0, 200.0, 700.0, 2000.0, 3500.0, 3840.0),
            PollutantIndex.PM10 to listOf(0.0, 50.0, 100.0, 300.0, 420.0, 500.0, 600.0),
            PollutantIndex.PM25 to listOf(0.0, 35.0, 50.0, 150.0, 250.0, 350.0, 500.0),
            PollutantIndex.CO to listOf(0.0, 10.0, 30.0, 60.0, 90.0, 120.0, 150.0),
            PollutantIndex.O3 to listOf(0.0, 50.0, 100.0, 265.0, 800.0, 1000.0, 1200.0)
        )
        // AQI less than 500
        for (i in 1..<thresholds.size) {
            if (aqi > thresholds[i - 1] && aqi <= thresholds[i]) {
                return (aqi - thresholds[i - 1]) /
                    (thresholds[i] - thresholds[i - 1]) *
                    (breakpoints[pollutant]!![i] - breakpoints[pollutant]!![i - 1]) +
                    (breakpoints[pollutant]!![i - 1])
            }
        }
        // AQI more than 500
        // linear extrapolation from the 400-500 range
        return (aqi - thresholds[5]) /
            (thresholds[6] - thresholds[5]) *
            (breakpoints[pollutant]!![6] - breakpoints[pollutant]!![5]) +
            (breakpoints[pollutant]!![5])
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getStations().map {
            convertLocation(latitude, longitude, it.locations)
        }
    }

    // Reverse geocoding
    internal fun convertLocation(
        latitude: Double,
        longitude: Double,
        stations: List<NamemStation>?,
    ): List<LocationAddressInfo> {
        val stationMap = stations?.filter {
            it.lat != null && it.lon != null && (it.sid != null || it.id != null)
        }?.associate {
            it.sid.toString() to LatLng(it.lat!!, it.lon!!)
        }
        val station = stations?.firstOrNull {
            it.sid.toString() == LatLng(latitude, longitude).getNearestLocation(stationMap, 200000.0)
        }

        if (station?.lat == null || station.lon == null) {
            throw InvalidLocationException()
        }
        return listOf(
            LocationAddressInfo(
                timeZoneId = when (station.provinceName) {
                    "Баян-Өлгий", // Bayan-Ölgii
                    "Говь-Алтай", // Govi-Altai
                    "Ховд", // Khovd
                    "Увс", // Uvs
                    "Завхан", // Zavkhan
                    -> "Asia/Hovd"
                    else -> "Asia/Ulaanbaatar"
                },
                countryCode = "MN",
                admin1 = station.provinceName,
                admin2 = station.districtName,
                city = station.districtName,
                district = if (station.stationName != station.districtName) {
                    station.stationName
                } else {
                    null
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

        val sid = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        return sid.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getStations().map {
            getLocationParameters(location, it.locations)
        }
    }

    // Location parameters
    private fun getLocationParameters(
        location: Location,
        stations: List<NamemStation>?,
    ): Map<String, String> {
        val stationMap = stations?.filter {
            it.lat != null && it.lon != null && (it.sid != null || it.id != null)
        }?.associate {
            it.sid.toString() to LatLng(it.lat!!, it.lon!!)
        }
        val nearestStation = LatLng(location.latitude, location.longitude).getNearestLocation(stationMap, 200000.0)
        if (nearestStation == null) {
            throw InvalidLocationException()
        }
        return mapOf(
            "stationId" to nearestStation
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val NAMEM_BASE_URL = "https://weather.gov.mn/"
    }
}
