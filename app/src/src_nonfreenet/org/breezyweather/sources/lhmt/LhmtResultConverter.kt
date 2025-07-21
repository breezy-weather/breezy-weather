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

package org.breezyweather.sources.lhmt

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.lhmt.json.LhmtAlertText
import org.breezyweather.sources.lhmt.json.LhmtAlertsResult
import org.breezyweather.sources.lhmt.json.LhmtLocationsResult
import org.breezyweather.sources.lhmt.json.LhmtWeatherResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// reverse geocoding
internal fun convert(
    location: Location,
    forecastLocations: List<LhmtLocationsResult>,
): List<Location> {
    val locationList = mutableListOf<Location>()
    val forecastLocationMap = forecastLocations.filter { it.countryCode == null || it.countryCode == "LT" }.associate {
        it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude)
    }
    val forecastLocation = LatLng(location.latitude, location.longitude)
        .getNearestLocation(forecastLocationMap, 50000.0)
    forecastLocations.firstOrNull { it.code == forecastLocation }?.let {
        val municipalityName = it.administrativeDivision
        val municipalityCode = MUNICIPALITIES.firstOrNull { pair ->
            pair.second == municipalityName
        }?.first
        val countyCode = COUNTIES_MUNICIPALITIES.firstOrNull { pair ->
            pair.second == municipalityCode
        }?.first
        val countyName = COUNTIES.firstOrNull { pair ->
            pair.first == countyCode
        }?.second
        locationList.add(
            location.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                timeZone = "Europe/Vilnius",
                country = "Lithuania",
                countryCode = "LT",
                admin1 = countyName,
                admin1Code = countyCode,
                admin2 = municipalityName,
                admin2Code = municipalityCode,
                city = it.name ?: ""
            )
        )
    }
    return locationList
}

// location parameters
internal fun convert(
    location: Location,
    forecastLocations: List<LhmtLocationsResult>,
    currentLocations: List<LhmtLocationsResult>,
): Map<String, String> {
    val forecastLocationMap = forecastLocations.filter { it.countryCode == null || it.countryCode == "LT" }.associate {
        it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude)
    }
    val forecastLocation = LatLng(location.latitude, location.longitude)
        .getNearestLocation(forecastLocationMap, 50000.0)

    val currentLocationMap = currentLocations.filter { it.countryCode == null || it.countryCode == "LT" }.associate {
        it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude)
    }
    val currentLocation = LatLng(location.latitude, location.longitude).getNearestLocation(currentLocationMap, 50000.0)

    val municipalityName = forecastLocations.firstOrNull { it.code == forecastLocation }?.administrativeDivision
    val municipalityCode = MUNICIPALITIES.firstOrNull { pair ->
        pair.second == municipalityName
    }?.first
    val countyCode = COUNTIES_MUNICIPALITIES.firstOrNull { pair ->
        pair.second == municipalityCode
    }?.first

    if (forecastLocation == null || currentLocation == null || municipalityCode == null || countyCode == null) {
        throw InvalidLocationException()
    }

    return mapOf(
        "forecastLocation" to forecastLocation,
        "currentLocation" to currentLocation,
        "municipality" to municipalityCode,
        "county" to countyCode
    )
}

internal fun getCurrent(
    context: Context,
    currentResult: LhmtWeatherResult,
): CurrentWrapper? {
    return currentResult.observations?.last()?.let {
        CurrentWrapper(
            weatherText = getWeatherText(context, it.conditionCode),
            weatherCode = getWeatherCode(it.conditionCode),
            temperature = TemperatureWrapper(
                temperature = it.airTemperature,
                feelsLike = it.feelsLikeTemperature
            ),
            wind = Wind(
                degree = it.windDirection,
                speed = it.windSpeed,
                gusts = it.windGust
            ),
            relativeHumidity = it.relativeHumidity,
            pressure = it.seaLevelPressure,
            cloudCover = it.cloudCover?.toInt()
        )
    }
}

internal fun getDailyForecast(
    hourlyForecast: List<HourlyWrapper>?,
): List<DailyWrapper> {
    if (hourlyForecast.isNullOrEmpty()) return emptyList()

    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Vilnius")
    val hourlyListDates = hourlyForecast.groupBy { formatter.format(it.date) }.keys

    return hourlyListDates.map {
        DailyWrapper(
            date = formatter.parse(it)!!
        )
    }.dropLast(1) // Remove last (incomplete) daily item
}

internal fun getHourlyForecast(
    context: Context,
    forecastResult: LhmtWeatherResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    forecastResult.forecastTimestamps?.forEach {
        if (it.forecastTimeUtc != null) {
            hourlyList.add(
                HourlyWrapper(
                    date = it.forecastTimeUtc,
                    weatherText = getWeatherText(context, it.conditionCode),
                    weatherCode = getWeatherCode(it.conditionCode),
                    temperature = TemperatureWrapper(
                        temperature = it.airTemperature,
                        feelsLike = it.feelsLikeTemperature
                    ),
                    precipitation = Precipitation(
                        total = it.totalPrecipitation
                    ),
                    wind = Wind(
                        degree = it.windDirection,
                        speed = it.windSpeed,
                        gusts = it.windGust
                    ),
                    relativeHumidity = it.relativeHumidity,
                    pressure = it.seaLevelPressure,
                    cloudCover = it.cloudCover?.toInt()
                )
            )
        }
    }
    return hourlyList
}

internal fun getAlertList(
    context: Context,
    location: Location,
    alertsResult: LhmtAlertsResult,
): List<Alert> {
    val id = "lhmt"
    val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
    val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }
    if (municipality.isNullOrEmpty() || county.isNullOrEmpty()) {
        throw InvalidLocationException()
    }

    val alertList = mutableListOf<Alert>()
    var severity: AlertSeverity
    var active: Boolean
    alertsResult.phenomenonGroups?.forEach { phenomenonGroup ->
        phenomenonGroup.areaGroups?.forEach { areaGroup ->
            active = false
            areaGroup.areas?.forEach { area ->
                active = active || (area.id.endsWith(municipality) || area.id.endsWith(county))
            }
            if (active) {
                areaGroup.singleAlerts?.forEach { singleAlert ->
                    if (singleAlert.responseType?.none != true) {
                        severity = getAlertSeverity(singleAlert.severity)
                        alertList.add(
                            Alert(
                                alertId = singleAlert.phenomenon +
                                    " " +
                                    singleAlert.severity +
                                    " " +
                                    singleAlert.tFrom?.time.toString(),
                                startDate = singleAlert.tFrom,
                                endDate = singleAlert.tTo,
                                headline = getAlertText(context, singleAlert.headline),
                                description = getAlertText(context, singleAlert.description),
                                instruction = getAlertText(context, singleAlert.instruction),
                                source = "Lietuvos hidrometeorologijos tarnyba",
                                severity = severity,
                                color = Alert.colorFromSeverity(severity)
                            )
                        )
                    }
                }
            }
        }
    }
    return alertList
}

private fun getWeatherText(
    context: Context,
    code: String?,
): String? {
    return when (code) {
        "clear" -> context.getString(R.string.common_weather_text_clear_sky)
        "partly-cloudy" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "variable-cloudiness" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "cloudy-with-sunny-intervals" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "cloudy" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "rain-showers" -> context.getString(R.string.common_weather_text_rain_showers)
        "light-rain-at-times" -> context.getString(R.string.common_weather_text_rain_light)
        "rain-at-times" -> context.getString(R.string.common_weather_text_rain)
        "light-rain" -> context.getString(R.string.common_weather_text_rain_light)
        "rain" -> context.getString(R.string.common_weather_text_rain)
        "heavy-rain" -> context.getString(R.string.common_weather_text_rain_heavy)
        "thunder" -> context.getString(R.string.weather_kind_thunder)
        "isolated-thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "heavy-rain-with-thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "sleet-showers" -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
        "sleet-at-times" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "light-sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        "sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "freezing-rain" -> context.getString(R.string.common_weather_text_rain_freezing)
        "hail" -> context.getString(R.string.weather_kind_hail)
        "snow-showers" -> context.getString(R.string.common_weather_text_snow_showers)
        "light-snow-at-times" -> context.getString(R.string.common_weather_text_snow_light)
        "snow-at-times" -> context.getString(R.string.common_weather_text_snow)
        "light-snow" -> context.getString(R.string.common_weather_text_snow_light)
        "snow" -> context.getString(R.string.common_weather_text_snow)
        "heavy-snow" -> context.getString(R.string.common_weather_text_snow_heavy)
        "snowstorm" -> context.getString(R.string.common_weather_text_snow_heavy)
        "fog" -> context.getString(R.string.common_weather_text_fog)
        "squall" -> context.getString(R.string.common_weather_text_squall)
        else -> null
    }
}

private fun getWeatherCode(
    code: String?,
): WeatherCode? {
    return when (code) {
        "clear" -> WeatherCode.CLEAR
        "partly-cloudy" -> WeatherCode.PARTLY_CLOUDY
        "variable-cloudiness" -> WeatherCode.PARTLY_CLOUDY
        "cloudy-with-sunny-intervals" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "rain-showers" -> WeatherCode.RAIN
        "light-rain-at-times" -> WeatherCode.RAIN
        "rain-at-times" -> WeatherCode.RAIN
        "light-rain" -> WeatherCode.RAIN
        "rain" -> WeatherCode.RAIN
        "heavy-rain" -> WeatherCode.RAIN
        "thunder" -> WeatherCode.THUNDER
        "isolated-thunderstorms" -> WeatherCode.THUNDERSTORM
        "thunderstorms" -> WeatherCode.THUNDERSTORM
        "heavy-rain-with-thunderstorms" -> WeatherCode.THUNDERSTORM
        "sleet-showers" -> WeatherCode.SLEET
        "sleet-at-times" -> WeatherCode.SLEET
        "light-sleet" -> WeatherCode.SLEET
        "sleet" -> WeatherCode.SLEET
        "freezing-rain" -> WeatherCode.SLEET
        "hail" -> WeatherCode.HAIL
        "snow-showers" -> WeatherCode.SNOW
        "light-snow-at-times" -> WeatherCode.SNOW
        "snow-at-times" -> WeatherCode.SNOW
        "light-snow" -> WeatherCode.SNOW
        "snow" -> WeatherCode.SNOW
        "heavy-snow" -> WeatherCode.SNOW
        "snowstorm" -> WeatherCode.SNOW
        "fog" -> WeatherCode.FOG
        "squall" -> WeatherCode.WIND
        else -> null
    }
}

private fun getAlertText(
    context: Context,
    text: LhmtAlertText?,
): String? {
    return if (context.currentLocale.code.startsWith("lt")) {
        text?.lt
    } else {
        text?.en
    }
}

private fun getAlertSeverity(
    severity: String?,
): AlertSeverity {
    return with(severity) {
        when {
            equals("Extreme", ignoreCase = true) -> AlertSeverity.EXTREME
            equals("Severe", ignoreCase = true) -> AlertSeverity.SEVERE
            equals("Moderate", ignoreCase = true) -> AlertSeverity.MODERATE
            equals("Minor", ignoreCase = true) -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
    }
}

// The municipality codes used by LHMT is obtained from any of their warning files,
// e.g. https://www.meteo.lt/meteo_jobs/pavojingi_met_reisk_ibl/20240910115424-00000280
//
// The codes are then matched to the names given by this endpoint:
// https://api.meteo.lt/v1/places
//
// We use the long names from that endpoint rather than the shortened names in the warning file,
// so that we can directly assign location parameters without further manipulation of the output.
//
// These codes are used only by LHMT for identifying whether an alert applies to a municipality.
// They are not related to Lithuania's ISO 3166-2 subdivision codes.
private val MUNICIPALITIES = listOf(
    Pair("LT032", "Akmenės rajono savivaldybė"),
    Pair("LT011", "Alytaus miesto savivaldybė"),
    Pair("LT033", "Alytaus rajono savivaldybė"),
    Pair("LT034", "Anykščių rajono savivaldybė"),
    Pair("LT012", "Birštono savivaldybė"),
    Pair("LT036", "Biržų rajono savivaldybė"),
    Pair("LT015", "Druskininkų savivaldybė"),
    Pair("LT042", "Elektrėnų savivaldybė"),
    Pair("LT045", "Ignalinos rajono savivaldybė"),
    Pair("LT046", "Jonavos rajono savivaldybė"),
    Pair("LT047", "Joniškio rajono savivaldybė"),
    Pair("LT094", "Jurbarko rajono savivaldybė"),
    Pair("LT049", "Kaišiadorių rajono savivaldybė"),
    Pair("LT048", "Kalvarijos savivaldybė"),
    Pair("LT019", "Kauno miesto savivaldybė"),
    Pair("LT052", "Kauno rajono savivaldybė"),
    Pair("LT058", "Kazlų Rūdos savivaldybė"),
    Pair("LT053", "Kelmės rajono savivaldybė"),
    Pair("LT054", "Klaipėdos miesto savivaldybė"),
    Pair("LT021", "Klaipėdos rajono savivaldybė"),
    Pair("LT055", "Kretingos rajono savivaldybė"),
    Pair("LT056", "Kupiškio rajono savivaldybė"),
    Pair("LT057", "Kėdainių rajono savivaldybė"),
    Pair("LT059", "Lazdijų rajono savivaldybė"),
    Pair("LT018", "Marijampolės savivaldybė"),
    Pair("LT061", "Mažeikių rajono savivaldybė"),
    Pair("LT062", "Molėtų rajono savivaldybė"),
    Pair("LT023", "Neringos miesto savivaldybė"),
    Pair("LT063", "Pagėgių savivaldybė"),
    Pair("LT065", "Pakruojo rajono savivaldybė"),
    Pair("LT025", "Palangos miesto savivaldybė"),
    Pair("LT027", "Panevėžio miesto savivaldybė"),
    Pair("LT066", "Panevėžio rajono savivaldybė"),
    Pair("LT067", "Pasvalio rajono savivaldybė"),
    Pair("LT068", "Plungės rajono savivaldybė"),
    Pair("LT069", "Prienų rajono savivaldybė"),
    Pair("LT071", "Radviliškio rajono savivaldybė"),
    Pair("LT072", "Raseinių rajono savivaldybė"),
    Pair("LT074", "Rietavo savivaldybė"),
    Pair("LT073", "Rokiškio rajono savivaldybė"),
    Pair("LT084", "Šakių rajono savivaldybė"),
    Pair("LT085", "Šalčininkų rajono savivaldybė"),
    Pair("LT029", "Šiaulių miesto savivaldybė"),
    Pair("LT091", "Šiaulių rajono savivaldybė"),
    Pair("LT087", "Šilalės rajono savivaldybė"),
    Pair("LT088", "Šilutės rajono savivaldybė"),
    Pair("LT089", "Širvintų rajono savivaldybė"),
    Pair("LT075", "Skuodo rajono savivaldybė"),
    Pair("LT086", "Švenčionių rajono savivaldybė"),
    Pair("LT077", "Tauragės rajono savivaldybė"),
    Pair("LT078", "Telšių rajono savivaldybė"),
    Pair("LT079", "Trakų rajono savivaldybė"),
    Pair("LT081", "Ukmergės rajono savivaldybė"),
    Pair("LT082", "Utenos rajono savivaldybė"),
    Pair("LT038", "Varėnos rajono savivaldybė"),
    Pair("LT039", "Vilkaviškio rajono savivaldybė"),
    Pair("LT013", "Vilniaus miesto savivaldybė"),
    Pair("LT041", "Vilniaus rajono savivaldybė"),
    Pair("LT030", "Visagino savivaldybė"),
    Pair("LT043", "Zarasų rajono savivaldybė")
)

// The county codes used by LHMT is obtained from any of their warning files,
// e.g. https://www.meteo.lt/meteo_jobs/pavojingi_met_reisk_ibl/20240910115424-00000280
//
// These codes are used only by LHMT for identifying whether an alert applies to a municipality.
// They are not related to Lithuania's ISO 3166-2 subdivision codes.
private val COUNTIES = listOf(
    Pair("LT001", "Alytaus apskritis"),
    Pair("LT002", "Kauno apskritis"),
    Pair("LT003", "Klaipėdos apskritis"),
    Pair("LT004", "Marijampolės apskritis"),
    Pair("LT005", "Panevėžio apskritis"),
    Pair("LT006", "Šiaulių apskritis"),
    Pair("LT007", "Tauragės apskritis"),
    Pair("LT008", "Telšių apskritis"),
    Pair("LT009", "Utenos apskritis"),
    Pair("LT010", "Vilniaus apskritis")
)

// The above LHMT county and municipality codes are matched according to this table:
// https://en.wikipedia.org/wiki/Municipalities_of_Lithuania#Municipalities
private val COUNTIES_MUNICIPALITIES = listOf(
    Pair("LT006", "LT032"), // Šiaulių apskritis -> Akmenės rajono savivaldybė
    Pair("LT001", "LT011"), // Alytaus apskritis -> Alytaus miesto savivaldybė
    Pair("LT001", "LT033"), // Alytaus apskritis -> Alytaus rajono savivaldybė
    Pair("LT009", "LT034"), // Utenos apskritis -> Anykščių rajono savivaldybė
    Pair("LT002", "LT012"), // Kauno apskritis -> Birštono savivaldybė
    Pair("LT005", "LT036"), // Panevėžio apskritis -> Biržų rajono savivaldybė
    Pair("LT001", "LT015"), // Alytaus apskritis -> Druskininkų savivaldybė
    Pair("LT010", "LT042"), // Vilniaus apskriti -> Elektrėnų savivaldybė
    Pair("LT009", "LT045"), // Utenos apskritis -> Ignalinos rajono savivaldybė
    Pair("LT002", "LT046"), // Kauno apskritis -> Jonavos rajono savivaldybė
    Pair("LT006", "LT047"), // Šiaulių apskritis -> Joniškio rajono savivaldybė
    Pair("LT007", "LT094"), // Tauragės apskritis -> Jurbarko rajono savivaldybė
    Pair("LT002", "LT049"), // Kauno apskritis -> Kaišiadorių rajono savivaldybė
    Pair("LT004", "LT048"), // Marijampolės apskritis -> Kalvarijos savivaldybė
    Pair("LT002", "LT019"), // Kauno apskritis -> Kauno miesto savivaldybė
    Pair("LT002", "LT052"), // Kauno apskritis -> Kauno rajono savivaldybė
    Pair("LT004", "LT058"), // Marijampolės apskritis -> Kazlų Rūdos savivaldybė
    Pair("LT006", "LT053"), // Šiaulių apskritis -> Kelmės rajono savivaldybė
    Pair("LT003", "LT054"), // Klaipėdos apskritis -> Klaipėdos miesto savivaldybė
    Pair("LT003", "LT021"), // Klaipėdos apskritis -> Klaipėdos rajono savivaldybė
    Pair("LT003", "LT055"), // Klaipėdos apskritis -> Kretingos rajono savivaldybė
    Pair("LT005", "LT056"), // Panevėžio apskritis -> Kupiškio rajono savivaldybė
    Pair("LT002", "LT057"), // Kauno apskritis -> Kėdainių rajono savivaldybė
    Pair("LT001", "LT059"), // Alytaus apskritis -> Lazdijų rajono savivaldybė
    Pair("LT004", "LT018"), // Marijampolės apskritis -> Marijampolės savivaldybė
    Pair("LT008", "LT061"), // Telšių apskritis -> Mažeikių rajono savivaldybė
    Pair("LT009", "LT062"), // Utenos apskritis -> Molėtų rajono savivaldybė
    Pair("LT003", "LT023"), // Klaipėdos apskritis -> Neringos miesto savivaldybė
    Pair("LT007", "LT063"), // Tauragės apskritis -> Pagėgių savivaldybė
    Pair("LT006", "LT065"), // Šiaulių apskritis -> Pakruojo rajono savivaldybė
    Pair("LT003", "LT025"), // Klaipėdos apskritis -> Palangos miesto savivaldybė
    Pair("LT005", "LT027"), // Panevėžio apskritis -> Panevėžio miesto savivaldybė
    Pair("LT005", "LT066"), // Panevėžio apskritis -> Panevėžio rajono savivaldybė
    Pair("LT005", "LT067"), // Panevėžio apskritis -> Pasvalio rajono savivaldybė
    Pair("LT008", "LT068"), // Telšių apskritis -> Plungės rajono savivaldybė
    Pair("LT002", "LT069"), // Kauno apskritis -> Prienų rajono savivaldybė
    Pair("LT006", "LT071"), // Šiaulių apskritis -> Radviliškio rajono savivaldybė
    Pair("LT002", "LT072"), // Kauno apskritis -> Raseinių rajono savivaldybė
    Pair("LT008", "LT074"), // Telšių apskritis -> Rietavo savivaldybė
    Pair("LT005", "LT073"), // Panevėžio apskritis -> Rokiškio rajono savivaldybė
    Pair("LT004", "LT084"), // Marijampolės apskritis -> Šakių rajono savivaldybė
    Pair("LT010", "LT085"), // Vilniaus apskriti -> Šalčininkų rajono savivaldybė
    Pair("LT006", "LT029"), // Šiaulių apskritis -> Šiaulių miesto savivaldybė
    Pair("LT006", "LT091"), // Šiaulių apskritis -> Šiaulių rajono savivaldybė
    Pair("LT007", "LT087"), // Tauragės apskritis -> Šilalės rajono savivaldybė
    Pair("LT003", "LT088"), // Klaipėdos apskritis -> Šilutės rajono savivaldybė
    Pair("LT010", "LT089"), // Vilniaus apskriti -> Širvintų rajono savivaldybė
    Pair("LT003", "LT075"), // Klaipėdos apskritis -> Skuodo rajono savivaldybė
    Pair("LT010", "LT086"), // Vilniaus apskriti -> Švenčionių rajono savivaldybė
    Pair("LT007", "LT077"), // Tauragės apskritis -> Tauragės rajono savivaldybė
    Pair("LT008", "LT078"), // Telšių apskritis -> Telšių rajono savivaldybė
    Pair("LT010", "LT079"), // Vilniaus apskriti -> Trakų rajono savivaldybė
    Pair("LT010", "LT081"), // Vilniaus apskriti -> Ukmergės rajono savivaldybė
    Pair("LT009", "LT082"), // Utenos apskritis -> Utenos rajono savivaldybė
    Pair("LT001", "LT038"), // Alytaus apskritis -> Varėnos rajono savivaldybė
    Pair("LT004", "LT039"), // Marijampolės apskritis -> Vilkaviškio rajono savivaldybė
    Pair("LT010", "LT013"), // Vilniaus apskriti -> Vilniaus miesto savivaldybė
    Pair("LT010", "LT041"), // Vilniaus apskriti -> Vilniaus rajono savivaldybė
    Pair("LT009", "LT030"), // Utenos apskritis -> Visagino savivaldybė
    Pair("LT009", "LT043") // Utenos apskritis -> Zarasų rajono savivaldyb
)
