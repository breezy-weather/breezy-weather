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

package org.breezyweather.sources.knmi

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.parseRawGeoJson
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.knmi.json.KnmiDailyWeatherForecast
import org.breezyweather.sources.knmi.json.KnmiDailyWeatherGraph
import org.breezyweather.sources.knmi.json.KnmiHourlyWeatherForecast
import org.breezyweather.sources.knmi.json.KnmiUvIndex
import org.breezyweather.sources.knmi.json.KnmiWeather
import org.breezyweather.sources.knmi.json.KnmiWeatherAlerts
import org.breezyweather.sources.knmi.json.KnmiWeatherDetail
import org.breezyweather.sources.knmi.json.KnmiWeatherSnapshot
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

class KnmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "knmi"
    override val name = "KNMI - Royal Dutch Weather Institute (Netherlands)"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.knmi.nl/privacy"

    private val mApi by lazy {
        client.baseUrl(instance!!).build().create(KnmiApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to name,
        SourceFeature.CURRENT to name,
        SourceFeature.ALERT to name,
        SourceFeature.NORMALS to name
    )
    override val attributionLinks = mapOf(
        name to "https://www.knmi.nl/"
    )


    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val locationCode = getLocationCode(location)

        if (locationCode == null) {
            for (feature in requestedFeatures) {
                failedFeatures[feature] = InvalidLocationException()
            }
            // Can't do anything without a valid location, so return nothing
            return Observable.just(
                WeatherWrapper(
                    failedFeatures = failedFeatures
                )
            )
        }

        // This gets alerts from the API and uses OkHttp to download a html document containing advice for today (usually 1-2)
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts()
        } else {
            Observable.just(KnmiWeatherAlerts(null, null, null, null, null))
        }

        val weather = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getWeather(
                locationCode, null
            )
        } else {
            Observable.just(KnmiWeather(null, null, null, null, null, null, null, null))
        }

        val weatherDetail = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getWeatherDetail(
                locationCode, null, date = Date().getFormattedDate("yyyy-MM-dd")
            )
        } else {
            Observable.just(KnmiWeatherDetail(null, null, null, null, null, null, null, null, null))
        }

        val fourteenDayForecast = if (SourceFeature.FORECAST in requestedFeatures) {
            val twoDaysInFuture = Calendar.getInstance()
            twoDaysInFuture.add(Calendar.DATE, 2)
            mApi.getWeatherDetail(
                locationCode, null, date = twoDaysInFuture.getIsoFormattedDate()
                // Note: date needs to be yesterday or at least two days in the future. Otherwise WeatherDetail won't return 14 day date
            )
        } else {
            Observable.just(KnmiWeatherDetail(null, null, null, null, null, null, null, null, null))
        }


        val weatherSnapshot = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getWeatherSnapshot(locationCode, null)
        } else {
            Observable.just(KnmiWeatherSnapshot(null, null, null))
        }

        return Observable.zip(
            alerts, weather, weatherDetail, fourteenDayForecast, weatherSnapshot
        ) { alerts, weather, weatherDetail, fourteenDayForecast, weatherSnapshot ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(weather.daily, fourteenDayForecast.daily, weather.uvIndex)
                } else {
                    null
                }, hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(weather.hourly)
                } else {
                    null
                }, current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(weatherSnapshot)
                } else {
                    null
                }, alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(alerts, location)
                } else {
                    null
                }, normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(weatherDetail)
                } else {
                    null
                }, failedFeatures = failedFeatures
            )
        }
    }


    // This code is based on the iOS version of the KNMI app: https://gitlab.com/KNMI-OSS/KNMI-App/knmi-app-ios/-/blob/35a30d8543995b8f297612341b24d5f7f7e8a366/KNMI/SharedLibrary/Sources/GridDefinition/GridDefinition.swift
    // Which is licensed under the EUPL 1.2.
    private fun getLocationCode(
        location: Location,
    ): Int? {
        // Check if coordinates are inside grid.
        // As per KNMI app: for southWestColumnRow, northern eastern edge are outside bounds
        // and for northWestColumnRow, southern and eastern edges.
        when (gridDefinition.direction) {
            KnmiGridDirection.southWestColumnRow -> {
                if (!(location.latitude >= gridDefinition.southWest.latitude && location.latitude < gridDefinition.northEast.latitude && location.longitude >= gridDefinition.southWest.longitude && location.longitude < gridDefinition.northEast.longitude)) {
                    return null
                }
            }

            KnmiGridDirection.northWestColumnRow -> {
                if (!(location.latitude > gridDefinition.southWest.latitude && location.latitude <= gridDefinition.northEast.latitude && location.longitude >= gridDefinition.southWest.longitude && location.longitude < gridDefinition.northEast.longitude)) {
                    return null
                }
            }
        }

        val latitudeMultiplier =
            gridDefinition.steps.latitude.toDouble() / (gridDefinition.northEast.latitude - gridDefinition.southWest.latitude)
        val longitudeMultiplier =
            gridDefinition.steps.longitude.toDouble() / (gridDefinition.northEast.longitude - gridDefinition.southWest.longitude)

        return when (gridDefinition.direction) {
            KnmiGridDirection.southWestColumnRow -> {
                val latitudeCell =
                    ((location.latitude - gridDefinition.southWest.latitude) * latitudeMultiplier).toInt()
                val longitudeCell =
                    ((location.longitude - gridDefinition.southWest.longitude) * longitudeMultiplier).toInt()

                latitudeCell + longitudeCell * gridDefinition.steps.latitude

            }

            KnmiGridDirection.northWestColumnRow -> {
                val latitudeCell =
                    ((gridDefinition.northEast.latitude - location.latitude) * latitudeMultiplier).toInt()
                val longitudeCell =
                    ((location.longitude - gridDefinition.southWest.longitude) * longitudeMultiplier).toInt()

                latitudeCell + longitudeCell * gridDefinition.steps.latitude

            }
        }
    }

    /**
     * Returns current weather
     */
    private fun getCurrent(
        snapshot: KnmiWeatherSnapshot?,
    ): CurrentWrapper? {
        return CurrentWrapper(
            weatherCode = getWeatherCode(snapshot?.weatherType), temperature = TemperatureWrapper(
                temperature = snapshot?.temperature?.celsius
            )
        )
    }

    private fun getDailyForecast(
        dailyWeatherForecast: KnmiDailyWeatherForecast?,
        dailyWeatherGraph: KnmiDailyWeatherGraph?,
        todayUv: KnmiUvIndex?,
    ): List<DailyWrapper>? {
        val numDays = dailyWeatherGraph?.temperature?.dates?.size ?: dailyWeatherForecast?.forecast?.size ?: return null

        val dailyList: MutableList<DailyWrapper> = ArrayList(numDays)
        for (i in 0 until numDays) {
            val date = dailyWeatherGraph?.temperature?.dates?.get(i) ?: dailyWeatherForecast?.forecast?.get(i)?.date
            ?: continue // date is required so fail, really should not happen

            val temperatureDay = dailyWeatherGraph?.temperature?.maxTemperatures?.get(i)?.celsius
            // use next day's minimum temp for night
            val temperatureNight = dailyWeatherGraph?.temperature?.minTemperatures?.getOrNull(i + 1)?.celsius
            val precipitation = dailyWeatherGraph?.precipitation?.amounts?.get(i)?.millimeters
            val todayForecast = dailyWeatherForecast?.forecast?.getOrNull(i)
            val precipitationChance = if (todayForecast?.date == date) {
                todayForecast.precipitation?.chance?.percent
            } else {
                null
            }

            val weatherCode = if (todayForecast?.date == date) {
                getWeatherCode(todayForecast.weatherType)
            } else {
                null
            }

            dailyList.add(
                DailyWrapper(
                    date = date, day = HalfDayWrapper(
                        weatherCode = weatherCode, temperature = TemperatureWrapper(
                            temperature = temperatureDay
                        ), precipitation = Precipitation(
                            total = precipitation
                        ), precipitationProbability = PrecipitationProbability(
                            total = precipitationChance
                        )
                    ), night = HalfDayWrapper(
//                        weatherCode = weatherCode,
                        temperature = TemperatureWrapper(
                            temperature = temperatureNight,
                        )
                    ), uV = if (i == 0) { // put the current UV day value into the first day
                        UV(index = todayUv?.value?.toDouble())
                    } else {
                        null
                    }
                )
            )
        }

        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: KnmiHourlyWeatherForecast?,
    ): List<HourlyWrapper>? {
        return hourlyResult?.forecast?.map {
            HourlyWrapper(
                date = it.dateTime ?: return null,
                weatherCode = getWeatherCode(it.weatherType),
                temperature = TemperatureWrapper(
                    temperature = it.temperature?.celsius,
                ),
                precipitation = Precipitation(
                    total = it.precipitation?.amount?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    total = it.precipitation?.chance?.percent
                ),
                wind = Wind(
                    degree = it.wind?.degree,
                    // the API also gives beaufort wind speeds, but use km/h instead for more precision.
                    // The app will convert automatically if the user requests those units.
                    speed = it.wind?.speed?.kilometersPerHour, gusts = it.wind?.gusts?.kilometersPerHour
                )
            )
        }
    }

    /**
     * Returns alerts
     */
    private fun getAlertList(
        alerts: KnmiWeatherAlerts?,
        location: Location,
    ): List<Alert>? {

        if (alerts?.regions.isNullOrEmpty()) return null
        val currentRegion = getAlertRegion(location)
        return alerts.regions.firstOrNull { it.region == currentRegion }?.alerts?.map { alert ->
            val severity = when (alert.level?.lowercase()) {
                "red" -> AlertSeverity.EXTREME
                "orange" -> AlertSeverity.SEVERE
                "yellow" -> AlertSeverity.MODERATE
                "potential" -> AlertSeverity.MINOR
                "none" -> AlertSeverity.UNKNOWN
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                // Create unique ID from: title, severity, description
                alertId = Objects.hash(alert.title, alert.level, alert.description).toString(),
                headline = alert.title,
                description = alert.description,
                severity = severity,
                color = Alert.colorFromSeverity(severity),
            )
        }
    }


    private fun getAlertRegion(
        location: Location,
    ): Int? {
        return alertAreas.features.firstOrNull { isInsideAlertArea(it, location) }?.getProperty("id")?.toIntOrNull()
    }

    private fun isInsideAlertArea(
        // same as isMatchingTimeZone from BreezyTimeZoneService
        feature: Feature,
        location: Location,
    ): Boolean {
        return when (feature.geometry) {
            is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
            }
            // file should only have GeoJsonPolygons, but include just in case
            is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                it.coordinates.any { polygon ->
                    PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                }
            }

            else -> false
        }
    }

    // The weather code is always used for the app background
    // KnmiBackground is ignored altogether
    private fun getWeatherCode(knmiCode: Int?): WeatherCode? {
        // see https://gitlab.com/KNMI-OSS/KNMI-App/knmi-app-api/-/blob/9ce829305e0b3f91cfb2977b86e1727029dc35ba/app/helpers/weather.ts
        // there are some extra codes for alerts, but not needed here: https://gitlab.com/KNMI-OSS/KNMI-App/knmi-app-api/-/blob/9ce829305e0b3f91cfb2977b86e1727029dc35ba/app/models/WeatherAlertsModel.ts
        // Breezy Weather has less icons, so this is done on a best-effort basis
        return when (knmiCode) {
            1380, 1381 -> WeatherCode.RAIN // lichte-bui-afgewisseld-door-zon - light rain with sun
            1377, 1377 -> WeatherCode.CLOUDY // lichte-regen-zwaar-bewolkt - light rain heavy clouds
            1405, 1406 -> WeatherCode.SNOW // lichte-sneeuwbui-afgewisseld-door-zon - light snow with sun
            1404, 1404 -> WeatherCode.SNOW // lichte-sneeuwbui-zwaar-bewolkt - light snow heavy clouds
            1382, 1383 -> WeatherCode.RAIN // matige-bui-afgewisseld-door-zon - moderate rain with sun
            1378, 1378 -> WeatherCode.RAIN // matige-regen-zwaar-bewolkt - moderate rain heavy clouds
            1408, 1409 -> WeatherCode.SNOW // matige-sneeuwbui-afgewisseld-door-zon - moderate snow with sun
            1407, 1407 -> WeatherCode.SNOW // matige-sneeuwbui-zwaar-bewolkt - moderate snow heavy clouds
            1375, 1376 -> WeatherCode.PARTLY_CLOUDY // opklaringen - cloudy with some sun
            1417, 1418 -> WeatherCode.HAIL // hagelbui-afgewisseld-door-zon - hail with sun
            1416, 1416 -> WeatherCode.HAIL // hagelbui-zwaar-bewolkt - hail with heavy clouds
            1420, 1420 -> WeatherCode.FOG // mist - fog
            1387, 1388 -> WeatherCode.PARTLY_CLOUDY // motregen-afgewisseld-door-opklaringen - drizzle with some sun
            1386, 1386 -> WeatherCode.CLOUDY // motregen-zwaar-bewolkt - drizzle with heavy clouds
            1396, 1397 -> WeatherCode.THUNDERSTORM // onweer-hagel-afgewisseld-door-zon - thunderstorm and hail with sun
            1395, 1395 -> WeatherCode.THUNDERSTORM // onweer-hagel-zwaar-bewolkt - thunderstorm with hail and heavy clouds
            1390, 1390 -> WeatherCode.THUNDERSTORM // onweer-zware-regen-zwaar-bewolkt - thunderstorm with heavy rain and heavy clouds
            1391, 1392 -> WeatherCode.THUNDERSTORM // onweer-matige-regen-afgewisseld-door- - thunderstorm moderate rain with sun (last word is a guess)
            1389, 1389 -> WeatherCode.THUNDERSTORM // onweer-matige-regen-zwaar-bewolkt - thunderstorm moderate rain heavy clouds
            1399, 1400 -> WeatherCode.THUNDERSTORM // winterse-buien-onweer-afgewisseld-doo - winter rain and thunderstorm with sun (last word is a guess)
            1398, 1398 -> WeatherCode.THUNDERSTORM // winterse-buien-onweer-zwaar-bewolkt - winter rain and thunderstorms with heavy clouds
            1402, 1403 -> WeatherCode.SNOW // sneeuwbui-met-onweer-afgewisseld-door - snow with thunder and sun
            1401, 1401 -> WeatherCode.SNOW // sneeuwbui-met-onweer-zwaar-bewolkt - snow with thunder and heavy rain
            1384, 1385 -> WeatherCode.RAIN // zware-bui-afgewisseld-door-zon - heavy rain with sun
            1379, 1379 -> WeatherCode.RAIN // zware-regen-zwaar-bewolkt - heavy rain heavy clouds
            1411, 1412 -> WeatherCode.SNOW // zware-sneeuwbui-afgewisseld-door-zon - heavy snows with sun
            1414, 1415 -> WeatherCode.SNOW // natte-sneeuwbui-afgewisseld-door-zon - wet snow with sun
            1413, 1413 -> WeatherCode.SNOW // natte-sneeuwbui-zwaar-bewolkt - wet snow heavy clouds
            1414, 1415 -> WeatherCode.SNOW // natte-sneeuwbui-afgewisseld-door-zon - wet snow with sun
            1413, 1413 -> WeatherCode.SNOW // natte-sneeuwbui-zwaar-bewolkt - wet snow heavy clouds
            1410, 1410 -> WeatherCode.SNOW // zware-sneeuwbui-zwaar-bewolkt - heavy snow heavy clouds
            1372, 1373 -> WeatherCode.CLEAR // zonnig / onbewolkt - sunny/no clouds
            1374, 1374 -> WeatherCode.CLOUDY // zwaar-bewolkt - heavy clouds
            1419, 1419 -> WeatherCode.SLEET // gladheid-door-ijzel - slipperiness due to sleet
            1393, 1394 -> WeatherCode.THUNDERSTORM // onweer-zware-regen-afgewisseld-door-zon - thunderstorm heavy rain with sun
            else -> null
        }
    }

    private fun getNormals(
        weatherDetail: KnmiWeatherDetail?,
    ): Map<Month, Normals>? {
        if (weatherDetail?.climate?.month == null) return null
        return mapOf(
            Month.of(weatherDetail.climate.month) to Normals(
                daytimeTemperature = weatherDetail.climate.averageTemperatureRange?.max?.celsius,
                // this is monthly, so nighttime temp is the minimum
                nighttimeTemperature = weatherDetail.climate.averageTemperatureRange?.min?.celsius
            )
        )
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null)
            ?: if (BreezyWeather.instance.debugMode) KNMI_DEMO_BASE_URL else KNMI_BASE_URL

    override val testingLocations: List<Location> = emptyList()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_knmi_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        if (BreezyWeather.instance.debugMode) KNMI_DEMO_BASE_URL else KNMI_BASE_URL
                    }
                },
                content = if (instance != if (BreezyWeather.instance.debugMode) {
                        KNMI_DEMO_BASE_URL
                    } else {
                        KNMI_BASE_URL
                    }
                ) {
                    instance
                } else {
                    null
                },
                placeholder = if (BreezyWeather.instance.debugMode) {
                    KNMI_DEMO_BASE_URL
                } else {
                    KNMI_BASE_URL
                },
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == if (BreezyWeather.instance.debugMode) {
                            KNMI_DEMO_BASE_URL
                        } else {
                            KNMI_BASE_URL
                        }
                    ) {
                        null
                    } else {
                        it.ifEmpty { null }
                    }
                })
        )
    }

    override val isConfigured = true

    override val isRestricted = false

    // This does not make sense as a setting. Should be changed if the KNMI changes their API at some point
    private val gridDefinition = KnmiGridDefinition(
        southWest = Location(50.7, 3.2),
        northEast = Location(53.6, 7.4),
        steps = KnmiGridSteps(35, 30),
        prefix = "A",
        proj = "epsg4326",
        direction = KnmiGridDirection.northWestColumnRow
    )

    private val alertAreas: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.source_knmi_alert_regions_simplified) }


    companion object {
        private const val KNMI_BASE_URL = "https://api.app.knmi.cloud/"
        private const val KNMI_DEMO_BASE_URL = "https://api.app.dev.knmi.cloud/"
    }
}
