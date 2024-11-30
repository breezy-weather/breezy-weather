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
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.aemet.json.AemetCurrentResult
import org.breezyweather.sources.aemet.json.AemetDailyResult
import org.breezyweather.sources.aemet.json.AemetHourlyResult
import org.breezyweather.sources.aemet.json.AemetNormalsResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.ifEmpty

class AemetService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, LocationParametersSource, ConfigurableSource {

    override val id = "aemet"
    override val name = "AEMET"
    override val privacyPolicyUrl = "https://www.aemet.es/es/nota_legal"

    override val color = Color.rgb(58, 133, 202)
    override val weatherAttribution = "Agencia Estatal de Meteorología"

    private val mApi by lazy {
        client
            .baseUrl(AEMET_BASE_URL)
            .build()
            .create(AemetApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_NORMALS
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("ES", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }
        if (municipio.isNullOrEmpty() || estacion.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }
        } else {
            Observable.just(emptyList())
        }

        val normals = if (!ignoreFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_NORMALS)
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    emptyList()
                }
            }
        } else {
            Observable.just(emptyList())
        }

        val daily = mApi.getForecastUrl(
            apiKey = apiKey,
            range = "diaria",
            municipio = municipio
        ).map {
            val path = it.datos?.substringAfter(AEMET_BASE_URL)
            if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
            mApi.getDaily(
                apiKey = apiKey,
                path = path
            ).blockingFirst()
        }

        val hourly = mApi.getForecastUrl(
            apiKey = apiKey,
            range = "horaria",
            municipio = municipio
        ).map {
            val path = it.datos?.substringAfter(AEMET_BASE_URL)
            if (path.isNullOrEmpty()) throw InvalidOrIncompleteDataException()
            mApi.getHourly(
                apiKey = apiKey,
                path = path
            ).blockingFirst()
        }

        return Observable.zip(current, daily, hourly, normals) {
                currentResult: List<AemetCurrentResult>,
                dailyResult: List<AemetDailyResult>,
                hourlyResult: List<AemetHourlyResult>,
                normalsResult: List<AemetNormalsResult>,
            ->
            convert(
                context = context,
                location = location,
                currentResult = currentResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                normalsResult = normalsResult,
                failedFeatures = failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        val municipio = location.parameters.getOrElse(id) { null }?.getOrElse("municipio") { null }
        val estacion = location.parameters.getOrElse(id) { null }?.getOrElse("estacion") { null }
        if (municipio.isNullOrEmpty() || estacion.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    throw InvalidOrIncompleteDataException()
                }
            }
        } else {
            Observable.just(emptyList())
        }

        val normals = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_NORMALS)
                        Observable.just(emptyList())
                    }.blockingFirst()
                } else {
                    throw InvalidOrIncompleteDataException()
                }
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, normals) {
                currentResult: List<AemetCurrentResult>,
                normalsResult: List<AemetNormalsResult>,
            ->
            convertSecondary(
                location = location,
                currentResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                normalsResult = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
                    normalsResult
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
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
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
        if (municipio == null) {
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

    companion object {
        private const val AEMET_BASE_URL = "https://opendata.aemet.es/opendata/"
    }
}
