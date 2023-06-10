package wangdaye.com.geometricweather.weather.converters

import android.content.Context
import us.dustinj.timezonemap.TimeZoneMap
import wangdaye.com.geometricweather.GeometricWeather.Companion.instance
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.*
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult.Properties.Timeseries
import wangdaye.com.geometricweather.weather.json.metno.MetNoSunsetResult
import wangdaye.com.geometricweather.weather.json.nominatim.NominatimLocationResult
import wangdaye.com.geometricweather.weather.services.WeatherService.WeatherResultWrapper
import java.util.*
import kotlin.math.roundToInt

fun convert(resultList: List<NominatimLocationResult>?): List<Location> {
    val locationList: MutableList<Location> = ArrayList()
    if (!resultList.isNullOrEmpty()) {
        // Since we don't have timezones in the result, we need to initialize a TimeZoneMap
        // Since it takes a lot of time, we make boundaries
        // However, even then, it can take a lot of time, even on good performing smartphones.
        // TODO: To improve performances, create a Location() with a null TimeZone.
        // When clicking in the location search result on a specific location, if TimeZone is
        // null, then make a TimeZoneMap of the lat/lon and find its TimeZone
        val minLat = resultList.minOf { it.lat }
        val maxLat = resultList.maxOf { it.lat } + 0.00001
        val minLon = resultList.minOf { it.lon }
        val maxLon = resultList.maxOf { it.lon } + 0.00001
        val map = TimeZoneMap.forRegion(minLat, minLon, maxLat, maxLon)
        for (r in resultList) {
            locationList.add(convert(null, r, map))
        }
    }
    return locationList
}

fun convert(location: Location?, result: NominatimLocationResult): Location {
    val map = TimeZoneMap.forRegion(result.lat, result.lon,result.lat + 0.00001,result.lon + 0.00001)
    return convert(location, result, map)
}

fun convert(location: Location?, result: NominatimLocationResult, map: TimeZoneMap): Location {
    return if (location != null && !location.province.isNullOrEmpty()
        && location.city.isNotEmpty()
        && !location.district.isNullOrEmpty()
    ) {
        Location(
            cityId = result.place_id.toString(),
            latitude = result.lat.toFloat(),
            longitude = result.lon.toFloat(),
            timeZone = getTimeZoneForPosition(map, result.lat.toDouble(), result.lon.toDouble()),
            country = result.address.country,
            province = location.province,
            city = location.city,
            weatherSource = WeatherSource.METNO,
            isChina = result.address.country_code.isNotEmpty()
                    && (result.address.country_code.equals("cn", ignoreCase = true)
                    || result.address.country_code.equals("hk", ignoreCase = true)
                    || result.address.country_code.equals("tw", ignoreCase = true))
        )
    } else {
        Location(
            cityId = result.place_id.toString(),
            latitude = result.lat.toFloat(),
            longitude = result.lon.toFloat(),
            timeZone = getTimeZoneForPosition(map, result.lat.toDouble(), result.lon.toDouble()),
            country = result.address.country,
            province = if (result.address.state == null) "" else result.address.state,
            city = result.display_name,
            weatherSource = WeatherSource.METNO,
            isChina = result.address.country_code.isNotEmpty()
                    && (result.address.country_code.equals("cn", ignoreCase = true)
                    || result.address.country_code.equals("hk", ignoreCase = true)
                    || result.address.country_code.equals("tw", ignoreCase = true))
        )
    }
}

fun convert(
    context: Context,
    location: Location,
    forecastResult: MetNoLocationForecastResult,
    sunsetResult: MetNoSunsetResult
): WeatherResultWrapper {
    // If the API doesn’t return hourly, consider data as garbage and keep cached data
    if (forecastResult.properties == null
        || forecastResult.properties.timeseries.isNullOrEmpty()) {
        return WeatherResultWrapper(null);
    }

    return try {
        val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<Hourly>>> = HashMap()
        val hourlyList: MutableList<Hourly> = mutableListOf()
        var currentI: Int? = null;

        for (i in forecastResult.properties.timeseries.indices) {
            val hourlyForecast = forecastResult.properties.timeseries[i]
            val hourly = Hourly(
                date = hourlyForecast.time,
                isDaylight = true, // Will be completed later with daily sunrise/set
                weatherText = null, // TODO
                weatherCode = getWeatherCode(hourlyForecast.data.next1Hours?.summary?.symbolCode
                        ?: hourlyForecast.data.next6Hours?.summary?.symbolCode
                        ?: hourlyForecast.data.next12Hours?.summary?.symbolCode),
                temperature = Temperature(
                    temperature = hourlyForecast.data.instant?.details?.airTemperature?.roundToInt(),
                ),
                precipitation = Precipitation(
                    total = hourlyForecast.data.next1Hours?.details?.precipitationAmount
                        ?: hourlyForecast.data.next6Hours?.details?.precipitationAmount
                        ?: hourlyForecast.data.next12Hours?.details?.precipitationAmount
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyForecast.data.next1Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data.next6Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data.next12Hours?.details?.probabilityOfPrecipitation,
                    thunderstorm = hourlyForecast.data.next1Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data.next6Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data.next12Hours?.details?.probabilityOfThunder
                ),
                wind = if (hourlyForecast.data.instant?.details == null) null else Wind(
                    direction = getWindDirection(context, hourlyForecast.data.instant.details.windFromDirection),
                    degree = WindDegree(hourlyForecast.data.instant.details.windFromDirection, false),
                    speed = hourlyForecast.data.instant.details.windSpeed * 3.6f,
                    level = getWindLevel(context, hourlyForecast.data.instant.details.windSpeed * 3.6f)
                ),
                // airQuality = TODO
                uV = UV(
                    index = hourlyForecast.data.instant?.details?.ultravioletIndexClearSky?.roundToInt(),
                    level = getUVLevel(context, hourlyForecast.data.instant?.details?.ultravioletIndexClearSky?.roundToInt())
                )
            )

            // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
            val theDayAtMidnight = DisplayUtils.toTimezoneNoHour(
                Date(hourlyForecast.time.time - (6 * 3600 * 1000)),
                location.timeZone
            )
            val theDayFormatted =
                DisplayUtils.getFormattedDate(theDayAtMidnight, location.timeZone, "yyyy-MM-dd")
            if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
                hourlyByHalfDay[theDayFormatted] = hashMapOf(
                    "day" to ArrayList(),
                    "night" to ArrayList()
                )
            }
            if (hourlyForecast.time.time < theDayAtMidnight.time + 18 * 3600 * 1000) {
                // 06:00 to 17:59 is the day
                hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
            } else {
                // 18:00 to 05:59 is the night
                hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
            }

            // Add to the app only if starts in the current hour
            if (hourlyForecast.time.time >= System.currentTimeMillis() - 3600 * 1000) {
                if (currentI == null) {
                    currentI = i + 1
                }
                hourlyList.add(hourly)
            }
        }

        val dailyList = getDailyList(context, location.timeZone, sunsetResult.location.time, hourlyList, hourlyByHalfDay)
        val weather = Weather(
            base = Base(
                cityId = location.cityId,
                publishDate = forecastResult.properties.meta.updatedAt
            ),
            current = Current(
                weatherText = hourlyList.getOrNull(1)?.weatherText,
                weatherCode = hourlyList.getOrNull(1)?.weatherCode,
                temperature = hourlyList.getOrNull(1)?.temperature,
                wind = hourlyList.getOrNull(1)?.wind,
                uV = getCurrentUV(
                    context,
                    dailyList.getOrNull(0)?.uV?.index,
                    Date(),
                    dailyList.getOrNull(0)?.sun?.riseDate,
                    dailyList.getOrNull(0)?.sun?.setDate,
                    location.timeZone
                ),
                // airQuality = TODO,
                relativeHumidity = if (currentI != null)
                    forecastResult.properties.timeseries.getOrNull(currentI)?.data?.instant?.details?.relativeHumidity
                else null,
                pressure = if (currentI != null)
                    forecastResult.properties.timeseries.getOrNull(currentI)?.data?.instant?.details?.airPressureAtSeaLevel
                else null,
                dewPoint = if (currentI != null)
                    forecastResult.properties.timeseries.getOrNull(currentI)?.data?.instant?.details?.dewPointTemperature?.roundToInt()
                else null
            ),
            dailyForecast = dailyList,
            hourlyForecast = completeHourlyListFromDailyList(context, hourlyList, dailyList, location.timeZone, completeDaylight = true)
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (instance.debugMode) {
            e.printStackTrace()
        }
        WeatherResultWrapper(null)
    }
}

private fun getDailyList(
    context: Context,
    timeZone: TimeZone,
    sunsetTimeResults: List<MetNoSunsetResult.Location.Time>,
    hourlyList: List<Hourly>,
    hourlyListByHalfDay: Map<String, Map<String, MutableList<Hourly>>>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList()
    val hourlyListByDay = hourlyList.groupBy { DisplayUtils.getFormattedDate(it.date, timeZone, "yyyy-MM-dd") }
    for (day in hourlyListByDay.entries) {
        val dayDate = DisplayUtils.toDateNoHour(timeZone, day.key)
        val ephemerisInfo = sunsetTimeResults.firstOrNull { it.date == day.key }

        dailyList.add(
            Daily(
                date = dayDate,
                day = completeHalfDayFromHourlyList(
                    dailyDate = dayDate,
                    initialHalfDay = null,
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(day.key, null)?.get("day"),
                    isDay = true
                ),
                night = completeHalfDayFromHourlyList(
                    dailyDate = dayDate,
                    initialHalfDay = null,
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(day.key, null)?.get("night"),
                    isDay = false
                ),
                sun = Astro(
                    riseDate = ephemerisInfo?.sunrise?.time,
                    setDate = ephemerisInfo?.sunset?.time,
                ),
                moon = Astro(
                    riseDate = ephemerisInfo?.moonrise?.time,
                    setDate = ephemerisInfo?.moonset?.time,
                ),
                moonPhase = MoonPhase(
                    angle = ephemerisInfo?.moonposition?.phase?.roundToInt(),
                    description = ephemerisInfo?.moonposition?.desc
                ),
                //airQuality = TODO,
                uV = getDailyUVFromHourlyList(context, day.value),
                hoursOfSun = getHoursOfDay(ephemerisInfo?.sunrise?.time, ephemerisInfo?.sunset?.time)
            )
        )
    }
    return dailyList
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else when(icon.replace("_night", "").replace("_day", "")) {
        "clearsky", "fair" -> WeatherCode.CLEAR
        "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "fog" -> WeatherCode.FOG
        "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" -> WeatherCode.RAIN
        "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder", "heavysleetshowersandthunder",
        "heavysnowandthunder", "heavysnowshowersandthunder", "lightrainandthunder", "lightrainshowersandthunder",
        "lightsleetandthunder", "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
        "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder", "snowandthunder",
        "snowshowersandthunder" -> WeatherCode.THUNDERSTORM
        "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" -> WeatherCode.SNOW
        "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet",
        "sleetshowers" -> WeatherCode.SLEET
        else -> null
    }
}
