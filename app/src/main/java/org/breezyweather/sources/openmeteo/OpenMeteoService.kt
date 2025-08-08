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

package org.breezyweather.sources.openmeteo

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import kotlinx.collections.immutable.ImmutableList
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.PreferencesParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoLocationResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.settings.preference.composables.PreferenceView
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import retrofit2.HttpException
import retrofit2.Retrofit
import java.text.Collator
import javax.inject.Inject
import javax.inject.Named

class OpenMeteoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationSearchSource, ConfigurableSource, PreferencesParametersSource {

    override val id = "openmeteo"
    override val name = "Open-Meteo"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://open-meteo.com/en/terms#privacy"

    override val locationSearchAttribution = "Open-Meteo (CC BY 4.0) • GeoNames"

    private val mForecastApi: OpenMeteoForecastApi
        get() {
            return client
                .baseUrl(forecastInstance!!)
                .build()
                .create(OpenMeteoForecastApi::class.java)
        }
    private val mGeocodingApi: OpenMeteoGeocodingApi
        get() {
            return client
                .baseUrl(geocodingInstance!!)
                .build()
                .create(OpenMeteoGeocodingApi::class.java)
        }
    private val mAirQualityApi: OpenMeteoAirQualityApi
        get() {
            return client
                .baseUrl(airQualityInstance!!)
                .build()
                .create(OpenMeteoAirQualityApi::class.java)
        }

    private val weatherAttribution = "Open-Meteo (CC BY 4.0)"
    private val airQualityAttribution = "Open-Meteo (CC BY 4.0) • CAMS ENSEMBLE data provider"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to airQualityAttribution,
        SourceFeature.POLLEN to airQualityAttribution,
        SourceFeature.MINUTELY to weatherAttribution
    )
    override val attributionLinks = mapOf(
        name to "https://open-meteo.com/",
        "CAMS ENSEMBLE data provider" to "https://confluence.ecmwf.int/display/CKB/" +
            "CAMS+Regional%3A+European+air+quality+analysis+and+forecast+data+documentation/" +
            "#CAMSRegional:Europeanairqualityanalysisandforecastdatadocumentation-" +
            "Howtoacknowledge,citeandrefertothedata"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.POLLEN -> COPERNICUS_POLLEN_BBOX.contains(LatLng(location.latitude, location.longitude))
            else -> true
        }
    }
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures ||
            SourceFeature.MINUTELY in requestedFeatures ||
            SourceFeature.CURRENT in requestedFeatures
        ) {
            val daily = arrayOf(
                "temperature_2m_max",
                "temperature_2m_min",
                "apparent_temperature_max",
                "apparent_temperature_min",
                "sunshine_duration",
                "uv_index_max",
                "relative_humidity_2m_mean",
                "relative_humidity_2m_max",
                "relative_humidity_2m_min",
                "dew_point_2m_mean",
                "dew_point_2m_max",
                "dew_point_2m_min",
                "pressure_msl_mean",
                "pressure_msl_max",
                "pressure_msl_min",
                "cloud_cover_mean",
                "cloud_cover_max",
                "cloud_cover_min",
                "visibility_mean",
                "visibility_max",
                "visibility_min"
            )
            val hourly = arrayOf(
                "temperature_2m",
                "apparent_temperature",
                "precipitation_probability",
                "precipitation",
                "rain",
                "showers",
                "snowfall",
                "weather_code",
                "wind_speed_10m",
                "wind_direction_10m",
                "wind_gusts_10m",
                "uv_index",
                "is_day",
                "relative_humidity_2m",
                "dew_point_2m",
                "pressure_msl",
                "cloud_cover",
                "visibility"
            )
            val current = arrayOf(
                "temperature_2m",
                "apparent_temperature",
                "weather_code",
                "wind_speed_10m",
                "wind_direction_10m",
                "wind_gusts_10m",
                "uv_index",
                "relative_humidity_2m",
                "dew_point_2m",
                "pressure_msl",
                "cloud_cover",
                "visibility"
            )
            val minutely = arrayOf(
                // "precipitation_probability",
                "precipitation"
            )

            mForecastApi.getWeather(
                location.latitude,
                location.longitude,
                getWeatherModels(location).joinToString(",") { it.id },
                if (SourceFeature.FORECAST in requestedFeatures) {
                    daily.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.FORECAST in requestedFeatures) {
                    hourly.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.MINUTELY in requestedFeatures) {
                    minutely.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.CURRENT in requestedFeatures) {
                    current.joinToString(",")
                } else {
                    ""
                },
                forecastDays = 16,
                pastDays = 1,
                windspeedUnit = "ms"
            ).onErrorResumeNext {
                if (it is HttpException &&
                    it.response()?.errorBody()?.string()
                        ?.contains("No data is available for this location") == true
                ) {
                    // Happens when user choose a model that doesn’t cover their location
                    Observable.error(InvalidLocationException())
                } else {
                    if (SourceFeature.FORECAST in requestedFeatures) {
                        failedFeatures[SourceFeature.FORECAST] = it
                    }
                    if (SourceFeature.MINUTELY in requestedFeatures) {
                        failedFeatures[SourceFeature.MINUTELY] = it
                    }
                    if (SourceFeature.CURRENT in requestedFeatures) {
                        failedFeatures[SourceFeature.CURRENT] = it
                    }
                    Observable.just(OpenMeteoWeatherResult())
                }
            }
        } else {
            Observable.just(OpenMeteoWeatherResult())
        }

        val aqi = if (SourceFeature.AIR_QUALITY in requestedFeatures ||
            SourceFeature.POLLEN in requestedFeatures
        ) {
            val airQualityHourly = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                arrayOf(
                    "pm10",
                    "pm2_5",
                    "carbon_monoxide",
                    "nitrogen_dioxide",
                    "sulphur_dioxide",
                    "ozone"
                )
            } else {
                arrayOf()
            }
            val pollenHourly = if (SourceFeature.POLLEN in requestedFeatures) {
                arrayOf(
                    "alder_pollen",
                    "birch_pollen",
                    "grass_pollen",
                    "mugwort_pollen",
                    "olive_pollen",
                    "ragweed_pollen"
                )
            } else {
                arrayOf()
            }
            val airQualityPollenHourly = airQualityHourly + pollenHourly
            mAirQualityApi.getAirQuality(
                location.latitude,
                location.longitude,
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1
            ).onErrorResumeNext {
                if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    failedFeatures[SourceFeature.AIR_QUALITY] = it
                }
                if (SourceFeature.POLLEN in requestedFeatures) {
                    failedFeatures[SourceFeature.POLLEN] = it
                }
                Observable.just(OpenMeteoAirQualityResult())
            }
        } else {
            Observable.just(OpenMeteoAirQualityResult())
        }
        return Observable.zip(
            weather,
            aqi
        ) { weatherResult: OpenMeteoWeatherResult, airQualityResult: OpenMeteoAirQualityResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(weatherResult.daily, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(context, weatherResult.hourly)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(weatherResult.current, context)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    getAirQuality(airQualityResult.hourly)
                } else {
                    null
                },
                pollen = if (SourceFeature.POLLEN in requestedFeatures) {
                    getPollen(airQualityResult.hourly)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(weatherResult.minutelyFifteen)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Location
    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        return mGeocodingApi.getLocations(
            query,
            count = 20,
            context.currentLocale.code
        ).map { results ->
            if (results.results == null) {
                if (results.generationtimeMs != null && results.generationtimeMs > 0.0) {
                    emptyList()
                } else {
                    throw LocationSearchException()
                }
            } else {
                results.results
                    .filter { !it.countryCode.isNullOrEmpty() }
                    .map { convertLocation(it) }
            }
        }
    }

    private fun convertLocation(
        result: OpenMeteoLocationResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            latitude = result.latitude,
            longitude = result.longitude,
            timeZoneId = result.timezone,
            country = result.country,
            countryCode = result.countryCode!!,
            admin1 = result.admin1,
            admin2 = result.admin2,
            admin3 = result.admin3,
            admin4 = result.admin4,
            city = result.name,
            cityCode = result.id.toString()
        )
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var forecastInstance: String?
        set(value) {
            value?.let {
                config.edit().putString("forecast_instance", it).apply()
            } ?: config.edit().remove("forecast_instance").apply()
        }
        get() = config.getString("forecast_instance", null) ?: OPEN_METEO_FORECAST_BASE_URL
    private var airQualityInstance: String?
        set(value) {
            value?.let {
                config.edit().putString("air_quality_instance", it).apply()
            } ?: config.edit().remove("air_quality_instance").apply()
        }
        get() = config.getString("air_quality_instance", null) ?: OPEN_METEO_AIR_QUALITY_BASE_URL
    private var geocodingInstance: String?
        set(value) {
            value?.let {
                config.edit().putString("geocoding_instance", it).apply()
            } ?: config.edit().remove("geocoding_instance").apply()
        }
        get() = config.getString("geocoding_instance", null) ?: OPEN_METEO_GEOCODING_BASE_URL

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_meteo_instance_forecast,
                summary = { _, content ->
                    content.ifEmpty {
                        OPEN_METEO_FORECAST_BASE_URL
                    }
                },
                content = if (forecastInstance != OPEN_METEO_FORECAST_BASE_URL) forecastInstance else null,
                placeholder = OPEN_METEO_FORECAST_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    forecastInstance = if (it == OPEN_METEO_FORECAST_BASE_URL) null else it.ifEmpty { null }
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_meteo_instance_air_quality,
                summary = { _, content ->
                    content.ifEmpty {
                        OPEN_METEO_AIR_QUALITY_BASE_URL
                    }
                },
                content = if (airQualityInstance != OPEN_METEO_AIR_QUALITY_BASE_URL) airQualityInstance else null,
                placeholder = OPEN_METEO_AIR_QUALITY_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    airQualityInstance = if (it == OPEN_METEO_AIR_QUALITY_BASE_URL) null else it.ifEmpty { null }
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_meteo_instance_geocoding,
                summary = { _, content ->
                    content.ifEmpty {
                        OPEN_METEO_GEOCODING_BASE_URL
                    }
                },
                content = if (geocodingInstance != OPEN_METEO_GEOCODING_BASE_URL) geocodingInstance else null,
                placeholder = OPEN_METEO_GEOCODING_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    geocodingInstance = if (it == OPEN_METEO_GEOCODING_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    // Per-location preferences
    override fun hasPreferencesScreen(
        location: Location,
        features: List<SourceFeature>,
    ): Boolean {
        return SourceFeature.FORECAST in features ||
            SourceFeature.CURRENT in features ||
            SourceFeature.MINUTELY in features
    }

    private fun getWeatherModels(
        location: Location,
    ): List<OpenMeteoWeatherModel> {
        return location.parameters
            .getOrElse(id) { null }?.getOrElse("weatherModels") { null }
            ?.split(",")
            ?.mapNotNull {
                OpenMeteoWeatherModel.getInstance(it)
            } ?: listOf(OpenMeteoWeatherModel.BEST_MATCH)
    }

    data class WeatherModelStatus(
        val model: OpenMeteoWeatherModel,
        val enabled: Boolean,
    )

    @Composable
    override fun PerLocationPreferences(
        context: Context,
        location: Location,
        features: ImmutableList<SourceFeature>,
        onSave: (Map<String, String>) -> Unit,
    ) {
        val dialogModelsOpenState = remember { mutableStateOf(false) }
        val changedWeatherModelsState = remember { mutableStateOf(false) }
        val weatherModels = remember {
            mutableStateListOf<WeatherModelStatus>().apply {
                val cv = getWeatherModels(location)
                addAll(
                    OpenMeteoWeatherModel.entries.map {
                        WeatherModelStatus(
                            model = it,
                            enabled = cv.contains(it)
                        )
                    }
                )
            }
        }

        PreferenceView(
            title = stringResource(R.string.settings_weather_source_open_meteo_weather_models),
            summary = weatherModels
                .filter { it.enabled }
                .sortedWith { ws1, ws2 ->
                    // Sort by name because there are now a lot of sources
                    Collator.getInstance(
                        context.currentLocale
                    ).compare(ws1.model.getName(context), ws2.model.getName(context))
                }
                .joinToString(context.getString(R.string.comma_separator)) {
                    it.model.getName(context)
                },
            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor)
        ) {
            dialogModelsOpenState.value = true
        }

        if (dialogModelsOpenState.value) {
            AlertDialogNoPadding(
                title = {
                    Text(
                        text = stringResource(R.string.settings_weather_source_open_meteo_weather_models),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(
                            weatherModels,
                            { key ->
                                // Doesn’t update otherwise
                                key.hashCode()
                            }
                        ) { model ->
                            if (model.model.id.endsWith("_seamless")) {
                                HorizontalDivider()
                            }
                            SwitchPreferenceView(
                                title = model.model.getName(context),
                                summary = { context, _ -> model.model.getDescription(context) },
                                checked = model.enabled,
                                card = false,
                                colors = ListItemDefaults.colors(AlertDialogDefaults.containerColor)
                            ) { checked ->
                                if (checked) {
                                    OpenMeteoWeatherModel
                                        .entries
                                        .filter { id != model.model.id }
                                        .forEach { incompatibleSource ->
                                            weatherModels.indexOfFirst { it.model.id == incompatibleSource.id }.let {
                                                if (it != -1) {
                                                    weatherModels[it] = weatherModels[it].copy(enabled = false)
                                                }
                                            }
                                        }
                                    weatherModels.indexOfFirst { it.model == model.model }.let {
                                        if (it != -1) {
                                            weatherModels[it] = weatherModels[it].copy(enabled = true)
                                        }
                                    }
                                } else {
                                    weatherModels.indexOfFirst { it.model == model.model }.let {
                                        if (it != -1) {
                                            weatherModels[it] = weatherModels[it].copy(enabled = false)
                                        }
                                    }
                                }
                                changedWeatherModelsState.value = true
                            }
                            if (model.model == OpenMeteoWeatherModel.BEST_MATCH) {
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (changedWeatherModelsState.value) {
                                onSave(
                                    mapOf(
                                        "weatherModels" to weatherModels.filter { it.enabled }
                                            .joinToString(",") { it.model.id }
                                            .ifEmpty { OpenMeteoWeatherModel.BEST_MATCH.id }
                                    )
                                )
                            }
                            dialogModelsOpenState.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_confirm),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val cv = getWeatherModels(location)
                            weatherModels.forEachIndexed { key, value ->
                                weatherModels[key] = value.copy(
                                    enabled = cv.contains(value.model)
                                )
                            }
                            dialogModelsOpenState.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                onDismissRequest = {
                    val cv = getWeatherModels(location)
                    weatherModels.forEachIndexed { key, value ->
                        weatherModels[key] = value.copy(
                            enabled = cv.contains(value.model)
                        )
                    }
                    dialogModelsOpenState.value = false
                }
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val OPEN_METEO_AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
        private const val OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        private const val OPEN_METEO_FORECAST_BASE_URL = "https://api.open-meteo.com/"

        // Coverage area of CAMS European air quality forecasts:
        // Europe (west boundary=25.0° W, east=45.0° E, south=30.0° N, north=72.0°)
        // Source: https://ads.atmosphere.copernicus.eu/datasets/cams-europe-air-quality-forecasts?tab=overview
        val COPERNICUS_POLLEN_BBOX = LatLngBounds(
            LatLng(30.0, -25.0),
            LatLng(72.0, 45.0)
        )
    }
}
