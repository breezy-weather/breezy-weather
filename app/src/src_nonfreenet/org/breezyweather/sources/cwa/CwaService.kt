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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
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
import org.breezyweather.sources.cwa.json.CwaAlertResult
import org.breezyweather.sources.cwa.json.CwaAstroResult
import org.breezyweather.sources.cwa.json.CwaNormalsResult
import org.breezyweather.sources.cwa.json.CwaWeatherResult
import retrofit2.Retrofit
import javax.inject.Named

class CwaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource,
    ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "cwa"
    override val name = "中央氣象署"
    override val privacyPolicyUrl = "https://www.cwa.gov.tw/V8/E/private.html"

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
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?
    ): Boolean {
        return location.countryCode.equals("TW", ignoreCase = true)
    }

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // County Name and Township Code are retrieved upon reverse geocoding,
        // but not for user-selected locations. Since a few API calls require these,
        // we will make sure these parameters are available before proceeding.
        val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }
        val township = location.parameters.getOrElse(id) { null }?.getOrElse("township") { null }
        if (county.isNullOrEmpty() || township.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        // The main weather API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = "{\"query\":\"query aqi { aqi(latitude: ${location.latitude}, longitude: ${location.longitude}) { station { stationId, locationName, latitude, longitude, time { obsTime }, weatherElement { elementName, elementValue } }, sitename, county, latitude, longitude, so2, co, o3, pm10, pm2_5, no2, publishtime, town { forecast72hr { Wx { timePeriods { startTime, weather, weatherIcon } }, T { timePeriods { dataTime, temperature, } }, AT { timePeriods { dataTime, apparentTemperature, } }, Td { timePeriods { dataTime, dewPointTemperature } }, RH { timePeriods { dataTime, relativeHumidity } }, WD { timePeriods { dataTime, windDirectionDescription } }, WS { timePeriods { dataTime, windSpeed } }, PoP6h { timePeriods { startTime, probabilityOfPrecipitation, } } }, forecastWeekday { Wx { timePeriods { startTime, weather, weatherIcon } }, MinT { timePeriods { startTime, temperature } }, MaxT { timePeriods { startTime, temperature } }, MinAT { timePeriods { startTime, apparentTemperature } }, MaxAT { timePeriods { startTime, apparentTemperature } }, WD { timePeriods { startTime, windDirectionDescription } }, WS { timePeriods { startTime, windSpeed } }, PoP12h { timePeriods { startTime, probabilityOfPrecipitation, } }, UVI { timePeriods { startTime, UVIndex, } } } } } }\",\"variables\":null}"
        val weather = mApi.getWeather(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        )

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
            SUN_ENDPOINT,
            apiKey,
            "json",
            county,
            SUN_PARAMETERS,
            timeFrom,
            timeTo
        )
        val moon = mApi.getAstro(
            MOON_ENDPOINT,
            apiKey,
            "json",
            county,
            MOON_PARAMETERS,
            timeFrom,
            timeTo
        )

        // Temperature normals are only available at 26 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val station = getNearestStation(location, CWA_NORMALS_STATIONS)
        val normals = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS) && station != null) {
            mApi.getNormals(
                apiKey,
                "json",
                station,
                "AirTemperature",
                (now.get(Calendar.MONTH) + 1).toString()
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaNormalsResult())
            }
        }

        val alerts = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                apiKey,
                "json"
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaAlertResult())
            }
        }

        return Observable.zip(weather, normals, alerts, sun, moon) {
                weatherResult: CwaWeatherResult,
                normalsResult: CwaNormalsResult,
                alertResult: CwaAlertResult,
                sunResult: CwaAstroResult,
                moonResult: CwaAstroResult
            ->
            convert(
                weatherResult = weatherResult,
                normalsResult = normalsResult,
                alertResult = alertResult,
                sunResult = sunResult,
                moonResult = moonResult,
                location = location,
                id = id,
                ignoreFeatures = ignoreFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val airQualityAttribution = "環境部"
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location, requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
            || !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_ALERT)
            || !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
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
        val county = location.parameters
            .getOrElse(id) { null }?.getOrElse("county") { null }
        val township = location.parameters
            .getOrElse(id) { null }?.getOrElse("township") { null }

        if (county.isNullOrEmpty() || township.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        // The air quality API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = "{\"query\":\"query aqi { aqi(latitude: ${location.latitude}, longitude: ${location.longitude}) { sitename, county, latitude, longitude, so2, co, o3, pm10, pm2_5, no2, publishtime } }\",\"variables\":null}"

        val airQuality = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getWeather(
                apiKey,
                body.toRequestBody("application/json".toMediaTypeOrNull())
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaWeatherResult())
            }
        }

        val alerts = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                apiKey,
                "json"
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
        val normals = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS) && station != null) {
            mApi.getNormals(
                apiKey,
                "json",
                station,
                "AirTemperature",
                (now.get(Calendar.MONTH) + 1).toString()
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(CwaNormalsResult())
            }
        }

        return Observable.zip(airQuality, alerts, normals) {
                airQualityResult: CwaWeatherResult,
                alertResult: CwaAlertResult,
                normalsResult: CwaNormalsResult
            ->
            convertSecondary(
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityResult
                } else null,
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    alertResult
                } else null,
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
                    normalsResult
                } else null,
                location,
                id
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context, location: Location
    ): Observable<List<Location>> {

        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = "{\"query\":\"query town { town (latitude: ${location.latitude}, longitude: ${location.longitude}) { townCode, ctyName, townName, villageName } }\",\"variables\":null}"
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.town == null) {
                throw InvalidLocationException()
            }
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it.data.town))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        if (coordinatesChanged) return true

        val currentCounty = location.parameters
            .getOrElse(id) { null }?.getOrElse("county") { null }
        val currentTownship = location.parameters
            .getOrElse(id) { null }?.getOrElse("township") { null }

        return currentCounty.isNullOrEmpty() ||
                currentTownship.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        // Use of API key is mandatory for CWA's API calls.
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = "{\"query\":\"query town { town (latitude: ${location.latitude}, longitude: ${location.longitude}) { townCode, ctyName, townName, villageName } }\",\"variables\":null}"
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.town?.ctyName == null || it.data.town.townCode == null) {
                throw InvalidLocationException()
            }
            mapOf(
                "county" to it.data.town.ctyName,
                "township" to it.data.town.townCode
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
            ),
        )
    }

    companion object {
        private const val CWA_BASE_URL = "https://opendata.cwa.gov.tw/"
        private const val SUN_ENDPOINT = "A-B0062-001"
        private const val SUN_PARAMETERS = "SunRiseTime,SunSetTime"
        private const val MOON_ENDPOINT = "A-B0063-001"
        private const val MOON_PARAMETERS = "MoonRiseTime,MoonSetTime"
   }
}
