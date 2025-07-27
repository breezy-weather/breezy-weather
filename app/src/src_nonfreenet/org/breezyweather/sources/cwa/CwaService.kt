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
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.cwa.json.CwaAirQualityResult
import org.breezyweather.sources.cwa.json.CwaAlertResult
import org.breezyweather.sources.cwa.json.CwaAssistantResult
import org.breezyweather.sources.cwa.json.CwaCbphAlert
import org.breezyweather.sources.cwa.json.CwaCurrentResult
import org.breezyweather.sources.cwa.json.CwaForecastResult
import org.breezyweather.sources.cwa.json.CwaNormalsResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.days

class CwaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") jsonClient: Retrofit.Builder,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "cwa"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "中央氣象署"
                else -> "CWA"
            }
        } +
            " (${Locale(context.currentLocale.code, "TW").displayCountry})"
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

    private val mJsonApi by lazy {
        jsonClient
            .baseUrl(CWA_BASE_URL)
            .build()
            .create(CwaJsonApi::class.java)
    }

    private val mCbphApi by lazy {
        jsonClient
            .baseUrl(CWA_CBPH_BASE_URL)
            .build()
            .create(CwaCbphApi::class.java)
    }

    private val mXmlApi by lazy {
        xmlClient
            .baseUrl(CWA_BASE_URL)
            .build()
            .create(CwaXmlApi::class.java)
    }

    private val weatherAttribution = "中央氣象署"
    private val airQualityAttribution = "環境部"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to airQualityAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.cwa.gov.tw/",
        airQualityAttribution to "https://airtw.moenv.gov.tw/"
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
            mJsonApi.getForecast(
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
            mJsonApi.getForecast(
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
            mJsonApi.getCurrent(
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
            mJsonApi.getAssistant(
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
            mJsonApi.getAirQuality(
                apiKey = apiKey,
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(CwaAirQualityResult())
            }
        } else {
            Observable.just(CwaAirQualityResult())
        }

        // Temperature normals are only available at 27 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val currentMonth = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH] + 1
        val station = LatLng(location.latitude, location.longitude).getNearestLocation(CWA_NORMALS_STATIONS)
        val normals = if (SourceFeature.NORMALS in requestedFeatures && station != null) {
            mJsonApi.getNormals(
                apiKey = apiKey,
                stationId = station,
                month = currentMonth.toString()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(CwaNormalsResult())
            }
        } else {
            Observable.just(CwaNormalsResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mJsonApi.getAlerts(
                apiKey = apiKey
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(CwaAlertResult())
            }
        } else {
            Observable.just(CwaAlertResult())
        }

        // Alerts for localized hazardous weather conditions.
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
        val now = Date()
        val yesterday = Date(now.time - 1.days.inWholeMilliseconds)
        val tomorrow = Date(now.time + 1.days.inWholeMilliseconds)
        val stime = formatter.format(yesterday)
        val etime = formatter.format(tomorrow)
        val geocode = getLegacyTownshipCode(townshipCode)

        val cbphAlertLists = MutableList(CWA_CBPH_ALERT_TYPES.size) { i ->
            if (SourceFeature.ALERT in requestedFeatures) {
                mCbphApi.getCbphAlerts(
                    scope = CWA_CBPH_ALERT_TYPES[i],
                    stime = stime,
                    etime = etime,
                    geocode = geocode
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(emptyList())
                }
            } else {
                Observable.just(emptyList())
            }
        }

        val cbphAlerts = if (SourceFeature.ALERT in requestedFeatures) {
            Observable.zip(cbphAlertLists[0], cbphAlertLists[1], cbphAlertLists[2]) {
                    cellAlerts: List<CwaCbphAlert>,
                    tyWindAlerts: List<CwaCbphAlert>,
                    mountainStormAlerts: List<CwaCbphAlert>,
                ->
                listOf(
                    cellAlerts,
                    tyWindAlerts,
                    mountainStormAlerts
                )
            }
        } else {
            Observable.just(emptyList())
        }

        val capAlerts = Observable.just(
            MutableList(CWA_CAP_ALERT_ENDPOINTS.size) { i ->
                if (SourceFeature.ALERT in requestedFeatures) {
                    mXmlApi.getAlert(
                        endpoint = CWA_CAP_ALERT_ENDPOINTS[i],
                        apiKey = apiKey
                    ).execute().body()
                } else {
                    CapAlert()
                }
            }
        )

        return Observable.zip(current, airQuality, daily, hourly, normals, alerts, assistant, cbphAlerts, capAlerts) {
                currentResult: CwaCurrentResult,
                airQualityResult: CwaAirQualityResult,
                dailyResult: CwaForecastResult,
                hourlyResult: CwaForecastResult,
                normalsResult: CwaNormalsResult,
                alertResult: CwaAlertResult,
                assistantResult: CwaAssistantResult,
                cbphAlertsResult: List<List<CwaCbphAlert>>,
                capAlertsResult: List<CapAlert?>,
            ->
            val currentWrapper = if (SourceFeature.CURRENT in requestedFeatures) {
                getCurrent(currentResult, assistantResult)
            } else {
                null
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(dailyResult)
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
                    getAlertList(alertResult, location) + getCbphAlertList(cbphAlertsResult, location) +
                        getCapAlertList(capAlertsResult, location)
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
                            StationId
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
        return mJsonApi.getLocation(
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
                            StationId
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
        return mJsonApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.aqi?.getOrNull(0) == null ||
                it.data.aqi[0].station?.StationId == null ||
                it.data.aqi[0].town?.ctyName == null ||
                it.data.aqi[0].town?.townName == null ||
                it.data.aqi[0].town?.townCode == null
            ) {
                throw InvalidLocationException()
            }
            mapOf(
                "stationId" to it.data.aqi[0].station!!.StationId!!,
                "countyName" to it.data.aqi[0].town!!.ctyName!!,
                "townshipName" to it.data.aqi[0].town!!.townName,
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
        private const val CWA_CBPH_BASE_URL = "https://cbph.cwa.gov.tw/"
        private val CWA_CBPH_ALERT_TYPES = listOf("cells", "tywinds", "mountainstorms")
        private val CWA_CAP_ALERT_ENDPOINTS = listOf("W-C0033-004", "W-C0033-005")

        private val LINE_FEED_SPACES = Regex("""\n\s*""")

        private val TAIWAN_BBOX = LatLngBounds.parse(119.99690416, 21.756143532, 122.10915909, 25.633378776)
        private val PENGHU_BBOX = LatLngBounds.parse(119.314301816, 23.186561404, 119.726986388, 23.810692086)
        private val KINMEN_BBOX = LatLngBounds.parse(118.137979837, 24.160255444, 118.505977425, 24.534228163)
        private val WUQIU_BBOX = LatLngBounds.parse(119.443195363, 24.97760013, 119.479213453, 24.999614154)
        private val MATSU_BBOX = LatLngBounds.parse(119.908905081, 25.940995457, 120.511750672, 26.385275262)
    }
}
