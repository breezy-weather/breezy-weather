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

package org.breezyweather.sources.aemet

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.aemet.json.AemetCurrentResult
import org.breezyweather.sources.aemet.json.AemetDailyResult
import org.breezyweather.sources.aemet.json.AemetHourlyResult
import org.breezyweather.sources.aemet.json.AemetNormalsResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.ifEmpty

class AemetService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource, ConfigurableSource {

    override val id = "aemet"
    override val name = "AEMET (${Locale(context.currentLocale.code, "ES").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.aemet.es/es/nota_legal"

    override val color = Color.rgb(58, 133, 202)

    private val mApi by lazy {
        client
            .baseUrl(AEMET_BASE_URL)
            .build()
            .create(AemetApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    private val weatherAttribution = "Agencia Estatal de Meteorología"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("ES", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }
        if (municipio.isNullOrEmpty() || estacion.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentUrl(
                apiKey = apiKey,
                estacion = estacion
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (!path.isNullOrEmpty()) {
                    mApi.getCurrent(
                        apiKey = apiKey,
                        path = path
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.CURRENT] = it
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormalsUrl(
                apiKey = apiKey,
                estacion = estacion
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (!path.isNullOrEmpty()) {
                    mApi.getNormals(
                        apiKey = apiKey,
                        path = path
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.NORMALS] = it
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecastUrl(
                apiKey = apiKey,
                range = "diaria",
                municipio = municipio
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
                mApi.getDaily(
                    apiKey = apiKey,
                    path = path
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.FORECAST] = it
                    Observable.just(emptyList())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecastUrl(
                apiKey = apiKey,
                range = "horaria",
                municipio = municipio
            ).map {
                val path = it.datos?.substringAfter(AEMET_BASE_URL)
                if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
                mApi.getHourly(
                    apiKey = apiKey,
                    path = path
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.FORECAST] = it
                    Observable.just(emptyList())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, normals) {
                currentResult: List<AemetCurrentResult>,
                dailyResult: List<AemetDailyResult>,
                hourlyResult: List<AemetHourlyResult>,
                normalsResult: List<AemetNormalsResult>,
            ->
            val sunMap = getSunMap(location, hourlyResult)
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, location, dailyResult, sunMap)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, location, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(location, normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }

        return municipio.isNullOrEmpty() || estacion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val apiKey = getApiKeyOrDefault()

        val url = "https://www.aemet.es/es/eltiempo/prediccion/municipios/geolocalizacion?" +
            "munhome=no_mun&y=${location.latitude}&x=${location.longitude}"
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute().use { call ->
            if (call.isSuccessful) {
                call.body!!.string()
            } else {
                throw InvalidLocationException()
            }
        }
        val matchResult = Regex("""<a href='/es/eltiempo/prediccion/municipios/[^']+-id(\d+)'>""").find(response)
        val municipio = matchResult?.groups?.get(1)?.value
        if (municipio.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        val stationList = mApi.getStationsUrl(apiKey).map {
            val path = it.datos?.substringAfter(AEMET_BASE_URL)
            if (path.isNullOrEmpty()) throw InvalidLocationException()
            mApi.getStations(
                apiKey = apiKey,
                path = path
            ).blockingFirst()
        }

        return stationList.map {
            mapOf(
                "municipio" to municipio,
                "estacion" to convert(location, it)
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.AEMET_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_aemet_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val AEMET_BASE_URL = "https://opendata.aemet.es/opendata/"
    }
}
