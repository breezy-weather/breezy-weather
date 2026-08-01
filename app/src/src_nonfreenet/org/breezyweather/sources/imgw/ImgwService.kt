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

package org.breezyweather.sources.imgw

import android.content.Context
import android.text.format.DateUtils
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import jakarta.inject.Inject
import org.breezyweather.R
import org.breezyweather.common.extensions.toCalendar
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.sources.imgw.json.ImgwMeteoStationEntry
import org.breezyweather.sources.imgw.json.forecast.ImgwDailyForecastEntry
import org.breezyweather.sources.imgw.json.forecast.ImgwDayNightForecastEntry
import org.breezyweather.sources.imgw.json.forecast.ImgwForecastData
import org.breezyweather.sources.imgw.json.forecast.ImgwShortTermForecastEntry
import org.breezyweather.sources.imgw.json.forecast.ImgwUnits
import org.breezyweather.unit.precipitation.Precipitation.Companion.centimeters
import org.breezyweather.unit.precipitation.Precipitation.Companion.inches
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.pressure.Pressure.Companion.inchesOfMercury
import org.breezyweather.unit.pressure.Pressure.Companion.kilopascals
import org.breezyweather.unit.pressure.Pressure.Companion.millibars
import org.breezyweather.unit.pressure.Pressure.Companion.millimetersOfMercury
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.speed.Speed.Companion.knots
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.speed.Speed.Companion.milesPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.breezyweather.unit.temperature.Temperature.Companion.fahrenheit
import org.breezyweather.unit.temperature.Temperature.Companion.kelvin
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone
import javax.inject.Named

class ImgwService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : ImgwServiceStub(context) {
    override val privacyPolicyUrl: String
        get() = "https://danepubliczne.imgw.pl/pl/regulations"

    private val mApi by lazy {
        client.baseUrl(IMGW_API_BASE_URL).build().create(ImgwApi::class.java)
    }

    private val mForecastApi by lazy {
        client.baseUrl(IMGW_FORECAST_API_BASE_URL).build().create(ImgwForecastApi::class.java)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        // if token is set, we use the forecast API; if not, we default to the public API for just current data
        return if (token.isNotEmpty()) {
            val gfsForecast = mForecastApi.getGfsForecast(
                token = token,
                latitude = location.latitude.toString(),
                longitude = location.longitude.toString()
            ).blockingFirst().data

            mForecastApi.getForecast(
                token = token,
                latitude = location.latitude.toString(),
                longitude = location.longitude.toString()
            ).map {
                if (it.data != null && gfsForecast != null) {
                    WeatherWrapper(
                        current = if (SourceFeature.CURRENT in requestedFeatures) {
                            getCurrentWeather(context, it.data)
                        } else {
                            null
                        },
                        hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                            getHourlyWeather(context, it.data, gfsForecast)
                        } else {
                            null
                        },
                        minutelyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                            getMinutelyWeather(it.data)
                        } else {
                            null
                        },
                        dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                            getDailyWeather(context, it.data, gfsForecast)
                        } else {
                            null
                        }
                    )
                } else {
                    WeatherWrapper()
                }
            }
        } else {
            mApi.getAllMeteoStationData().map {
                WeatherWrapper(
                    current = mapToCurrent(findClosestStation(location, it))
                )
            }
        }
    }

    private fun getCurrentWeather(context: Context, forecastData: ImgwForecastData): CurrentWrapper {
        val latestData = forecastData.shortTermData.getCurrent()
        val units = forecastData.units

        return CurrentWrapper(
            weatherCode = getWeatherCode(latestData.icon),
            weatherText = getWeatherText(context, latestData.icon),
            temperature = TemperatureWrapper(
                temperature = getTemperature(units, latestData.airTemperature),
                feelsLike = getTemperature(units, latestData.feelsLike)
            ),
            pressure = getPressure(units, latestData.pressureMSL),
            wind = Wind(
                degree = latestData.windDir?.toDouble(),
                speed = getWindSpeed(units, latestData.windSpeed),
                gusts = getWindSpeed(units, latestData.gustsSpeed)
            ),
            relativeHumidity = latestData.humidity?.percent,
            dewPoint = getTemperature(units, latestData.dewpointTemperature),
            cloudCover = latestData.cloud?.percent
        )
    }

    private fun getDailyWeather(
        context: Context,
        hybridForecastData: ImgwForecastData,
        gfsForecastData: ImgwForecastData,
    ): List<DailyWrapper> {
        val units = hybridForecastData.units
        val collectedData = combineDailySources(
            hybridForecastData.dailyData,
            gfsForecastData.dailyData
        ).filter { elem ->
            !hybridForecastData.dayNightData.any { it.date == elem.date }
        }

        return hybridForecastData.dayNightData.groupBy { it.date }.map { dayNight ->
            val daytime = dayNight.value.find { it.isDay }
            val nighttime = dayNight.value.find { !it.isDay }
            val allDay = hybridForecastData.dailyData.find { it.date == dayNight.key }
                ?: gfsForecastData.dailyData.find { it.date == dayNight.key }

            DailyWrapper(
                date = dayNight.key,
                day = HalfDayWrapper(
                    weatherText = getWeatherText(context, daytime?.icon),
                    weatherCode = getWeatherCode(daytime?.icon),
                    temperature = TemperatureWrapper(
                        temperature = getTemperature(units, daytime?.maxTemperature)
                    ),
                    precipitation = Precipitation(
                        total = getPrecipitation(units, daytime?.sumPrecipitation),
                        rain = getPrecipitation(units, daytime?.sumRain),
                        snow = getPrecipitation(units, daytime?.sumSnow)
                    ),
                    wind = Wind(
                        speed = getWindSpeed(units, daytime?.maxWindSpeed)
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(context, nighttime?.icon),
                    weatherCode = getWeatherCode(nighttime?.icon),
                    temperature = TemperatureWrapper(
                        temperature = getTemperature(units, nighttime?.minTemperature)
                    ),
                    precipitation = Precipitation(
                        total = getPrecipitation(units, nighttime?.sumPrecipitation),
                        rain = getPrecipitation(units, nighttime?.sumRain),
                        snow = getPrecipitation(units, nighttime?.sumSnow)
                    ),
                    wind = Wind(
                        speed = getWindSpeed(units, nighttime?.maxWindSpeed)
                    )
                ),
                cloudCover = DailyCloudCover(
                    min = allDay?.minCloudCover?.percent,
                    max = allDay?.maxCloudCover?.percent,
                    average = allDay?.avgCloudCover?.percent
                )
            )
        }.plus(
            collectedData.map {
                DailyWrapper(
                    date = it.date,
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, it.icon),
                        weatherCode = getWeatherCode(it.icon),
                        temperature = TemperatureWrapper(
                            temperature = getTemperature(units, it.maxTemperature)
                        ),
                        precipitation = Precipitation(
                            total = getPrecipitation(units, it.maxPrecipitation)
                        ),
                        wind = Wind(
                            speed = getWindSpeed(units, it.maxWindSpeed)
                        )
                    ),
                    cloudCover = DailyCloudCover(
                        min = it.minCloudCover.percent,
                        max = it.maxCloudCover.percent,
                        average = it.avgCloudCover.percent
                    )
                )
            }
        ).let { dailyWrapperList ->
            if (!dailyWrapperList.any { DateUtils.isToday(it.date.time) }) {
                val current = hybridForecastData.shortTermData.getCurrent()

                listOf(DailyWrapper(
                    date = current.date.toTimezoneSpecificHour(
                        timeZone = TimeZone.getTimeZone("UTC"),
                        hour = 0,
                    ),
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, current.icon),
                        weatherCode = getWeatherCode(current.icon),
                        temperature = TemperatureWrapper(
                            temperature = getTemperature(units, current.airTemperature)
                        ),
                        precipitation = Precipitation(
                            total = getPrecipitation(units, current.precipitation)
                        ),
                        wind = Wind(
                            speed = getWindSpeed(units, current.windSpeed)
                        )
                    ),
                    cloudCover = DailyCloudCover(
                        average = current.cloud?.percent
                    )
                )).plus(dailyWrapperList)
            } else {
                dailyWrapperList
            }
        }
    }

    private fun getHourlyWeather(
        context: Context,
        hybridForecastData: ImgwForecastData,
        gfsForecastData: ImgwForecastData,
    ): List<HourlyWrapper> {
        val collectedData = combineHourlySources(
            hybrid = hybridForecastData.shortTermData.filterHourly(),
            gfs = gfsForecastData.shortTermData.filterHourly()
        )
        val units = hybridForecastData.units

        return collectedData.map {
            HourlyWrapper(
                date = it.date,
                isDaylight = isDaytime(it.icon),
                weatherCode = getWeatherCode(it.icon),
                weatherText = getWeatherText(context, it.icon),
                temperature = TemperatureWrapper(
                    temperature = getTemperature(units, it.airTemperature),
                    feelsLike = getTemperature(units, it.feelsLike)
                ),
                pressure = getPressure(units, it.pressureMSL),
                wind = Wind(
                    degree = it.windDir?.toDouble(),
                    speed = getWindSpeed(units, it.windSpeed),
                    gusts = getWindSpeed(units, it.gustsSpeed)
                ),
                relativeHumidity = it.humidity?.percent,
                dewPoint = getTemperature(units, it.dewpointTemperature),
                cloudCover = it.cloud?.percent,
                precipitation = Precipitation(
                    total = getPrecipitation(units, it.precipitation),
                    rain = getPrecipitation(units, it.rain),
                    snow = getPrecipitation(units, it.snow)
                )
            )
        }
    }

    private fun getMinutelyWeather(forecastData: ImgwForecastData): List<Minutely> =
        forecastData.shortTermData.filterMinutely().map {
            Minutely(
                date = it.date,
                minuteInterval = 10,
                precipitationIntensity = getPrecipitation(forecastData.units, it.precipitation10m)
            )
        }

    private fun List<ImgwShortTermForecastEntry>.filterHourly() = this.filter {
        it.type == "Type_Hour"
    }

    private fun List<ImgwShortTermForecastEntry>.filterMinutely() = this.filter {
        it.precipitation10m != null
    }

    private fun List<ImgwShortTermForecastEntry>.getCurrent() = this.first {
        it.airTemperature != null
    }

    private fun combineHourlySources(
        hybrid: List<ImgwShortTermForecastEntry>,
        gfs: List<ImgwShortTermForecastEntry>,
    ) = hybrid.plus(
        gfs.filter {
            !hybrid.any { elem -> elem.date == it.date }
        }
    )

    private fun combineDailySources(
        hybrid: List<ImgwDailyForecastEntry>,
        gfs: List<ImgwDailyForecastEntry>,
    ) = hybrid.plus(
        gfs.filter {
            !hybrid.any { elem -> elem.date == it.date }
        }
    )

    // IMGW Meteo has icons encoded in "nXzYY<d/n>" format, where X - cloudiness, YY - WMO 4677 event code,
    // d - day, n - night; Breezy Weather does not support most of the events and cloudiness levels
    private fun getWeatherCode(iconCode: String?) = if (iconCode.isNullOrBlank()) {
        null
    } else {
        val cloudiness = iconCode.take(2).takeLast(1)
        val wmoCode = iconCode.takeLast(3).take(2)

        val wmoCodeWeather = when (wmoCode) {
            "00", "04", "76" -> WeatherCode.CLEAR
            "06", "07", "08", "09", "18", "19" -> WeatherCode.WIND
            "05", "10" -> WeatherCode.HAZE
            "45", "49" -> WeatherCode.FOG
            "50", "60", "61", "63", "65", "80", "81" -> WeatherCode.RAIN
            "56", "66", "68", "69", "83", "84" -> WeatherCode.SLEET
            "38", "70", "71", "73", "75", "85", "86" -> WeatherCode.SNOW
            "90", "96", "99" -> WeatherCode.HAIL
            "13", "17" -> WeatherCode.THUNDER
            "95", "97" -> WeatherCode.THUNDERSTORM
            else -> null
        }

        if (wmoCodeWeather == WeatherCode.CLEAR || wmoCodeWeather == null) {
            when (cloudiness) {
                "0" -> WeatherCode.CLEAR
                "1", "2", "3", "4" -> WeatherCode.PARTLY_CLOUDY
                "5", "6", "7", "8" -> WeatherCode.CLOUDY
                else -> null
            }
        } else {
            wmoCodeWeather
        }
    }

    private fun getWeatherText(context: Context, iconCode: String?) = if (iconCode.isNullOrBlank()) {
        null
    } else {
        val cloudiness = iconCode.take(2).takeLast(1)
        val wmoCode = iconCode.takeLast(3).take(2)

        val cloudinessString = when (cloudiness) {
            "0" -> context.getString(R.string.common_weather_text_clear_sky)
            "1", "2" -> context.getString(R.string.common_weather_text_mostly_clear)
            "3", "4" -> context.getString(R.string.common_weather_text_partly_cloudy)
            "5", "6" -> context.getString(R.string.common_weather_text_mostly_cloudy)
            "7" -> context.getString(R.string.common_weather_text_cloudy)
            "8" -> context.getString(R.string.common_weather_text_overcast)
            else -> null
        }

        val weatherConditionString = when (wmoCode) {
            "04" -> context.getString(R.string.common_weather_text_smoke)
            "05" -> context.getString(R.string.imgw_weather_text_haze)
            "10" -> context.getString(R.string.common_weather_text_mist)
            "06", "07" -> context.getString(R.string.common_weather_text_dust)
            "09" -> context.getString(R.string.common_weather_text_dust_storm)
            "13" -> context.getString(R.string.imgw_weather_text_thunder)
            "17" -> context.getString(R.string.imgw_weather_text_thunderstorm_approaching)
            "18" -> context.getString(R.string.common_weather_text_squall)
            "19" -> context.getString(R.string.common_weather_text_tornado)
            "38" -> context.getString(R.string.common_weather_text_blowing_snow)
            "45" -> context.getString(R.string.common_weather_text_fog)
            "49" -> context.getString(R.string.imgw_weather_text_depositing_rime_fog)
            "50" -> context.getString(R.string.common_weather_text_drizzle)
            "56" -> context.getString(R.string.common_weather_text_drizzle_freezing)
            "60" -> context.getString(R.string.common_weather_text_rain_showers_light)
            "61" -> context.getString(R.string.common_weather_text_rain_light)
            "63" -> context.getString(R.string.common_weather_text_rain_moderate)
            "65" -> context.getString(R.string.common_weather_text_rain_heavy)
            "66" -> context.getString(R.string.common_weather_text_rain_freezing_light)
            "68" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "69" -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy)
            "70" -> context.getString(R.string.common_weather_text_snow_showers_light)
            "71" -> context.getString(R.string.common_weather_text_snow_light)
            "73" -> context.getString(R.string.common_weather_text_snow_moderate)
            "75" -> context.getString(R.string.common_weather_text_snow_heavy)
            "76" -> context.getString(R.string.imgw_weather_text_diamond_dust)
            "80" -> context.getString(R.string.common_weather_text_rain_showers)
            "81" -> context.getString(R.string.common_weather_text_rain_showers_heavy)
            "85" -> context.getString(R.string.common_weather_text_snow_showers)
            "86" -> context.getString(R.string.common_weather_text_snow_showers_heavy)
            "90" -> context.getString(R.string.imgw_weather_text_hail)
            "95" -> context.getString(R.string.imgw_weather_text_thunderstorm_slight_or_moderate)
            "96" -> context.getString(R.string.imgw_weather_text_thunderstorm_with_slight_hail)
            "97" -> context.getString(R.string.imgw_weather_text_thunderstorm_heavy)
            "99" -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_heavy_hail)
            else -> null
        }

        if (cloudinessString == null) {
            null
        } else if (weatherConditionString == null) {
            cloudinessString
        } else {
            "$cloudinessString - $weatherConditionString"
        }
    }

    private fun isDaytime(iconCode: String?) = if (iconCode.isNullOrBlank()) {
        null
    } else {
        iconCode.endsWith('d')
    }

    private fun getWindSpeed(units: ImgwUnits, value: Double?) = when (units.windSpeed) {
        "[m/s]" -> value?.metersPerSecond
        "[km/h]" -> value?.kilometersPerHour
        "[mph]" -> value?.milesPerHour
        "[kt]" -> value?.knots
        else -> null
    }

    private fun getTemperature(units: ImgwUnits, value: Double?) = when (units.temperature) {
        "[K]" -> value?.kelvin
        "[C]" -> value?.celsius
        "[F]" -> value?.fahrenheit
        else -> null
    }

    private fun getPressure(units: ImgwUnits, value: Double?) =
        when (units.pressure) {
            "[Pa]" -> value?.pascals
            "[hPa]" -> value?.hectopascals
            "[kPa]" -> value?.kilopascals
            "[mmHg]" -> value?.millimetersOfMercury
            "[inHg]" -> value?.inchesOfMercury
            "[mbar]" -> value?.millibars
            else -> null
        }

    private fun getPrecipitation(units: ImgwUnits, value: Double?) = when (units.precipitation) {
        "[mm]" -> value?.millimeters
        "[cm]" -> value?.centimeters
        "[in]" -> value?.inches
        else -> null
    }

    private fun findClosestStation(location: Location, meteoData: List<ImgwMeteoStationEntry>) = meteoData.filter {
        it.airTemperature != null && it.latitude != null && it.longitude != null
    }.minByOrNull {
        SphericalUtil.computeDistanceBetween(
            LatLng(location.latitude, location.longitude),
            LatLng(it.latitude!!, it.longitude!!)
        )
    }

    private fun mapToCurrent(meteoStation: ImgwMeteoStationEntry?) = CurrentWrapper(
        temperature = TemperatureWrapper(
            temperature = meteoStation?.airTemperature?.celsius
        ),
        wind = Wind(
            speed = meteoStation?.avgWindSpeed?.kilometersPerHour,
            gusts = meteoStation?.windGust?.kilometersPerHour,
            degree = meteoStation?.windDirection
        ),
        relativeHumidity = meteoStation?.relativeHumidity?.percent
    )

    companion object {
        private const val IMGW_API_BASE_URL = "https://danepubliczne.imgw.pl"
        private const val IMGW_FORECAST_API_BASE_URL = "https://meteo.imgw.pl"
    }
}
