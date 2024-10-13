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
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
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
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.PreferencesParametersSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.ui.composables.AlertDialogNoPadding
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import retrofit2.HttpException
import retrofit2.Retrofit
import java.text.Collator
import javax.inject.Inject
import javax.inject.Named

class OpenMeteoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, LocationSearchSource,
    ConfigurableSource, PreferencesParametersSource {

    override val id = "openmeteo"
    override val name = "Open-Meteo"
    override val privacyPolicyUrl = "https://open-meteo.com/en/terms#privacy"

    override val color = Color.rgb(255, 136, 0)
    override val weatherAttribution = "Open-Meteo (CC BY 4.0)"
    override val locationSearchAttribution = "Open-Meteo (CC BY 4.0) / GeoNames"

    private val mForecastApi: OpenMeteoForecastApi
        get() {
            return client
                .baseUrl(forecastInstance)
                .build()
                .create(OpenMeteoForecastApi::class.java)
        }
    private val mGeocodingApi: OpenMeteoGeocodingApi
        get() {
            return client
                .baseUrl(geocodingInstance)
                .build()
                .create(OpenMeteoGeocodingApi::class.java)
        }
    private val mAirQualityApi: OpenMeteoAirQualityApi
        get() {
            return client
                .baseUrl(airQualityInstance)
                .build()
                .create(OpenMeteoAirQualityApi::class.java)
        }

    val airQualityHourly = arrayOf(
        "pm10",
        "pm2_5",
        "carbon_monoxide",
        "nitrogen_dioxide",
        "sulphur_dioxide",
        "ozone"
    )
    val pollenHourly = arrayOf(
        "alder_pollen",
        "birch_pollen",
        "grass_pollen",
        "mugwort_pollen",
        "olive_pollen",
        "ragweed_pollen"
    )
    val minutely = arrayOf(
        //"precipitation_probability",
        "precipitation"
    )

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_POLLEN,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val daily = arrayOf(
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "sunrise",
            "sunset",
            "sunshine_duration",
            "uv_index_max"
        )
        val hourly = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "precipitation_probability",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "is_day",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )
        val current = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )
        val weather = mForecastApi.getWeather(
            location.latitude,
            location.longitude,
            getWeatherModels(location).joinToString(",") { it.id },
            daily.joinToString(","),
            hourly.joinToString(","),
            if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                minutely.joinToString(",")
            } else "",
            current.joinToString(","),
            forecastDays = 16,
            pastDays = 1,
            windspeedUnit = "ms"
        )

        val aqi = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) ||
            !ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)
        ) {
            val airQualityPollenHourly =
                (if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityHourly
                } else arrayOf()) +
                (if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
                    pollenHourly
                } else arrayOf())
            mAirQualityApi.getAirQuality(
                location.latitude,
                location.longitude,
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1,
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoAirQualityResult())
            }
        }
        return Observable.zip(
            weather.onErrorResumeNext {
                if (it is HttpException &&
                    it.response()?.errorBody()?.string()
                        ?.contains("No data is available for this location") == true) {
                    // Happens when user choose a model that doesn’t cover their location
                    Observable.error(InvalidLocationException())
                } else {
                    Observable.error(it)
                }
            },
            aqi
        ) {
            openMeteoWeatherResult: OpenMeteoWeatherResult,
            openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convert(
                context,
                location,
                openMeteoWeatherResult,
                openMeteoAirQualityResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_POLLEN,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override val airQualityAttribution =
        "Open-Meteo (CC BY 4.0) / METEO FRANCE, Institut national de l'environnement industriel et des risques (Ineris), Aarhus University, Norwegian Meteorological Institute (MET Norway), Jülich Institut für Energie- und Klimaforschung (IEK), Institute of Environmental Protection – National Research Institute (IEP-NRI), Koninklijk Nederlands Meteorologisch Instituut (KNMI), Nederlandse Organisatie voor toegepast-natuurwetenschappelijk onderzoek (TNO), Swedish Meteorological and Hydrological Institute (SMHI), Finnish Meteorological Institute (FMI), Italian National Agency for New Technologies, Energy and Sustainable Economic Development (ENEA) and Barcelona Supercomputing Center (BSC) (2022): CAMS European air quality forecasts, ENSEMBLE data. Copernicus Atmosphere Monitoring Service (CAMS) Atmosphere Data Store (ADS). (Updated twice daily)."
    override val pollenAttribution = airQualityAttribution
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val weather = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mForecastApi.getWeather(
                location.latitude,
                location.longitude,
                getWeatherModels(location).joinToString(",") { it.id },
                "",
                "",
                minutely.joinToString(","),
                "",
                forecastDays = 2, // In case current + 2 hours overlap two days
                pastDays = 0,
                windspeedUnit = "ms"
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoWeatherResult())
            }
        }

        val aqi = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) ||
            requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
            val airQualityPollenHourly =
                (if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) airQualityHourly else emptyArray()) +
                    (if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) pollenHourly else emptyArray())
            mAirQualityApi.getAirQuality(
                location.latitude,
                location.longitude,
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1,
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenMeteoAirQualityResult())
            }
        }

        return Observable.zip(
            weather.onErrorResumeNext {
                if (it is HttpException &&
                    it.response()?.errorBody()?.string()
                        ?.contains("No data is available for this location") == true) {
                    // Happens when user choose a model that doesn’t cover their location
                    Observable.error(InvalidLocationException())
                } else {
                    Observable.error(it)
                }
            },
            aqi
        ) { openMeteoWeatherResult: OpenMeteoWeatherResult,
            openMeteoAirQualityResult: OpenMeteoAirQualityResult
            ->
            convertSecondary(
                openMeteoWeatherResult.minutelyFifteen,
                openMeteoAirQualityResult.hourly,
                requestedFeatures
            )
        }
    }

    // Location
    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
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
                results.results.mapNotNull {
                    convert(it)
                }
            }
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var forecastInstance: String
        set(value) {
            config.edit().putString("forecast_instance", value).apply()
        }
        get() = config.getString("forecast_instance", null) ?: OPEN_METEO_FORECAST_BASE_URL
    private var airQualityInstance: String
        set(value) {
            config.edit().putString("air_quality_instance", value).apply()
        }
        get() = config.getString("air_quality_instance", null) ?: OPEN_METEO_AIR_QUALITY_BASE_URL
    private var geocodingInstance: String
        set(value) {
            config.edit().putString("geocoding_instance", value).apply()
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
                content = forecastInstance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    forecastInstance = it
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_meteo_instance_air_quality,
                summary = { _, content ->
                    content.ifEmpty {
                        OPEN_METEO_AIR_QUALITY_BASE_URL
                    }
                },
                content = airQualityInstance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    airQualityInstance = it
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_meteo_instance_geocoding,
                summary = { _, content ->
                    content.ifEmpty {
                        OPEN_METEO_GEOCODING_BASE_URL
                    }
                },
                content = geocodingInstance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    geocodingInstance = it
                }
            )
        )
    }

    // Per-location preferences
    override fun hasPreferencesScreen(
        location: Location,
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        return features.isEmpty() ||
                features.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)
    }

    private fun getWeatherModels(
        location: Location
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
        val enabled: Boolean
    )

    @Composable
    override fun PerLocationPreferences(
        context: Context,
        location: Location,
        features: List<SecondaryWeatherSourceFeature>,
        onSave: (Map<String, String>) -> Unit
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
                .sortedWith { ws1, ws2 -> // Sort by name because there are now a lot of sources
                    Collator.getInstance(
                        context.currentLocale
                    ).compare(ws1.model.getName(context), ws2.model.getName(context))
                }
                .joinToString(context.getString(R.string.comma_separator)) {
                    it.model.getName(context)
                },
            card = false,
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
                        style = MaterialTheme.typography.headlineSmall,
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
                                    model.model.incompatibleSources.forEach { incompatibleSource ->
                                        weatherModels.indexOfFirst { it.model.id == incompatibleSource }.let {
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
                            style = MaterialTheme.typography.labelLarge,
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
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
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

    companion object {
        private const val OPEN_METEO_AIR_QUALITY_BASE_URL =
            "https://air-quality-api.open-meteo.com/"
        private const val OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        private const val OPEN_METEO_FORECAST_BASE_URL = "https://api.open-meteo.com/"
    }
}
