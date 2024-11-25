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

package org.breezyweather.sources.cwa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.cwa.json.CwaAirQualityResult
import org.breezyweather.sources.cwa.json.CwaAlertResult
import org.breezyweather.sources.cwa.json.CwaAssistantResult
import org.breezyweather.sources.cwa.json.CwaAstroResult
import org.breezyweather.sources.cwa.json.CwaCurrentResult
import org.breezyweather.sources.cwa.json.CwaForecastResult
import org.breezyweather.sources.cwa.json.CwaNormalsResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class CwaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(),
    MainWeatherSource,
    SecondaryWeatherSource,
    ReverseGeocodingSource,
    LocationParametersSource,
    ConfigurableSource {

    override val id = "cwa"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "中央氣象署"
                else -> "Central Weather Administration (CWA)"
            }
        }
    }
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://www.cwa.gov.tw/V8/C/private.html"
                else -> "https://www.cwa.gov.tw/V8/E/private.html"
            }
        }
    }

    // Color of CWA's logo
    // Source: https://www.cwa.gov.tw/V8/assets/img/logoBlue.svg
    override val color = Color.rgb(55, 74, 135)
    override val weatherAttribution = "中央氣象署"

    private val mApi by lazy {
        client
            .baseUrl(CWA_BASE_URL)
            .build()
            .create(CwaApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?,
    ): Boolean {
        return location.countryCode.equals("TW", ignoreCase = true)
    }

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<WeatherWrapper> {
        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // County Name and Township Code are retrieved upon reverse geocoding,
        // but not for user-selected locations. Since a few API calls require these,
        // we will make sure these parameters are available before proceeding.
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }
        if (stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty() ||
            !CWA_HOURLY_ENDPOINTS.containsKey(countyName) ||
            !CWA_DAILY_ENDPOINTS.containsKey(countyName) ||
            !CWA_ASSISTANT_ENDPOINTS.containsKey(countyName)
        ) {
            return Observable.error(InvalidLocationException())
        }

        val hourly = mApi.getForecast(
            apiKey = apiKey,
            endpoint = CWA_HOURLY_ENDPOINTS[countyName]!!,
            townshipName = townshipName
        )
        val daily = mApi.getForecast(
            apiKey = apiKey,
            endpoint = CWA_DAILY_ENDPOINTS[countyName]!!,
            townshipName = townshipName
        )

        val current = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                apiKey = apiKey,
                stationId = stationId
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(CwaCurrentResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaCurrentResult())
            }
        }

        // "Weather Assistant" provides human-written forecast summary on a county level.
        val assistant = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getAssistant(
                endpoint = CWA_ASSISTANT_ENDPOINTS[countyName]!!,
                apiKey = apiKey
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(CwaAssistantResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAssistantResult())
            }
        }

        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        pm2_5,
                        pm10,
                        o3,
                        no2,
                        so2,
                        co
                    }
                }",
                "variables":null
            }
            """,
            ""
        )

        val airQuality = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality(
                apiKey = apiKey,
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(CwaAirQualityResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAirQualityResult())
            }
        }

        // The sunrise/sunset, moonrise/moonset API calls require start and end dates.
        // We will cover 8 days including "today" in the calls.
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.ENGLISH)
        val timeFrom = formatter.format(now.time)
        now.add(Calendar.DATE, 8)
        val timeTo = formatter.format(now.time)
        now.add(Calendar.DATE, -8)

        val sun = mApi.getAstro(
            endpoint = SUN_ENDPOINT,
            apiKey = apiKey,
            countyName = countyName,
            parameter = SUN_PARAMETERS,
            timeFrom = timeFrom,
            timeTo = timeTo
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(CwaAstroResult())
            }
        }
        val moon = mApi.getAstro(
            endpoint = MOON_ENDPOINT,
            apiKey = apiKey,
            countyName = countyName,
            parameter = MOON_PARAMETERS,
            timeFrom = timeFrom,
            timeTo = timeTo
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(CwaAstroResult())
            }
        }

        // Temperature normals are only available at 26 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val station = getNearestStation(location, CWA_NORMALS_STATIONS)
        val normals = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS) && station != null) {
            mApi.getNormals(
                apiKey = apiKey,
                stationId = station,
                month = (now.get(Calendar.MONTH) + 1).toString()
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(CwaNormalsResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaNormalsResult())
            }
        }

        val alerts = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                apiKey = apiKey
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAlertResult())
            }
        }

        return Observable.zip(current, airQuality, daily, hourly, normals, alerts, sun, moon, assistant) {
                currentResult: CwaCurrentResult,
                airQualityResult: CwaAirQualityResult,
                dailyResult: CwaForecastResult,
                hourlyResult: CwaForecastResult,
                normalsResult: CwaNormalsResult,
                alertResult: CwaAlertResult,
                sunResult: CwaAstroResult,
                moonResult: CwaAstroResult,
                assistantResult: CwaAssistantResult,
            ->
            convert(
                currentResult = currentResult,
                airQualityResult = airQualityResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                normalsResult = normalsResult,
                alertResult = alertResult,
                sunResult = sunResult,
                moonResult = moonResult,
                assistantResult = assistantResult,
                location = location
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = "環境部"
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) ||
            !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_ALERT) ||
            !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_NORMALS) ||
            !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_CURRENT)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // County Name and Township Code are retrieved upon reverse geocoding,
        // but not for user-selected locations. Since a few API calls require these,
        // we will make sure these parameters are available before proceeding.
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }
        if (stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty() ||
            !CWA_ASSISTANT_ENDPOINTS.containsKey(countyName)
        ) {
            return Observable.error(InvalidLocationException())
        }

        val current = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                apiKey = apiKey,
                stationId = stationId
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaCurrentResult())
            }
        }

        // "Weather Assistant" provides human-written forecast summary on a county level.
        val assistant = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getAssistant(
                endpoint = CWA_ASSISTANT_ENDPOINTS[countyName]!!,
                apiKey = apiKey
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAssistantResult())
            }
        }

        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        pm2_5,
                        pm10,
                        o3,
                        no2,
                        so2,
                        co
                    }
                }",
                "variables":null
            }
            """,
            ""
        )

        val airQuality = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality(
                apiKey = apiKey,
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAirQualityResult())
            }
        }

        val alerts = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                apiKey = apiKey
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAlertResult())
            }
        }

        // Temperature normals are only available at 26 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val station = getNearestStation(location, CWA_NORMALS_STATIONS)
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.ENGLISH)
        val normals = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS) &&
            station != null
        ) {
            mApi.getNormals(
                apiKey = apiKey,
                stationId = station,
                month = (now.get(Calendar.MONTH) + 1).toString()
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaNormalsResult())
            }
        }

        return Observable.zip(current, assistant, airQuality, alerts, normals) {
                currentResult: CwaCurrentResult,
                assistantResult: CwaAssistantResult,
                airQualityResult: CwaAirQualityResult,
                alertResult: CwaAlertResult,
                normalsResult: CwaNormalsResult,
            ->
            convertSecondary(
                currentResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                assistantResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
                    assistantResult
                } else {
                    null
                },
                airQualityResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityResult
                } else {
                    null
                },
                alertResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    alertResult
                } else {
                    null
                },
                normalsResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
                    normalsResult
                } else {
                    null
                },
                location = location
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        station{
                            stationId
                        },
                        town{
                            ctyName,
                            townCode,
                            townName,
                            villageName
                        }
                    }
                }",
                "variables":null
            }
        """,
            ""
        )
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.aqi?.getOrNull(0)?.town == null) {
                throw InvalidLocationException()
            }
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it.data.aqi[0].town!!))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }

        return stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        station{
                            stationId
                        },
                        town{
                            ctyName,
                            townCode,
                            townName,
                            villageName
                        }
                    }
                }",
                "variables":null
            }
        """,
            ""
        )
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.aqi?.getOrNull(0) == null ||
                it.data.aqi[0].station?.stationId == null ||
                it.data.aqi[0].town?.ctyName == null ||
                it.data.aqi[0].town?.townName == null ||
                it.data.aqi[0].town?.townCode == null
            ) {
                throw InvalidLocationException()
            }
            mapOf(
                "stationId" to it.data.aqi[0].station?.stationId!!,
                "countyName" to it.data.aqi[0].town?.ctyName!!,
                "townshipName" to it.data.aqi[0].town?.townName!!,
                "townshipCode" to it.data.aqi[0].town?.townCode!!
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
        return apikey.ifEmpty { BuildConfig.CWA_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_cwa_api_key,
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
        private const val CWA_BASE_URL = "https://opendata.cwa.gov.tw/"
        private const val SUN_ENDPOINT = "A-B0062-001"
        private const val SUN_PARAMETERS = "SunRiseTime,SunSetTime"
        private const val MOON_ENDPOINT = "A-B0063-001"
        private const val MOON_PARAMETERS = "MoonRiseTime,MoonSetTime"
        private val LINE_FEED_SPACES = Regex("""\n\s*""")
    }
}
