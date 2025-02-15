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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
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
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "cwa"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "中央氣象署"
                else -> "CWA"
            }
        } + " (${Locale(context.currentLocale.code, "TW").displayCountry})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://www.cwa.gov.tw/V8/C/private.html"
                else -> "https://www.cwa.gov.tw/V8/E/private.html"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(CWA_BASE_URL)
            .build()
            .create(CwaApi::class.java)
    }

    private val weatherAttribution = "中央氣象署"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to "環境部",
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return location.countryCode.equals("TW", ignoreCase = true) ||
            TAIWAN_BBOX.contains(latLng) ||
            PENGHU_BBOX.contains(latLng) ||
            KINMEN_BBOX.contains(latLng) ||
            WUQIU_BBOX.contains(latLng) ||
            MATSU_BBOX.contains(latLng)
    }

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
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

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey = apiKey,
                endpoint = CWA_HOURLY_ENDPOINTS[countyName]!!,
                townshipName = townshipName
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(CwaForecastResult())
            }
        } else {
            Observable.just(CwaForecastResult())
        }
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey = apiKey,
                endpoint = CWA_DAILY_ENDPOINTS[countyName]!!,
                townshipName = townshipName
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(CwaForecastResult())
            }
        } else {
            Observable.just(CwaForecastResult())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                apiKey = apiKey,
                stationId = stationId
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(CwaCurrentResult())
            }
        } else {
            Observable.just(CwaCurrentResult())
        }

        // "Weather Assistant" provides human-written forecast summary on a county level.
        val assistant = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getAssistant(
                endpoint = CWA_ASSISTANT_ENDPOINTS[countyName]!!,
                apiKey = apiKey
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(CwaAssistantResult())
            }
        } else {
            Observable.just(CwaAssistantResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
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
            mApi.getAirQuality(
                apiKey = apiKey,
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(CwaAirQualityResult())
            }
        } else {
            Observable.just(CwaAirQualityResult())
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

        val sun = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getAstro(
                endpoint = SUN_ENDPOINT,
                apiKey = apiKey,
                countyName = countyName,
                parameter = SUN_PARAMETERS,
                timeFrom = timeFrom,
                timeTo = timeTo
            ).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(CwaAstroResult())
            }
        } else {
            Observable.just(CwaAstroResult())
        }
        val moon = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getAstro(
                endpoint = MOON_ENDPOINT,
                apiKey = apiKey,
                countyName = countyName,
                parameter = MOON_PARAMETERS,
                timeFrom = timeFrom,
                timeTo = timeTo
            ).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(CwaAstroResult())
            }
        } else {
            Observable.just(CwaAstroResult())
        }

        // Temperature normals are only available at 27 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val station = LatLng(location.latitude, location.longitude).getNearestLocation(CWA_NORMALS_STATIONS)
        val normals = if (SourceFeature.NORMALS in requestedFeatures && station != null) {
            mApi.getNormals(
                apiKey = apiKey,
                stationId = station,
                month = (now.get(Calendar.MONTH) + 1).toString()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(CwaNormalsResult())
            }
        } else {
            Observable.just(CwaNormalsResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                apiKey = apiKey
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(CwaAlertResult())
            }
        } else {
            Observable.just(CwaAlertResult())
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
            val currentWrapper = if (SourceFeature.CURRENT in requestedFeatures) {
                getCurrent(currentResult, assistantResult)
            } else {
                null
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(dailyResult, sunResult, moonResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyResult)
                } else {
                    null
                },
                current = currentWrapper,
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(
                            airQualityResult,
                            currentWrapper?.temperature?.temperature,
                            getValid(currentResult.records?.station?.getOrNull(0)?.weatherElement?.airPressure)
                                as Double?
                        )
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(alertResult, location)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
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
        features: List<SourceFeature>,
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
                "stationId" to it.data.aqi[0].station!!.stationId!!,
                "countyName" to it.data.aqi[0].town!!.ctyName!!,
                "townshipName" to it.data.aqi[0].town!!.townName!!,
                "townshipCode" to it.data.aqi[0].town!!.townCode!!
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

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val CWA_BASE_URL = "https://opendata.cwa.gov.tw/"
        private const val SUN_ENDPOINT = "A-B0062-001"
        private const val SUN_PARAMETERS = "SunRiseTime,SunSetTime"
        private const val MOON_ENDPOINT = "A-B0063-001"
        private const val MOON_PARAMETERS = "MoonRiseTime,MoonSetTime"
        private val LINE_FEED_SPACES = Regex("""\n\s*""")

        private val TAIWAN_BBOX = LatLngBounds.parse(119.99690416, 21.756143532, 122.10915909, 25.633378776)
        private val PENGHU_BBOX = LatLngBounds.parse(119.314301816, 23.186561404, 119.726986388, 23.810692086)
        private val KINMEN_BBOX = LatLngBounds.parse(118.137979837, 24.160255444, 118.505977425, 24.534228163)
        private val WUQIU_BBOX = LatLngBounds.parse(119.443195363, 24.97760013, 119.479213453, 24.999614154)
        private val MATSU_BBOX = LatLngBounds.parse(119.908905081, 25.940995457, 120.511750672, 26.385275262)
    }
}
