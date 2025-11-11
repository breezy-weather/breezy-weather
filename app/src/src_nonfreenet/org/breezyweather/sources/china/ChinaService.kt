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

package org.breezyweather.sources.china

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.sources.china.json.ChinaCurrent
import org.breezyweather.sources.china.json.ChinaForecastDaily
import org.breezyweather.sources.china.json.ChinaForecastHourly
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaLocationResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

class ChinaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : ChinaServiceStub(context) {

    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://privacy.mi.com/all/zh_CN"
                else -> "https://privacy.mi.com/all/en_US"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(CHINA_WEATHER_BASE_URL)
            .build()
            .create(ChinaApi::class.java)
    }

    override val attributionLinks = mapOf(
        "彩云天气" to "https://caiyunapp.com/",
        "中国环境监测总站" to "https://www.cnemc.cn/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val locationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }

        if (locationKey.isNullOrEmpty()) {
            return if (location.isCurrentPosition) {
                Observable.error(ReverseGeocodingException())
            } else {
                Observable.error(InvalidLocationException())
            }
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val main = if (SourceFeature.FORECAST in requestedFeatures ||
            SourceFeature.CURRENT in requestedFeatures ||
            SourceFeature.AIR_QUALITY in requestedFeatures ||
            SourceFeature.ALERT in requestedFeatures
        ) {
            mApi.getForecastWeather(
                location.latitude,
                location.longitude,
                location.isCurrentPosition,
                locationKey = "weathercn:$locationKey",
                days = 15,
                appKey = CHINA_APP_KEY,
                sign = CHINA_SIGN,
                isGlobal = false,
                context.currentLocale.toString().lowercase()
            ).onErrorResumeNext {
                if (SourceFeature.FORECAST in requestedFeatures) {
                    failedFeatures[SourceFeature.FORECAST] = it
                }
                if (SourceFeature.CURRENT in requestedFeatures) {
                    failedFeatures[SourceFeature.CURRENT] = it
                }
                if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    failedFeatures[SourceFeature.AIR_QUALITY] = it
                }
                if (SourceFeature.ALERT in requestedFeatures) {
                    failedFeatures[SourceFeature.ALERT] = it
                }
                Observable.just(ChinaForecastResult())
            }
        } else {
            Observable.just(ChinaForecastResult())
        }
        val minutely = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getMinutelyWeather(
                location.latitude,
                location.longitude,
                context.currentLocale.toString().lowercase(),
                isGlobal = false,
                appKey = CHINA_APP_KEY,
                locationKey = "weathercn:$locationKey",
                sign = CHINA_SIGN
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(ChinaMinutelyResult())
            }
        } else {
            Observable.just(ChinaMinutelyResult())
        }
        return Observable.zip(main, minutely) { mainResult: ChinaForecastResult, minutelyResult: ChinaMinutelyResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        mainResult.current?.pubTime,
                        location,
                        mainResult.forecastDaily
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        mainResult.current?.pubTime,
                        location,
                        mainResult.forecastHourly
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(mainResult.current, minutelyResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    mainResult.aqi?.let {
                        AirQualityWrapper(
                            current = AirQuality(
                                pM25 = it.pm25?.toDoubleOrNull()?.microgramsPerCubicMeter,
                                pM10 = it.pm10?.toDoubleOrNull()?.microgramsPerCubicMeter,
                                sO2 = it.so2?.toDoubleOrNull()?.microgramsPerCubicMeter,
                                nO2 = it.no2?.toDoubleOrNull()?.microgramsPerCubicMeter,
                                o3 = it.o3?.toDoubleOrNull()?.microgramsPerCubicMeter,
                                cO = it.co?.toDoubleOrNull()?.milligramsPerCubicMeter
                            )
                        )
                    }
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(
                        location,
                        minutelyResult
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(mainResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        current: ChinaCurrent?,
        minutelyResult: ChinaMinutelyResult? = null,
    ): CurrentWrapper? {
        if (current == null) return null

        return CurrentWrapper(
            weatherText = getWeatherText(current.weather),
            weatherCode = getWeatherCode(current.weather),
            temperature = TemperatureWrapper(
                temperature = current.temperature?.value?.toDoubleOrNull()?.celsius,
                feelsLike = current.feelsLike?.value?.toDoubleOrNull()?.celsius
            ),
            wind = if (current.wind != null) {
                Wind(
                    degree = current.wind.direction?.value?.toDoubleOrNull(),
                    speed = current.wind.speed?.value?.toDoubleOrNull()?.kilometersPerHour
                )
            } else {
                null
            },
            uV = if (current.uvIndex != null) {
                UV(index = current.uvIndex.toDoubleOrNull())
            } else {
                null
            },
            relativeHumidity = if (!current.humidity?.value.isNullOrEmpty()) {
                current.humidity.value.toDoubleOrNull()?.percent
            } else {
                null
            },
            pressure = if (!current.pressure?.value.isNullOrEmpty()) {
                current.pressure.value.toDoubleOrNull()?.hectopascals
            } else {
                null
            },
            visibility = if (!current.visibility?.value.isNullOrEmpty()) {
                current.visibility.value.toDoubleOrNull()?.kilometers
            } else {
                null
            },
            hourlyForecast = if (minutelyResult?.precipitation != null) {
                minutelyResult.precipitation.description
            } else {
                null
            }
        )
    }

    private fun getDailyList(
        publishDate: Date?,
        location: Location,
        dailyForecast: ChinaForecastDaily?,
    ): List<DailyWrapper> {
        if (publishDate == null || dailyForecast?.weather?.value.isNullOrEmpty()) return emptyList()

        val dailyList: MutableList<DailyWrapper> = ArrayList(dailyForecast.weather.value.size)
        dailyForecast.weather.value.forEachIndexed { index, weather ->
            val calendar = publishDate.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_YEAR, index)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dailyList.add(
                DailyWrapper(
                    date = calendar.time,
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(weather.from),
                        weatherCode = getWeatherCode(weather.from),
                        temperature = TemperatureWrapper(
                            temperature = dailyForecast.temperature?.value?.getOrNull(index)?.from?.toDoubleOrNull()
                                ?.celsius
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = getPrecipitationProbability(dailyForecast, index)
                        ),
                        wind = if (dailyForecast.wind != null) {
                            Wind(
                                degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toDoubleOrNull(),
                                speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toDoubleOrNull()
                                    ?.kilometersPerHour
                            )
                        } else {
                            null
                        }
                    ),
                    night = HalfDayWrapper(
                        weatherText = getWeatherText(weather.to),
                        weatherCode = getWeatherCode(weather.to),
                        temperature = TemperatureWrapper(
                            temperature = dailyForecast.temperature?.value?.getOrNull(index)?.to?.toDoubleOrNull()
                                ?.celsius
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = getPrecipitationProbability(dailyForecast, index)
                        ),
                        wind = if (dailyForecast.wind != null) {
                            Wind(
                                degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toDoubleOrNull(),
                                speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toDoubleOrNull()
                                    ?.kilometersPerHour
                            )
                        } else {
                            null
                        }
                    )
                )
            )
        }
        return dailyList
    }

    private fun getPrecipitationProbability(forecast: ChinaForecastDaily, index: Int): Ratio? {
        if (forecast.precipitationProbability == null || forecast.precipitationProbability.value.isNullOrEmpty()) {
            return null
        }

        return forecast.precipitationProbability.value.getOrNull(index)?.toDoubleOrNull()?.percent
    }

    private fun getHourlyList(
        publishDate: Date?,
        location: Location,
        hourlyForecast: ChinaForecastHourly?,
    ): List<HourlyWrapper> {
        if (publishDate == null || hourlyForecast?.weather?.value.isNullOrEmpty()) return emptyList()

        val hourlyListPubTime = hourlyForecast.temperature?.pubTime ?: publishDate

        val hourlyList: MutableList<HourlyWrapper> = ArrayList(hourlyForecast.weather.value.size)
        hourlyForecast.weather.value.forEachIndexed { index, weather ->
            val calendar = hourlyListPubTime.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.HOUR_OF_DAY, index) // FIXME: Wrong TimeZone for the first item
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val date = calendar.time
            hourlyList.add(
                HourlyWrapper(
                    date = date,
                    weatherText = getWeatherText(weather.toString()),
                    weatherCode = getWeatherCode(weather.toString()),
                    temperature = TemperatureWrapper(
                        temperature = hourlyForecast.temperature?.value?.getOrNull(index)?.toDouble()?.celsius
                    ),
                    wind = if (hourlyForecast.wind != null) {
                        Wind(
                            degree = hourlyForecast.wind.value?.getOrNull(index)?.direction?.toDoubleOrNull(),
                            speed = hourlyForecast.wind.value?.getOrNull(index)?.speed?.toDoubleOrNull()
                                ?.kilometersPerHour
                        )
                    } else {
                        null
                    }
                )
            )
        }
        return hourlyList
    }

    private fun getMinutelyList(
        location: Location,
        minutelyResult: ChinaMinutelyResult,
    ): List<Minutely> {
        if (minutelyResult.precipitation == null || minutelyResult.precipitation.value.isNullOrEmpty()) {
            return emptyList()
        }

        val current = minutelyResult.precipitation.pubTime ?: return emptyList()
        val minutelyList: MutableList<Minutely> = ArrayList(minutelyResult.precipitation.value.size)

        minutelyResult.precipitation.value.forEachIndexed { minute, precipitation ->
            val calendar = current.toCalendarWithTimeZone(location.timeZone).apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, minute)
            }
            minutelyList.add(
                Minutely(
                    date = calendar.time,
                    minuteInterval = 1,
                    precipitationIntensity = precipitation.times(60) // mm/min -> mm/h
                        .millimeters
                )
            )
        }
        return minutelyList
    }

    private fun getAlertList(result: ChinaForecastResult): List<Alert> {
        if (result.alerts.isNullOrEmpty()) return emptyList()

        return result.alerts.map { alert ->
            Alert(
                // Create unique ID from: title, level, start time
                alertId = Objects.hash(alert.title, alert.level, alert.pubTime?.time ?: System.currentTimeMillis())
                    .toString(),
                startDate = alert.pubTime,
                headline = alert.title,
                description = alert.detail,
                severity = getAlertPriority(alert.level),
                color = getAlertColor(alert.level) ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
            )
        }
    }

    private fun getWeatherText(icon: String?): String {
        return if (icon.isNullOrEmpty()) {
            "未知"
        } else {
            when (icon) {
                "0", "00" -> "晴"
                "1", "01" -> "多云"
                "2", "02" -> "阴"
                "3", "03" -> "阵雨"
                "4", "04" -> "雷阵雨"
                "5", "05" -> "雷阵雨伴有冰雹"
                "6", "06" -> "雨夹雪"
                "7", "07" -> "小雨"
                "8", "08" -> "中雨"
                "9", "09" -> "大雨"
                "10" -> "暴雨"
                "11" -> "大暴雨"
                "12" -> "特大暴雨"
                "13" -> "阵雪"
                "14" -> "小雪"
                "15" -> "中雪"
                "16" -> "大雪"
                "17" -> "暴雪"
                "18" -> "雾"
                "19" -> "冻雨"
                "20" -> "沙尘暴"
                "21" -> "小到中雨"
                "22" -> "中到大雨"
                "23" -> "大到暴雨"
                "24" -> "暴雨到大暴雨"
                "25" -> "大暴雨到特大暴雨"
                "26" -> "小到中雪"
                "27" -> "中到大雪"
                "28" -> "大到暴雪"
                "29" -> "浮尘"
                "30" -> "扬沙"
                "31" -> "强沙尘暴"
                "53", "54", "55", "56" -> "霾"
                else -> "未知"
            }
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return if (icon.isNullOrEmpty()) {
            null
        } else {
            when (icon) {
                "0", "00" -> WeatherCode.CLEAR
                "1", "01" -> WeatherCode.PARTLY_CLOUDY
                "3", "7", "8", "9", "03", "07", "08", "09", "10", "11", "12", "21", "22", "23", "24", "25" ->
                    WeatherCode.RAIN
                "4", "04" -> WeatherCode.THUNDERSTORM
                "5", "05" -> WeatherCode.HAIL
                "6", "06", "19" -> WeatherCode.SLEET
                "13", "14", "15", "16", "17", "26", "27", "28" -> WeatherCode.SNOW
                "18", "32", "49", "57" -> WeatherCode.FOG
                "20", "29", "30" -> WeatherCode.WIND
                "53", "54", "55", "56" -> WeatherCode.HAZE
                else -> WeatherCode.CLOUDY
            }
        }
    }

    private fun getAlertPriority(color: String?): AlertSeverity {
        if (color.isNullOrEmpty()) return AlertSeverity.UNKNOWN
        return when (color) {
            "红", "红色" -> AlertSeverity.EXTREME
            "橙", "橙色", "橘", "橘色", "橘黄", "橘黄色" -> AlertSeverity.SEVERE
            "黄", "黄色" -> AlertSeverity.MODERATE
            "蓝", "蓝色" -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
    }

    @ColorInt
    private fun getAlertColor(color: String?): Int? {
        if (color.isNullOrEmpty()) return null
        return when (color) {
            "红", "红色" -> Color.rgb(215, 48, 42)
            "橙", "橙色", "橘", "橘色", "橘黄", "橘黄色" -> Color.rgb(249, 138, 30)
            "黄", "黄色" -> Color.rgb(250, 237, 36)
            "蓝", "蓝色" -> Color.rgb(51, 100, 255)
            else -> null
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocationSearch(
            query,
            context.currentLocale.code
        ).map { results ->
            results
                .filter { it.locationKey?.startsWith("weathercn:") == true && it.status == 0 }
                .map { convertLocation(it) }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocationByGeoPosition(
            latitude,
            longitude,
            context.currentLocale.code
        ).map { results ->
            results
                .filter { it.locationKey?.startsWith("weathercn:") == true && it.status == 0 }
                .map { convertLocation(it) }
        }
    }

    private fun convertLocation(
        result: ChinaLocationResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            latitude = result.latitude!!.toDouble(),
            longitude = result.longitude!!.toDouble(),
            timeZoneId = "Asia/Shanghai",
            countryCode = "CN",
            admin2 = result.affiliation, // TODO: Double check if admin1 or admin2
            city = result.name,
            cityCode = result.locationKey!!.replace("weathercn:", "")
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocationKey = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationKey") { null }

        return currentLocationKey.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocationByGeoPosition(
            location.latitude,
            location.longitude,
            context.currentLocale.code
        ).map {
            if (it.getOrNull(0)?.locationKey?.startsWith("weathercn:") == true && it[0].status == 0) {
                mapOf("locationKey" to it[0].locationKey!!.replace("weathercn:", ""))
            } else {
                throw InvalidLocationException()
            }
        }
    }

    companion object {
        private const val CHINA_WEATHER_BASE_URL = "https://weatherapi.intl.xiaomi.com/wtr-v3/"
        private const val CHINA_APP_KEY = "weather20151024"
        private const val CHINA_SIGN = "zUFJoAR2ZVrDy1vF3D07"
    }
}
