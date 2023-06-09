package wangdaye.com.geometricweather.weather.converters

import android.content.Context
import android.graphics.Color
import us.dustinj.timezonemap.TimeZoneMap
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.*
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult
import wangdaye.com.geometricweather.weather.services.WeatherService.WeatherResultWrapper
import java.util.*
import kotlin.math.roundToInt

fun convert(location: Location?, result: OwmLocationResult): Location {
    val map = TimeZoneMap.forRegion(result.lat, result.lon, result.lat + 0.00001, result.lon + 0.00001)
    return convert(location, result, map)
}

fun convert(resultList: List<OwmLocationResult>?): List<Location> {
    val locationList: MutableList<Location> = ArrayList()
    if (resultList != null && resultList.size != 0) {
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

fun convert(
    location: Location?,
    result: OwmLocationResult,
    map: TimeZoneMap
): Location {
    return if (location != null && !location.province.isNullOrEmpty()
        && location.city.isNotEmpty()
        && !location.district.isNullOrEmpty()
    ) {
        Location(
            cityId = result.lat.toString() + "," + result.lon,
            latitude = result.lat.toFloat(),
            longitude = result.lon.toFloat(),
            timeZone = getTimeZoneForPosition(map, result.lat, result.lon),
            country = result.country,
            city = location.city,
            weatherSource = WeatherSource.OWM,
            isChina = result.country.isNotEmpty()
                    && (result.country.equals("cn", ignoreCase = true)
                    || result.country.equals("hk", ignoreCase = true)
                    || result.country.equals("tw", ignoreCase = true))
        )
    } else {
        Location(
            cityId = result.lat.toString() + "," + result.lon,
            latitude = result.lat.toFloat(),
            longitude = result.lon.toFloat(),
            timeZone = getTimeZoneForPosition(map, result.lat, result.lon),
            country = result.country,
            city = result.name,
            weatherSource = WeatherSource.OWM,
            isChina = result.country.isNotEmpty()
                    && (result.country.equals("cn", ignoreCase = true)
                    || result.country.equals("hk", ignoreCase = true)
                    || result.country.equals("tw", ignoreCase = true))
        )
    }
}

fun convert(
    context: Context,
    location: Location,
    oneCallResult: OwmOneCallResult,
    airPollutionResult: OwmAirPollutionResult?
): WeatherResultWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (oneCallResult.hourly == null || oneCallResult.daily == null) {
        return WeatherResultWrapper(null);
    }

    return try {
        val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<Hourly>>> = HashMap()
        val hourlyList: MutableList<Hourly> = mutableListOf()

        for (result in oneCallResult.hourly) {
            val hourly = Hourly(
                date = Date(result.dt * 1000),
                isDaylight = true,
                weatherText = result.weather[0].main,
                weatherCode = getWeatherCode(result.weather[0].id),
                temperature = Temperature(
                    temperature = toInt(result.temp),
                    apparentTemperature = toInt(result.feelsLike)
                ),
                precipitation = Precipitation(
                    total = getTotalPrecipitation(result.rain?.cumul1h, result.snow?.cumul1h),
                    rain = result.rain?.cumul1h,
                    snow = result.snow?.cumul1h
                ),
                precipitationProbability = PrecipitationProbability(total = result.pop),
                wind = Wind(
                    direction = getWindDirection(context, result.windDeg.toFloat()),
                    degree = WindDegree(result.windDeg.toFloat(), false),
                    speed = result.windSpeed * 3.6f,
                    level = getWindLevel(context, result.windSpeed * 3.6f)
                ),
                airQuality = getAirQuality(result.dt, airPollutionResult),
                uV = UV(
                    index = toInt(result.uvi),
                    level = getUVLevel(context, toInt(result.uvi))
                )
            )

            // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
            val theDayAtMidnight = DisplayUtils.toTimezoneNoHour(
                Date((result.dt - 6 * 3600) * 1000),
                location.timeZone
            )
            val theDayFormatted = DisplayUtils.getFormattedDate(theDayAtMidnight, location.timeZone, "yyyyMMdd")
            if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
                hourlyByHalfDay[theDayFormatted] = hashMapOf(
                    "day" to ArrayList(),
                    "night" to ArrayList()
                )
            }
            if (result.dt < theDayAtMidnight.time / 1000 + 18 * 3600) {
                // 06:00 to 17:59 is the day
                hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
            } else {
                // 18:00 to 05:59 is the night
                hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
            }

            // Add to the app only if starts in the current hour
            if (result.dt >= System.currentTimeMillis() / 1000 - 3600) {
                hourlyList.add(hourly)
            }
        }

        val dailyList = getDailyList(context, location.timeZone, oneCallResult.daily, hourlyList, hourlyByHalfDay)
        val weather = Weather(
            base = Base(cityId = location.cityId),
            current = Current(
                weatherText = oneCallResult.current.weather[0].description,
                weatherCode = getWeatherCode(oneCallResult.current.weather[0].id),
                temperature = Temperature(
                    temperature = toInt(oneCallResult.current.temp),
                    apparentTemperature = toInt(oneCallResult.current.feelsLike)
                ),
                wind = Wind(
                    direction = getWindDirection(context, oneCallResult.current.windDeg.toFloat()),
                    degree = WindDegree(oneCallResult.current.windDeg.toFloat(), false),
                    speed = oneCallResult.current.windSpeed * 3.6f,
                    level = getWindLevel(context, oneCallResult.current.windSpeed * 3.6f)
                ),
                uV = UV(
                    index = toInt(oneCallResult.current.uvi),
                    level = getUVLevel(context, toInt(oneCallResult.current.uvi))
                ),
                airQuality = hourlyList.getOrNull(1)?.airQuality,
                relativeHumidity = oneCallResult.current.humidity.toFloat(),
                pressure = oneCallResult.current.pressure.toFloat(),
                visibility = (oneCallResult.current.visibility / 1000).toFloat(),
                dewPoint = toInt(oneCallResult.current.dewPoint),
                cloudCover = oneCallResult.current.clouds
            ),
            dailyForecast = dailyList,
            hourlyForecast = hourlyList,
            minutelyForecast = getMinutelyList(
                oneCallResult.current.sunrise,
                oneCallResult.current.sunset,
                oneCallResult.minutely
            ),
            alertList = getAlertList(oneCallResult.alerts)
        )
        WeatherResultWrapper(weather)
    } catch (ignored: Exception) {
        WeatherResultWrapper(null)
    }
}

private fun getDailyList(
    context: Context, timeZone: TimeZone,
    dailyResult: List<OwmOneCallResult.Daily>,
    hourlyList: List<Hourly>,
    hourlyListByHalfDay: Map<String, Map<String, MutableList<Hourly>>>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.size)
    val hourlyListByDay = hourlyList.groupBy { DisplayUtils.getFormattedDate(it.date, timeZone, "yyyyMMdd") }
    for (dailyForecast in dailyResult) {
        val theDay = Date(dailyForecast.dt * 1000)
        val dailyDateFormatted = DisplayUtils.getFormattedDate(theDay, timeZone, "yyyyMMdd")
        dailyList.add(
            Daily(
                date = theDay,
                day = completeHalfDayFromHourlyList(
                    dailyDate = theDay,
                    initialHalfDay = HalfDay(
                        weatherText = dailyForecast.weather[0].description,
                        weatherPhase = dailyForecast.weather[0].description,
                        weatherCode = getWeatherCode(dailyForecast.weather[0].id),
                        temperature = Temperature(temperature = toInt(dailyForecast.temp.day), apparentTemperature = toInt(dailyForecast.feelsLike.day))
                        // TODO cloudCover with hourly data
                    ),
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(dailyDateFormatted, null)?.get("day"),
                    isDay = true
                ),
                night = completeHalfDayFromHourlyList(
                    dailyDate = theDay,
                    initialHalfDay = HalfDay(
                        weatherText = dailyForecast.weather[0].description,
                        weatherPhase = dailyForecast.weather[0].description,
                        weatherCode = getWeatherCode(dailyForecast.weather[0].id),
                        temperature = Temperature(temperature = toInt(dailyForecast.temp.night), apparentTemperature = toInt(dailyForecast.feelsLike.night))
                        // TODO cloudCover with hourly data
                    ),
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(dailyDateFormatted, null)?.get("night"),
                    isDay = false
                ),
                sun = Astro(
                    riseDate = Date(dailyForecast.sunrise * 1000),
                    setDate = Date(dailyForecast.sunset * 1000)
                ),
                moon = Astro(
                    riseDate = Date(dailyForecast.moonrise * 1000),
                    setDate = Date(dailyForecast.moonset * 1000)
                ),
                airQuality = getAirQualityFromHourlyList(hourlyListByDay.getOrDefault(dailyDateFormatted, null)),
                uV = UV(toInt(dailyForecast.uvi), getUVLevel(context, toInt(dailyForecast.uvi)), null),
                hoursOfSun = getHoursOfDay(Date(dailyForecast.sunrise * 1000), Date(dailyForecast.sunset * 1000))
            )
        )
    }
    return dailyList
}

// Function that checks for null before sum up
private fun getTotalPrecipitation(rain: Float?, snow: Float?): Float? {
    if (rain == null) {
        return snow
    }
    return if (snow == null) {
        rain
    } else rain + snow
}

// TODO
private fun getMinutelyList(
    sunrise: Long, sunset: Long,
    minuteResult: List<OwmOneCallResult.Minutely>?
): List<Minutely> {
    //if (minuteResult == null) {
    return ArrayList()
    /*}
    List<Minutely> minutelyList = new ArrayList<>(minuteResult.size());
    for (OwmOneCallResult.Minutely interval : minuteResult) {
        minutelyList.add(
                new Minutely(
                        interval.StartDateTime,
                        interval.StartEpochDateTime,
                        CommonConverter.isDaylight(new Date(sunrise * 1000), new Date(sunset * 1000), interval.StartDateTime),
                        interval.ShortPhrase,
                        getWeatherCode(interval.IconCode),
                        interval.Minute,
                        toInt(interval.Dbz),
                        interval.CloudCover
                )
        );
    }
    return minutelyList;*/
}

private fun getAirQuality(requestedTime: Long, ownAirPollutionResult: OwmAirPollutionResult?): AirQuality? {
    if (ownAirPollutionResult == null) return null

    val matchingAirQualityForecast = ownAirPollutionResult.list.filter { it.dt == requestedTime }.firstOrNull()
    if (matchingAirQualityForecast == null) return null

    val pm25: Float? = matchingAirQualityForecast.components?.pm2_5?.toFloat()
    val pm10: Float? = matchingAirQualityForecast.components?.pm10?.toFloat()
    val so2: Float? = matchingAirQualityForecast.components?.so2?.toFloat()
    val no2: Float? = matchingAirQualityForecast.components?.no2?.toFloat()
    val o3: Float? = matchingAirQualityForecast.components?.o3?.toFloat()
    val co: Float? = matchingAirQualityForecast.components?.co?.toFloat()?.div(1000)

    // Return null instead of an object initialized with null values to ease the filtering later when aggregating for daily
    return if (pm25 != null || pm10 != null || so2 != null || no2 != null || o3 != null || co != null) AirQuality(
        pM25 = pm25,
        pM10 = pm10,
        sO2 = so2,
        nO2 = no2,
        o3 = o3,
        cO = co
    ) else null
}

private fun getAlertList(resultList: List<OwmOneCallResult.Alert>?): List<Alert> {
    var i = 0
    return if (resultList != null) {
        val alertList: MutableList<Alert> = ArrayList(resultList.size)
        for (result in resultList) {
            alertList.add(
                Alert(
                    i.toLong(),  // Does not exist
                    Date(result.start * 1000),
                    result.start * 1000,
                    result.event,
                    result.description,
                    result.event,
                    1,  // Does not exist
                    Color.rgb(255, 184, 43) // Defaulting to orange as we don't know
                )
            )
            ++i
        }
        Alert.deduplication(alertList)
        Alert.descByTime(alertList)
        alertList
    } else {
        ArrayList<Alert>()
    }
}

private fun toInt(value: Double): Int {
    return Math.round(value).toInt()
}

private fun getWeatherCode(icon: Int): WeatherCode {
    return when (icon) {
        200, 201, 202 -> WeatherCode.THUNDERSTORM
        210, 211, 212 -> WeatherCode.THUNDER
        221, 230, 231, 232 -> WeatherCode.THUNDERSTORM
        300, 301, 302, 310, 311, 312, 313, 314, 321 -> WeatherCode.RAIN
        500, 501, 502, 503, 504 -> WeatherCode.RAIN
        511 -> WeatherCode.SLEET
        600, 601, 602 -> WeatherCode.SNOW
        611, 612, 613, 614, 615, 616 -> WeatherCode.SLEET
        620, 621, 622 -> WeatherCode.SNOW
        701, 711, 721, 731 -> WeatherCode.HAZE
        741 -> WeatherCode.FOG
        751, 761, 762 -> WeatherCode.HAZE
        771, 781 -> WeatherCode.WIND
        800 -> WeatherCode.CLEAR
        801, 802 -> WeatherCode.PARTLY_CLOUDY
        803, 804 -> WeatherCode.CLOUDY
        else -> WeatherCode.CLOUDY
    }
}