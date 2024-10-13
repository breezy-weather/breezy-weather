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

import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.sources.mf.json.MfCurrentResult
import org.breezyweather.sources.mf.json.MfEphemeris
import org.breezyweather.sources.mf.json.MfEphemerisResult
import org.breezyweather.sources.mf.json.MfForecastDaily
import org.breezyweather.sources.mf.json.MfForecastHourly
import org.breezyweather.sources.mf.json.MfForecastProbability
import org.breezyweather.sources.mf.json.MfForecastResult
import org.breezyweather.sources.mf.json.MfNormalsResult
import org.breezyweather.sources.mf.json.MfRainResult
import org.breezyweather.sources.mf.json.MfWarningsResult
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun convert(location: Location, result: MfForecastResult): Location {
    return if (result.properties == null) {
        location
    } else {
        location.copy(
            timeZone = result.properties.timezone,
            country = result.properties.country,
            countryCode = result.properties.country.substring(0, 2),
            admin2 = if (!result.properties.frenchDepartment.isNullOrEmpty()) {
                getFrenchDepartmentName(result.properties.frenchDepartment)
            } else null, // Département
            admin2Code = result.properties.frenchDepartment, // Département
            city = result.properties.name
        )
    }
}

fun convert(
    location: Location,
    currentResult: MfCurrentResult,
    forecastResult: MfForecastResult,
    ephemerisResult: MfEphemerisResult,
    rainResult: MfRainResult?,
    warningsResult: MfWarningsResult,
    normalsResult: MfNormalsResult
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.properties == null ||
        forecastResult.properties.forecast.isNullOrEmpty() ||
        forecastResult.properties.dailyForecast.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }
    return WeatherWrapper(
        /*base = Base(
            publishDate = forecastResult.updateTime ?: Date()
        ),*/
        current = Current(
            weatherText = currentResult.properties?.gridded?.weatherDescription,
            weatherCode = getWeatherCode(currentResult.properties?.gridded?.weatherIcon),
            temperature = Temperature(
                temperature = currentResult.properties?.gridded?.temperature
            ),
            wind = if (currentResult.properties?.gridded != null) Wind(
                degree = currentResult.properties.gridded.windDirection?.toDouble(),
                speed = currentResult.properties.gridded.windSpeed
            ) else null
        ),
        normals = getNormals(location, normalsResult),
        dailyForecast = getDailyList(
            location,
            forecastResult.properties.dailyForecast,
            ephemerisResult.properties?.ephemeris
        ),
        hourlyForecast = getHourlyList(
            forecastResult.properties.forecast,
            forecastResult.properties.probabilityForecast
        ),
        minutelyForecast = getMinutelyList(rainResult),
        alertList = getWarningsList(warningsResult)
    )
}

private fun getDailyList(
    location: Location,
    dailyForecasts: List<MfForecastDaily>,
    ephemerisResult: MfEphemeris?
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyForecasts.size)
    for (i in 0 until dailyForecasts.size - 1) {
        val dailyForecast = dailyForecasts[i]
        // Given as UTC, we need to convert in the correct timezone at 00:00
        val dayInUTCCalendar = dailyForecast.time.toCalendarWithTimeZone(TimeZone.getTimeZone("UTC"))
        val dayInLocalCalendar = Calendar.getInstance(location.javaTimeZone).apply {
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
            Daily(
                date = theDayInLocal,
                day = HalfDay(
                    // Too complicated to get weather from hourly, so let's just use daily info for both day and night
                    weatherText = dailyForecast.dailyWeatherDescription,
                    weatherPhase = dailyForecast.dailyWeatherDescription,
                    weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                    temperature = Temperature(temperature = dailyForecast.tMax)
                ),
                night = HalfDay(
                    weatherText = dailyForecast.dailyWeatherDescription,
                    weatherPhase = dailyForecast.dailyWeatherDescription,
                    weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                    // tMin is for current day, so it actually takes the previous night,
                    // so we try to get tMin from next day if available
                    temperature = Temperature(temperature = dailyForecasts.getOrNull(i + 1)?.tMin)
                ),
                sun = Astro(
                    riseDate = dailyForecast.sunriseTime,
                    setDate = dailyForecast.sunsetTime
                ),
                moon = if (i == 0) Astro(
                    riseDate = ephemerisResult?.moonriseTime,
                    setDate = ephemerisResult?.moonsetTime
                ) else null,
                moonPhase = if (i == 0) MoonPhase(
                    angle = MoonPhase.getAngleFromEnglishDescription(ephemerisResult?.moonPhaseDescription)
                ) else null,
                uV = UV(index = dailyForecast.uvIndex?.toDouble())
            )
        )
    }
    return dailyList
}

private fun getHourlyList(
    hourlyForecastList: List<MfForecastHourly>,
    probabilityForecastResult: List<MfForecastProbability>?
): List<HourlyWrapper> {
    return hourlyForecastList.map { hourlyForecast ->
        HourlyWrapper(
            date = hourlyForecast.time,
            weatherText = hourlyForecast.weatherDescription,
            weatherCode = getWeatherCode(hourlyForecast.weatherIcon),
            temperature = Temperature(
                temperature = hourlyForecast.t,
                windChillTemperature = hourlyForecast.tWindchill
            ),
            precipitation = getHourlyPrecipitation(hourlyForecast),
            precipitationProbability = getHourlyPrecipitationProbability(
                probabilityForecastResult,
                hourlyForecast.time
            ),
            wind = Wind(
                degree = hourlyForecast.windDirection?.toDouble(),
                speed = hourlyForecast.windSpeed?.toDouble(),
                gusts = hourlyForecast.windSpeedGust?.toDouble() // Seems to be always 0? Or not available in low wind speeds maybe
            ),
            relativeHumidity = hourlyForecast.relativeHumidity?.toDouble(),
            pressure = hourlyForecast.pSea,
            cloudCover = hourlyForecast.totalCloudCover
        )
    }
}

private fun getHourlyPrecipitation(hourlyForecast: MfForecastHourly): Precipitation {
    val rainCumul = with (hourlyForecast) {
        rain1h ?: rain3h ?: rain6h ?: rain12h ?: rain24h
    }
    val snowCumul = with (hourlyForecast) {
        snow1h ?: snow3h ?: snow6h ?: snow12h ?: snow24h
    }
    return Precipitation(
        total = rainCumul + snowCumul,
        rain = rainCumul,
        snow = snowCumul
    )
}

/**
 * TODO: Needs to be reviewed
 */
private fun getHourlyPrecipitationProbability(
    probabilityForecastResult: List<MfForecastProbability>?,
    dt: Date
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
            probabilityForecast.time.time + 2.hours.inWholeMilliseconds == dt.time) {
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
            probabilityForecast.time.time + 5.hours.inWholeMilliseconds == dt.time) {
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
                    ((rainResult.properties.rainForecasts[i + 1].time.time - rainForecast.time.time) / 1.minutes.inWholeMilliseconds).toDouble()
                        .roundToInt()
                } else ((rainForecast.time.time - rainResult.properties.rainForecasts[i - 1].time.time) / 1.minutes.inWholeMilliseconds).toDouble()
                    .roundToInt(),
                precipitationIntensity = if (rainForecast.rainIntensity != null) getPrecipitationIntensity(rainForecast.rainIntensity) else null
            )
        )
    }
    return minutelyList
}

private fun getWarningsList(warningsResult: MfWarningsResult): List<Alert> {
    val alertList: MutableList<Alert> = arrayListOf()
    warningsResult.text?.let {
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
                alertList.add(
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
                )
            }
        }
    }
    warningsResult.timelaps?.forEach { timelaps ->
        timelaps.timelapsItems
            ?.filter { it.colorId > 1 }
            ?.forEach { timelapsItem ->
                alertList.add(
                    Alert(
                        // Create unique ID from: alert type ID, alert level, start time
                        alertId = Objects.hash(timelaps.phenomenonId, timelapsItem.colorId, timelapsItem.beginTime.time).toString(),
                        startDate = timelapsItem.beginTime,
                        endDate = timelapsItem.endTime,
                        headline = getWarningType(timelaps.phenomenonId) + " — " + getWarningText(timelapsItem.colorId),
                        description = if (timelapsItem.colorId >= 3) getWarningContent(
                            timelaps.phenomenonId, warningsResult
                        ) else null,
                        source = "Météo-France",
                        severity = AlertSeverity.getInstance(timelapsItem.colorId),
                        color = getWarningColor(timelapsItem.colorId)
                    )
                )
            }
    }
    return alertList
}

fun getNormals(location: Location, normalsResult: MfNormalsResult): Normals? {
    val currentMonth = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH]
    val normalsStats = normalsResult.properties?.stats?.getOrNull(currentMonth)
    return if (normalsStats != null) {
        Normals(
            month = currentMonth,
            daytimeTemperature = normalsStats.tMax,
            nighttimeTemperature = normalsStats.tMin
        )
    } else null
}

private fun getPrecipitationIntensity(rain: Int): Double = when (rain) {
    4 -> Precipitation.PRECIPITATION_HOURLY_HEAVY
    3 -> Precipitation.PRECIPITATION_HOURLY_MEDIUM
    2 -> Precipitation.PRECIPITATION_HOURLY_LIGHT
    else -> 0.0
}

private fun getWarningType(phemononId: String): String = when (phemononId) {
    "1" -> "Vent"
    "2" -> "Pluie-Inondation"
    "3" -> "Orages"
    "4" -> "Crues"
    "5" -> "Neige-Verglas"
    "6" -> "Canicule"
    "7" -> "Grand Froid"
    "8" -> "Avalanches"
    "9" -> "Vagues-Submersion"
    else -> "Divers"
}

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
    val consequences = warningsResult.consequences?.firstOrNull { it.phenomenonId == phenomenonId }?.textConsequence?.replace("<br>", "\n")
    val advices = warningsResult.advices?.firstOrNull { it.phenomenonId == phenomenonId }?.textAdvice?.replace("<br>", "\n")

    val content = StringBuilder()
    if (!textBlocs.isNullOrEmpty()) {
        textBlocs.forEach { textBlocItem ->
            if (content.toString().isNotEmpty()) {
                content.append("\n\n")
            }
            if (!textBlocItem.typeName.isNullOrEmpty()) {
                content
                    .append(textBlocItem.typeName.uppercase(Locale.FRENCH))
                    .append("\n")
            }
            textBlocItem.textItems?.filter { it.hazardCode == phenomenonId }?.forEach { textItem ->
                textItem.termItems?.forEach { termItem ->
                    termItem.subdivisionTexts?.forEach { subdivisionText ->
                        subdivisionText.text?.let {
                            content.append(it.joinToString("\n"))
                        }
                    }
                }
            }
        }
    }
    if (!consequences.isNullOrEmpty()) {
        if (content.toString().isNotEmpty()) {
            content.append("\n\n")
        }
        content
            .append("CONSÉQUENCES POSSIBLES\n")
            .append(consequences)
    }
    if (!advices.isNullOrEmpty()) {
        if (content.toString().isNotEmpty()) {
            content.append("\n\n")
        }
        content
            .append("CONSEILS DE COMPORTEMENT\n")
            .append(advices)
    }

    return content.toString().ifEmpty { null }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else with (icon) {
        when {
            // We need to take care of two-digits first
            startsWith("p32") || startsWith("p33") ||
                startsWith("p34") -> WeatherCode.WIND
            startsWith("p31") -> null // What is this?
            startsWith("p26") || startsWith("p27") || startsWith("p28") ||
                startsWith("p29") -> WeatherCode.THUNDER
            startsWith("p26") || startsWith("p27") || startsWith("p28") ||
                startsWith("p29") -> WeatherCode.THUNDER
            startsWith("p21") || startsWith("p22") ||
                startsWith("p23") -> WeatherCode.SNOW
            startsWith("p19") || startsWith("p20") -> WeatherCode.HAIL
            startsWith("p17") || startsWith("p18") -> WeatherCode.SLEET
            startsWith("p16") || startsWith("p24") || startsWith("p25") ||
                startsWith("p30") -> WeatherCode.THUNDERSTORM
            startsWith("p9") || startsWith("p10") || startsWith("p11") ||
                startsWith("p12") || startsWith("p13") || startsWith("p14") ||
                startsWith("p15") -> WeatherCode.RAIN
            startsWith("p6") || startsWith("p7") ||
                startsWith("p8") -> WeatherCode.FOG
            startsWith("p4") || startsWith("p5") -> WeatherCode.HAZE
            startsWith("p3") -> WeatherCode.CLOUDY
            startsWith("p2") -> WeatherCode.PARTLY_CLOUDY
            startsWith("p1") -> WeatherCode.CLEAR
            else -> null
        }
    }
}

/**
 * Secondary convert
 */
fun convertSecondary(
    location: Location,
    minuteResult: MfRainResult?,
    alertResultList: MfWarningsResult?,
    normalsResult: MfNormalsResult?
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        minutelyForecast = if (minuteResult != null) {
            getMinutelyList(minuteResult)
        } else null,
        alertList = if (alertResultList != null) {
            getWarningsList(alertResultList)
        } else null,
        normals = if (normalsResult != null) {
            getNormals(location, normalsResult)
        } else null
    )
}

fun getFrenchDepartmentName(frenchDepartmentCode: String): String? {
    return getFrenchDepartments().firstOrNull { it.first == frenchDepartmentCode }?.second
}

fun getFrenchDepartmentCode(frenchDepartmentName: String): String? {
    return getFrenchDepartments().firstOrNull { it.second == frenchDepartmentName }?.first
}

fun getFrenchDepartments(): List<Pair<String, String>> {
    return listOf(
        Pair("01", "Ain"),
        Pair("02", "Aisne"),
        Pair("03", "Allier"),
        Pair("04", "Alpes de Hautes-Provence"),
        Pair("05", "Hautes-Alpes"),
        Pair("06", "Alpes-Maritimes"),
        Pair("07", "Ardèche"),
        Pair("08", "Ardennes"),
        Pair("09", "Ariège"),
        Pair("10", "Aube"),
        Pair("11", "Aude"),
        Pair("12", "Aveyron"),
        Pair("13", "Bouches-du-Rhône"),
        Pair("14", "Calvados"),
        Pair("15", "Cantal"),
        Pair("16", "Charente"),
        Pair("17", "Charente-Maritime"),
        Pair("18", "Cher"),
        Pair("19", "Corrèze"),
        Pair("21", "Côte-d'Or"),
        Pair("22", "Côtes d'Armor"),
        Pair("23", "Creuse"),
        Pair("24", "Dordogne"),
        Pair("25", "Doubs"),
        Pair("26", "Drôme"),
        Pair("27", "Eure"),
        Pair("28", "Eure-et-Loir"),
        Pair("29", "Finistère"),
        Pair("2A", "Corse-du-Sud"),
        Pair("2B", "Haute-Corse"),
        Pair("30", "Gard"),
        Pair("31", "Haute-Garonne"),
        Pair("32", "Gers"),
        Pair("33", "Gironde"),
        Pair("34", "Hérault"),
        Pair("35", "Ille-et-Vilaine"),
        Pair("36", "Indre"),
        Pair("37", "Indre-et-Loire"),
        Pair("38", "Isère"),
        Pair("39", "Jura"),
        Pair("40", "Landes"),
        Pair("41", "Loir-et-Cher"),
        Pair("42", "Loire"),
        Pair("43", "Haute-Loire"),
        Pair("44", "Loire-Atlantique"),
        Pair("45", "Loiret"),
        Pair("46", "Lot"),
        Pair("47", "Lot-et-Garonne"),
        Pair("48", "Lozère"),
        Pair("49", "Maine-et-Loire"),
        Pair("50", "Manche"),
        Pair("51", "Marne"),
        Pair("52", "Haute-Marne"),
        Pair("53", "Mayenne"),
        Pair("54", "Meurthe-et-Moselle"),
        Pair("55", "Meuse"),
        Pair("56", "Morbihan"),
        Pair("57", "Moselle"),
        Pair("58", "Nièvre"),
        Pair("59", "Nord"),
        Pair("60", "Oise"),
        Pair("61", "Orne"),
        Pair("62", "Pas-de-Calais"),
        Pair("63", "Puy-de-Dôme"),
        Pair("64", "Pyrénées-Atlantiques"),
        Pair("65", "Hautes-Pyrénées"),
        Pair("66", "Pyrénées-Orientales"),
        Pair("67", "Bas-Rhin"),
        Pair("68", "Haut-Rhin"),
        Pair("69", "Rhône"),
        Pair("70", "Haute-Saône"),
        Pair("71", "Saône-et-Loire"),
        Pair("72", "Sarthe"),
        Pair("73", "Savoie"),
        Pair("74", "Haute-Savoie"),
        Pair("75", "Paris"),
        Pair("76", "Seine-Maritime"),
        Pair("77", "Seine-et-Marne"),
        Pair("78", "Yvelines"),
        Pair("79", "Deux-Sèvres"),
        Pair("80", "Somme"),
        Pair("81", "Tarn"),
        Pair("82", "Tarn-et-Garonne"),
        Pair("83", "Var"),
        Pair("84", "Vaucluse"),
        Pair("85", "Vendée"),
        Pair("86", "Vienne"),
        Pair("87", "Haute-Vienne"),
        Pair("88", "Vosges"),
        Pair("89", "Yonne"),
        Pair("90", "Territoire-de-Belfort"),
        Pair("91", "Essonne"),
        Pair("92", "Hauts-de-Seine"),
        Pair("93", "Seine-Saint-Denis"),
        Pair("94", "Val-de-Marne"),
        Pair("95", "Val-d'Oise"),
        Pair("99", "Andorre")
    )
}
