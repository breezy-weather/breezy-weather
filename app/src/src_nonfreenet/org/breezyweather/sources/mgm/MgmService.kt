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

package org.breezyweather.sources.mgm

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.mgm.json.MgmAlertResult
import org.breezyweather.sources.mgm.json.MgmCurrentResult
import org.breezyweather.sources.mgm.json.MgmDailyForecastResult
import org.breezyweather.sources.mgm.json.MgmHourlyForecast
import org.breezyweather.sources.mgm.json.MgmHourlyForecastResult
import org.breezyweather.sources.mgm.json.MgmLocationResult
import org.breezyweather.sources.mgm.json.MgmNormalsResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class MgmService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "mgm"
    override val name = "MGM (${context.currentLocale.getCountryName("TR")})"
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = "https://www.mgm.gov.tr/site/gizlilik-politikasi.aspx"

    private val mApi by lazy {
        client
            .baseUrl(MGM_BASE_URL)
            .build()
            .create(MgmApi::class.java)
    }

    private val weatherAttribution = "Meteoroloji Genel Müdürlüğü"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.mgm.gov.tr/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("TR", ignoreCase = true)
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
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val hourlyStation = location.parameters.getOrElse(id) { null }?.getOrElse("hourlyStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }
        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        if (currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"), Locale.ENGLISH)

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(currentStation).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(dailyStation).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        // Some rural locations in Türkiye are not assigned to an hourlyStation
        val hourly = if (SourceFeature.FORECAST in requestedFeatures && !hourlyStation.isNullOrEmpty()) {
            mApi.getHourly(hourlyStation).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val todayAlerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert("today").onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val tomorrowAlerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert("tomorrow").onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        // can pull multiple days but seems to be an overkill
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                station = dailyStation,
                month = now.get(Calendar.MONTH) + 1,
                day = now.get(Calendar.DATE)
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, todayAlerts, tomorrowAlerts, normals) {
                currentResult: List<MgmCurrentResult>,
                dailyForecastResult: List<MgmDailyForecastResult>,
                hourlyForecastResult: List<MgmHourlyForecastResult>,
                todayAlertResult: List<MgmAlertResult>,
                tomorrowAlertResult: List<MgmAlertResult>,
                normalsResult: List<MgmNormalsResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyForecastResult.getOrNull(0))
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyForecastResult.getOrNull(0)?.forecast)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult.getOrNull(0))
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(currentStation.toInt(), todayAlertResult, tomorrowAlertResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    normalsResult.getOrNull(0)?.let { normals ->
                        mapOf(
                            Date().getCalendarMonth(location) to Normals(
                                daytimeTemperature = normals.meanMax?.celsius,
                                nighttimeTemperature = normals.meanMin?.celsius
                            )
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
        currentResult: MgmCurrentResult?,
    ): CurrentWrapper {
        return CurrentWrapper(
            weatherText = getWeatherText(context, currentResult?.condition),
            weatherCode = getWeatherCode(currentResult?.condition),
            temperature = TemperatureWrapper(
                temperature = getValid(currentResult?.temperature)?.celsius
            ),
            wind = Wind(
                degree = getValid(currentResult?.windDirection),
                speed = getValid(currentResult?.windSpeed)?.kilometersPerHour
            ),
            relativeHumidity = getValid(currentResult?.humidity)?.percent,
            pressure = getValid(currentResult?.pressure)?.hectopascals
        )
    }

    private fun getDailyForecast(
        context: Context,
        dailyForecast: MgmDailyForecastResult?,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        dailyForecast?.let {
            dailyList.add(
                getDaily(
                    context,
                    it.dateDay1,
                    it.conditionDay1,
                    it.maxTempDay1,
                    it.minTempDay2, // Temperature of the night forward
                    it.windDirectionDay1,
                    it.windSpeedDay1
                )
            )
            dailyList.add(
                getDaily(
                    context,
                    it.dateDay2,
                    it.conditionDay2,
                    it.maxTempDay2,
                    it.minTempDay3,
                    it.windDirectionDay2,
                    it.windSpeedDay2
                )
            )
            dailyList.add(
                getDaily(
                    context,
                    it.dateDay3,
                    it.conditionDay3,
                    it.maxTempDay3,
                    it.minTempDay4,
                    it.windDirectionDay3,
                    it.windSpeedDay3
                )
            )
            dailyList.add(
                getDaily(
                    context,
                    it.dateDay4,
                    it.conditionDay4,
                    it.maxTempDay4,
                    it.minTempDay5,
                    it.windDirectionDay4,
                    it.windSpeedDay4
                )
            )
            dailyList.add(
                getDaily(
                    context,
                    it.dateDay5,
                    it.conditionDay5,
                    it.maxTempDay5,
                    null,
                    it.windDirectionDay5,
                    it.windSpeedDay5
                )
            )
        }
        return dailyList
    }

    private fun getHourlyForecast(
        context: Context,
        hourlyForecast: List<MgmHourlyForecast>?,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        // The 'Z' in the timestamp is misused. It is actually in Europe/Istanbul rather than Etc/UTC
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        hourlyForecast?.forEach {
            hourlyList.add(
                HourlyWrapper(
                    date = formatter.parse(it.time)!!,
                    weatherText = getWeatherText(context, it.condition),
                    weatherCode = getWeatherCode(it.condition),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.celsius
                    ),
                    wind = Wind(
                        degree = it.windDirection,
                        speed = it.windSpeed?.kilometersPerHour,
                        gusts = it.gust?.kilometersPerHour
                    ),
                    relativeHumidity = it.humidity?.percent
                )
            )
        }
        return hourlyList
    }

    private fun getAlertList(
        townCode: Int,
        todayAlertResult: List<MgmAlertResult>?,
        tomorrowAlertResult: List<MgmAlertResult>?,
    ): List<Alert> {
        val alertList = mutableListOf<Alert>()
        val source = "Meteoroloji Genel Müdürlüğü"
        listOf(todayAlertResult, tomorrowAlertResult).forEach { alerts ->
            alerts?.forEach {
                if (it.towns?.red?.contains(townCode)!!) {
                    alertList.add(
                        Alert(
                            alertId = it.id,
                            startDate = it.begin,
                            endDate = it.end,
                            headline = getAlertHeadline(it.weather?.red),
                            description = it.text?.red,
                            source = source,
                            severity = AlertSeverity.EXTREME,
                            color = getAlertColor(AlertSeverity.EXTREME)
                        )
                    )
                } else if (it.towns.orange?.contains(townCode)!!) {
                    alertList.add(
                        Alert(
                            alertId = it.id,
                            startDate = it.begin,
                            endDate = it.end,
                            headline = getAlertHeadline(it.weather?.orange),
                            description = it.text?.orange,
                            source = source,
                            severity = AlertSeverity.SEVERE,
                            color = getAlertColor(AlertSeverity.SEVERE)
                        )
                    )
                } else if (it.towns.yellow?.contains(townCode)!!) {
                    alertList.add(
                        Alert(
                            alertId = it.id,
                            startDate = it.begin,
                            endDate = it.end,
                            headline = getAlertHeadline(it.weather?.yellow),
                            description = it.text?.yellow,
                            source = source,
                            severity = AlertSeverity.MODERATE,
                            color = getAlertColor(AlertSeverity.MODERATE)
                        )
                    )
                }
            }
        }
        return alertList
    }

    private fun getDaily(
        context: Context,
        date: String,
        condition: String?,
        maxTemp: Double?,
        nextDayMinTemp: Double?,
        windDirection: Double?,
        windSpeed: Double?,
    ): DailyWrapper {
        // The 'Z' in the timestamp is misused. It is actually in Europe/Istanbul rather than Etc/UTC
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        return DailyWrapper(
            date = formatter.parse(date)!!,
            day = HalfDayWrapper(
                weatherText = getWeatherText(context, condition),
                weatherCode = getWeatherCode(condition),
                temperature = maxTemp?.let { TemperatureWrapper(temperature = it.celsius) },
                wind = Wind(
                    degree = windDirection,
                    speed = windSpeed?.kilometersPerHour
                )
            ),
            night = HalfDayWrapper(
                weatherText = getWeatherText(context, condition),
                weatherCode = getWeatherCode(condition),
                temperature = nextDayMinTemp?.let { TemperatureWrapper(temperature = it.celsius) },
                wind = Wind(
                    degree = windDirection,
                    speed = windSpeed?.kilometersPerHour
                )
            )
        )
    }

    // Source: https://www.mgm.gov.tr/Scripts/ziko16_js/angularService/ililceler.js?v=4
    // under function convertHadise
    private fun getWeatherText(
        context: Context,
        condition: String?,
    ): String? {
        return when (condition) {
            "A" -> context.getString(R.string.common_weather_text_clear_sky) // Açık
            "AB" -> context.getString(R.string.common_weather_text_mostly_clear) // Az Bulutlu
            "PB" -> context.getString(R.string.common_weather_text_partly_cloudy) // Parçalı Bulutlu
            "CB" -> context.getString(R.string.common_weather_text_cloudy) // Çok Bulutlu
            "HY" -> context.getString(R.string.common_weather_text_rain_light) // Hafif Yağmurlu
            "Y" -> context.getString(R.string.common_weather_text_rain) // Yağmurlu
            "KY" -> context.getString(R.string.common_weather_text_rain_heavy) // Kuvvetli Yağmurlu
            "KKY" -> context.getString(R.string.common_weather_text_rain_snow_mixed) // Karla Karışık Yağmurlu
            "HKY" -> context.getString(R.string.common_weather_text_snow_light) // Hafif Kar Yağışlı
            "K" -> context.getString(R.string.common_weather_text_snow) // Kar Yağışlı
            "KYK", "YKY" -> context.getString(R.string.common_weather_text_snow_heavy) // Yoğun Kar Yağışlı
            "HSY" -> context.getString(R.string.common_weather_text_rain_showers_light) // Hafif Sağanak Yağışlı
            "SY" -> context.getString(R.string.common_weather_text_rain_showers) // Sağanak Yağışlı
            "KSY" -> context.getString(R.string.common_weather_text_rain_showers_heavy) // Kuvvetli Sağanak Yağışlı
            "MSY" -> context.getString(R.string.common_weather_text_rain_showers) // Mevzi Sağanak Yağışlı
            "DY" -> context.getString(R.string.weather_kind_hail) // Dolu
            "GSY" -> context.getString(R.string.weather_kind_thunderstorm) // Gökgürültülü Sağanak Yağışlı
            "KGY" -> context.getString(R.string.weather_kind_thunderstorm) // Kuvvetli Gökgürültülü Sağanak Yağışlı
            "SIS" -> context.getString(R.string.common_weather_text_fog) // Sisli
            "PUS" -> context.getString(R.string.common_weather_text_mist) // Puslu
            "DNM" -> context.getString(R.string.common_weather_text_smoke) // Dumanlı
            "KF" -> context.getString(R.string.common_weather_text_sand_storm) // Toz veya Kum Fırtınası
            "R" -> context.getString(R.string.weather_kind_wind) // Rüzgarlı
            "GKR" -> context.getString(R.string.weather_kind_wind) // Güneyli Kuvvetli Rüzgar
            "KKR" -> context.getString(R.string.weather_kind_wind) // Kuzeyli Kuvvetli Rüzgar
            "SCK" -> context.getString(R.string.common_weather_text_hot) // Sıcak
            "SGK" -> context.getString(R.string.common_weather_text_cold) // Soğuk
            "HHY" -> context.getString(R.string.common_weather_text_rain) // Yağışlı
            else -> null
        }
    }

    private fun getWeatherCode(
        condition: String?,
    ): WeatherCode? {
        return when (condition) {
            "A" -> WeatherCode.CLEAR // Açık
            "AB" -> WeatherCode.PARTLY_CLOUDY // Az Bulutlu
            "PB" -> WeatherCode.PARTLY_CLOUDY // Parçalı Bulutlu
            "CB" -> WeatherCode.CLOUDY // Çok Bulutlu
            "HY" -> WeatherCode.RAIN // Hafif Yağmurlu
            "Y" -> WeatherCode.RAIN // Yağmurlu
            "KY" -> WeatherCode.RAIN // Kuvvetli Yağmurlu
            "KKY" -> WeatherCode.SLEET // Karla Karışık Yağmurlu
            "HKY" -> WeatherCode.SNOW // Hafif Kar Yağışlı
            "K" -> WeatherCode.SNOW // Kar Yağışlı
            "KYK", "YKY" -> WeatherCode.SNOW // Yoğun Kar Yağışlı
            "HSY" -> WeatherCode.RAIN // Hafif Sağanak Yağışlı
            "SY" -> WeatherCode.RAIN // Sağanak Yağışlı
            "KSY" -> WeatherCode.RAIN // Kuvvetli Sağanak Yağışlı
            "MSY" -> WeatherCode.RAIN // Mevzi Sağanak Yağışlı
            "DY" -> WeatherCode.HAIL // Dolu
            "GSY" -> WeatherCode.THUNDERSTORM // Gökgürültülü Sağanak Yağışlı
            "KGY" -> WeatherCode.THUNDERSTORM // Kuvvetli Gökgürültülü Sağanak Yağışlı
            "SIS" -> WeatherCode.FOG // Sisli
            "PUS" -> WeatherCode.FOG // Puslu
            "DNM" -> WeatherCode.HAZE // Dumanlı
            "KF" -> WeatherCode.WIND // Toz veya Kum Fırtınası
            "R" -> WeatherCode.WIND // Rüzgarlı
            "GKR" -> WeatherCode.WIND // Güneyli Kuvvetli Rüzgar
            "KKR" -> WeatherCode.WIND // Kuzeyli Kuvvetli Rüzgar
            "SCK" -> null // Sıcak
            "SGK" -> null // Soğuk
            "HHY" -> WeatherCode.RAIN // Yağışlı
            else -> null
        }
    }

    // Simply join names of the active alert types as alert headline.
    // Turkish terminology from: https://www.mgm.gov.tr/meteouyari/turkiye.aspx?Gun=1
    // under "harita-alti-hadise"
    private fun getAlertHeadline(
        weather: List<String>?,
    ): String {
        val items = mutableListOf<String>()
        weather?.forEach {
            when (it) {
                "cold" -> items.add("Soğuk")
                "hot" -> items.add("Sıcak")
                "fog" -> items.add("Sis")
                "agricultural" -> items.add("Zirai Don")
                "ice" -> items.add("Buzlanma ve Don")
                "dust" -> items.add("Toz Taşınımı")
                "snowmelt" -> items.add("Kar Erimesi")
                "avalanche" -> items.add("Çığ")
                "snow" -> items.add("Kar")
                "thunderstorm" -> items.add("Gökgürültülü Sağanak Yağış")
                "wind" -> items.add("Rüzgar")
                "rain" -> items.add("Yağmur")
            }
        }
        return items.joinToString(", ")
    }

    // Color source: https://www.mgm.gov.tr/meteouyari/meteouyari.css
    private fun getAlertColor(
        severity: AlertSeverity,
    ): Int {
        return when (severity) {
            AlertSeverity.EXTREME -> Color.rgb(249, 75, 101) // #btnKirmizi
            AlertSeverity.SEVERE -> Color.rgb(255, 199, 87) // #btnTuruncu
            AlertSeverity.MODERATE -> Color.rgb(255, 244, 49) // #btnSari
            else -> Alert.colorFromSeverity(severity)
        }
    }

    private fun getValid(
        value: Double?,
    ): Double? {
        return if (value == -9999.0) null else value
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocation(
            lat = latitude,
            lon = longitude
        ).map {
            listOf(convertLocation(it))
        }
    }

    private fun convertLocation(
        result: MgmLocationResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = "Europe/Istanbul",
            countryCode = "TR",
            admin1 = result.province,
            city = result.province,
            district = result.district
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }

        return currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocation(
            lat = location.latitude,
            lon = location.longitude
        ).map {
            mapOf(
                "currentStation" to it.currentStationId.toString(),
                "hourlyStation" to if (it.hourlyStationId !== null) {
                    it.hourlyStationId.toString()
                } else {
                    ""
                },
                "dailyStation" to it.dailyStationId.toString()
            )
        }
    }

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val MGM_BASE_URL = "https://servis.mgm.gov.tr/"
    }
}
