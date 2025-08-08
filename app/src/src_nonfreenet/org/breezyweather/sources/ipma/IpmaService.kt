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

package org.breezyweather.sources.ipma

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
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
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.ipma.json.IpmaAlertResult
import org.breezyweather.sources.ipma.json.IpmaDistrictResult
import org.breezyweather.sources.ipma.json.IpmaForecastResult
import org.breezyweather.sources.ipma.json.IpmaLocationResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class IpmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "ipma"
    override val name = "IPMA (${context.currentLocale.getCountryName("PT")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("pt")) {
            "https://www.ipma.pt/pt/siteinfo/index.html"
        } else {
            "https://www.ipma.pt/en/siteinfo/index.html"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(IPMA_BASE_URL)
            .build()
            .create(IpmaApi::class.java)
    }

    private val weatherAttribution = "Instituto Português do Mar e da Atmosfera"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.ipma.pt/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("PT", ignoreCase = true)
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
        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }
        if (globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(globalIdLocal).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts().onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(IpmaAlertResult())
            }
        } else {
            Observable.just(IpmaAlertResult())
        }

        return Observable.zip(forecast, alerts) {
                forecastResult: List<IpmaForecastResult>,
                alertResult: IpmaAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, location, forecastResult.filter { it.idPeriodo == 24 })
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, forecastResult.filter { it.idPeriodo != 24 })
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getDailyForecast(
        context: Context,
        location: Location,
        forecastResult: List<IpmaForecastResult>,
    ): List<DailyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = location.timeZone

        return forecastResult.mapIndexed { index, result ->
            DailyWrapper(
                date = formatter.parse(result.dataPrev)!!,
                day = HalfDayWrapper(
                    weatherText = getWeatherText(context, result.idTipoTempo),
                    weatherCode = getWeatherCode(result.idTipoTempo),
                    temperature = result.tMax?.toDoubleOrNull()?.let { tMax ->
                        TemperatureWrapper(temperature = tMax)
                    },
                    precipitationProbability = PrecipitationProbability(
                        total = result.probabilidadePrecipita?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = getWindDegree(result.ddVento)
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(context, result.idTipoTempo),
                    weatherCode = getWeatherCode(result.idTipoTempo),
                    temperature = forecastResult.elementAtOrNull(index + 1)
                        ?.tMin?.toDoubleOrNull() // Get next day min temperature to have overnight temp
                        ?.let { tMin -> TemperatureWrapper(temperature = tMin) },
                    precipitationProbability = PrecipitationProbability(
                        total = result.probabilidadePrecipita?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = getWindDegree(result.ddVento)
                    )
                ),
                uV = UV(
                    index = result.iUv?.toDoubleOrNull()
                ),
                relativeHumidity = result.hR?.toDoubleOrNull()?.let { hr -> DailyRelativeHumidity(average = hr) }
            )
        }
    }

    private fun getHourlyForecast(
        context: Context,
        location: Location,
        forecastResult: List<IpmaForecastResult>,
    ): List<HourlyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        var lastPrecipitationProbability: Double? = null

        return forecastResult.map {
            HourlyWrapper(
                date = formatter.parse(it.dataPrev)!!,
                weatherText = getWeatherText(context, it.idTipoTempo),
                weatherCode = getWeatherCode(it.idTipoTempo),
                temperature = TemperatureWrapper(
                    temperature = it.tMed?.toDoubleOrNull(),
                    feelsLike = it.utci?.toDoubleOrNull()
                ),
                precipitationProbability = PrecipitationProbability(
                    total = if (it.probabilidadePrecipita != "-99.0") {
                        it.probabilidadePrecipita?.toDoubleOrNull()
                    } else {
                        lastPrecipitationProbability
                    }
                ),
                wind = Wind(
                    degree = getWindDegree(it.ddVento),
                    speed = it.ffVento?.toDoubleOrNull()?.div(3.6)
                ),
                relativeHumidity = it.hR?.toDoubleOrNull()
            ).also { hourly ->
                if (it.probabilidadePrecipita != "-99.0") {
                    lastPrecipitationProbability = it.probabilidadePrecipita?.toDoubleOrNull()
                }
            }
        }
    }

    private fun getAlertList(
        location: Location,
        alertResult: IpmaAlertResult,
    ): List<Alert> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = location.timeZone
        val alertList = mutableListOf<Alert>()
        alertResult.data?.forEach {
            val id = "ipma"
            var severity: AlertSeverity
            if (it.idAreaAviso == location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }) {
                if (it.awarenessLevelID != "green") {
                    severity = when (it.awarenessLevelID) {
                        "yellow" -> AlertSeverity.MODERATE
                        "orange" -> AlertSeverity.SEVERE
                        "red" -> AlertSeverity.EXTREME
                        else -> AlertSeverity.UNKNOWN
                    }
                    alertList.add(
                        Alert(
                            alertId = "${it.awarenessTypeName} ${it.startTime}",
                            startDate = formatter.parse(it.startTime)!!,
                            endDate = formatter.parse(it.endTime)!!,
                            headline = it.awarenessTypeName,
                            description = it.text,
                            source = "Instituto Português do Mar e da Atmosfera",
                            severity = severity,
                            color = Alert.colorFromSeverity(severity)
                        )
                    )
                }
            }
        }
        return alertList
    }

    // Source: https://www.ipma.pt/opencms/bin/file.data/weathertypes.json
    private fun getWeatherText(
        context: Context,
        code: Int?,
    ): String? {
        return when (code) {
            1 -> context.getString(R.string.common_weather_text_clear_sky)
            2, 3, 25 -> context.getString(R.string.common_weather_text_partly_cloudy)
            4, 5, 24, 27 -> context.getString(R.string.common_weather_text_cloudy)
            6, 9 -> context.getString(R.string.common_weather_text_rain_showers)
            7 -> context.getString(R.string.common_weather_text_rain_showers_light)
            8, 11 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
            10, 13 -> context.getString(R.string.common_weather_text_rain_light)
            12 -> context.getString(R.string.common_weather_text_rain)
            14 -> context.getString(R.string.common_weather_text_rain_heavy)
            15 -> context.getString(R.string.common_weather_text_drizzle)
            16 -> context.getString(R.string.common_weather_text_mist)
            17, 26 -> context.getString(R.string.common_weather_text_fog)
            18 -> context.getString(R.string.common_weather_text_snow)
            19, 20, 23 -> context.getString(R.string.weather_kind_thunderstorm)
            21 -> context.getString(R.string.weather_kind_hail)
            22 -> context.getString(R.string.common_weather_text_frost)
            28 -> context.getString(R.string.common_weather_text_snow_showers)
            29, 30 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            else -> null
        }
    }

    private fun getWeatherCode(
        code: Int?,
    ): WeatherCode? {
        return when (code) {
            1 -> WeatherCode.CLEAR
            2, 3, 25 -> WeatherCode.PARTLY_CLOUDY
            4, 5, 24, 27 -> WeatherCode.CLOUDY
            6, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> WeatherCode.RAIN
            16, 17, 26 -> WeatherCode.FOG
            18, 22, 28 -> WeatherCode.SNOW
            19, 20, 23 -> WeatherCode.THUNDERSTORM
            21 -> WeatherCode.HAIL
            29, 30 -> WeatherCode.SLEET
            else -> null
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val districts = mApi.getDistricts()
        val locations = mApi.getLocations()
        return Observable.zip(districts, locations) {
                districtResult: List<IpmaDistrictResult>,
                locationResult: List<IpmaLocationResult>,
            ->
            convertLocation(latitude, longitude, districtResult, locationResult)
        }
    }

    private fun convertLocation(
        latitude: Double,
        longitude: Double,
        districts: List<IpmaDistrictResult>,
        locations: List<IpmaLocationResult>,
    ): List<LocationAddressInfo> {
        val locationList = mutableListOf<LocationAddressInfo>()
        val locationMap = mutableMapOf<String, LatLng>()
        locations.mapIndexed { i, loc ->
            i.toString() to LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())
        }
        LatLng(latitude, longitude).getNearestLocation(locationMap, 50000.0)?.let {
            val nearestLocation = locations[it.toInt()]
            var districtName: String? = null
            districts.forEach { d ->
                if (d.idRegiao == nearestLocation.idRegiao && d.idDistrito == nearestLocation.idDistrito) {
                    districtName = d.nome
                }
            }
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = when (nearestLocation.idRegiao) {
                        2 -> "Atlantic/Madeira"
                        3 -> "Atlantic/Azores"
                        else -> "Europe/Lisbon"
                    },
                    countryCode = "PT",
                    admin1 = when (nearestLocation.idRegiao) {
                        2 -> "Madeira"
                        3 -> "Azores"
                        else -> districtName
                    },
                    admin2 = when (nearestLocation.idRegiao) {
                        2, 3 -> districtName
                        else -> null
                    },
                    city = nearestLocation.local
                )
            )
        }
        return locationList
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val globalIdLocal = location.parameters.getOrElse(id) { null }?.getOrElse("globalIdLocal") { null }
        val idAreaAviso = location.parameters.getOrElse(id) { null }?.getOrElse("idAreaAviso") { null }

        return globalIdLocal.isNullOrEmpty() || idAreaAviso.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocations().map {
            convertLocation(location, it)
        }
    }

    private fun convertLocation(
        location: Location,
        locations: List<IpmaLocationResult>,
    ): Map<String, String> {
        val locationMap = mutableMapOf<String, LatLng>()
        locations.forEachIndexed { i, loc ->
            locationMap[i.toString()] = LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())
        }
        LatLng(location.latitude, location.longitude).getNearestLocation(locationMap, 50000.0)?.let {
            val nearestLocation = locations[it.toInt()]
            return mapOf(
                "globalIdLocal" to nearestLocation.globalIdLocal.toString(),
                "idAreaAviso" to nearestLocation.idAreaAviso.toString()
            )
        }
        // No forecast location within 50km
        throw InvalidLocationException()
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val IPMA_BASE_URL = "https://api.ipma.pt/"
    }
}
