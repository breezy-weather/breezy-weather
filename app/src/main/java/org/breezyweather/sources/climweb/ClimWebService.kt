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

package org.breezyweather.sources.climweb

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.climweb.json.ClimWebAlertsResult
import org.breezyweather.sources.climweb.json.ClimWebNormals
import retrofit2.Retrofit

/**
 * Open-source system used by many African countries
 * https://github.com/wmo-raf/climweb
 *
 * Is an abstract class that must be implemented by each national source
 */
abstract class ClimWebService : HttpSource(), WeatherSource, ConfigurableSource, LocationParametersSource {

    protected abstract val context: Context
    protected abstract val jsonClient: Retrofit.Builder

    /**
     * E.g. https://www.weatherzw.org.zw/
     */
    protected abstract val baseUrl: String

    /**
     * Populate this with the country's ISO 3166-1 alpha-2 code.
     * E.g. "ZW" for Zimbabwe
     */
    protected abstract val countryCode: String

    // TODO: Remove this if/when ClimWeb starts being used outside of Africa
    override val continent = SourceContinent.AFRICA

    /**
     * Populate these variable only if the source supports normals. Otherwise use null.
     *
     * Get pageId from the JavaScript in the source's city climate page.
     * (The page may be labelled differently depending on each source.)
     * E.g. In https://www.meteobenin.bj/climatologie-des-villes-1/ there are these lines:
     *
     *     const options = {
     *         pageId: 105,
     *         city_data_url: "https://www.meteobenin.bj/api/cityclimate/data/105/", ...
     *
     * Then the value for cityClimatePageId would be "105".
     */
    protected abstract val cityClimatePageId: String?

    /**
     * E.g. R.string.settings_weather_source_msd_zw_instance
     */
    protected abstract val instancePreference: Int

    protected abstract val weatherAttribution: String
    protected abstract val alertAttribution: String
    protected abstract val normalsAttribution: String

    protected val mApi: ClimWebApi
        get() {
            return jsonClient
                .baseUrl(instance!!)
                .build()
                .create(ClimWebApi::class.java)
        }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures
        get() = mapOf(
            SourceFeature.ALERT to alertAttribution,
            SourceFeature.NORMALS to normalsAttribution
        )
    override val attributionLinks
        get() = mapOf(
            weatherAttribution to baseUrl
        )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.ALERT ->
                location.countryCode != null &&
                    location.countryCode.equals(countryCode, ignoreCase = true)
            SourceFeature.NORMALS ->
                location.countryCode != null &&
                    location.countryCode.equals(countryCode, ignoreCase = true) &&
                    !cityClimatePageId.isNullOrEmpty()
            else -> false
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts().onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(ClimWebAlertsResult())
            }
        } else {
            Observable.just(ClimWebAlertsResult())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures &&
            !cityClimatePageId.isNullOrEmpty()
        ) {
            val cityId = location.parameters.getOrElse(id) { null }?.getOrElse("cityId") { null }
            if (cityId != null) {
                mApi.getNormals(cityClimatePageId!!, cityId).onErrorResumeNext {
                    // Silently fail here: not all forecast cities have normals
                    Observable.just(emptyList())
                }
            } else {
                // Fail here: There should be a city ID in the location parameters
                failedFeatures[SourceFeature.NORMALS] = InvalidLocationException()
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(alerts, normals) {
                alertsResult: ClimWebAlertsResult,
                normalsResult: List<ClimWebNormals>,
            ->
            WeatherWrapper(
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertAttribution, alertsResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures &&
                    !cityClimatePageId.isNullOrEmpty()
                ) {
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
        if (SourceFeature.NORMALS !in features) return false

        if (coordinatesChanged) return true

        val cityId = location.parameters.getOrElse(id) { null }?.getOrElse("cityId") { null }
        val citySlug = location.parameters.getOrElse(id) { null }?.getOrElse("citySlug") { null }

        return cityId.isNullOrEmpty() || citySlug.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        var nearestDistance = Double.POSITIVE_INFINITY
        var distance: Double
        var nearestCity = ""
        var nearestSlug = ""
        val cities = mApi.getLocationList()
        return cities.map {
            it.forEach { city ->
                if (city.coordinates?.getOrNull(0) != null &&
                    city.coordinates.getOrNull(1) != null &&
                    city.id != null &&
                    city.slug != null
                ) {
                    distance = SphericalUtil.computeDistanceBetween(
                        LatLng(city.coordinates[1], city.coordinates[0]),
                        LatLng(location.latitude, location.longitude)
                    )
                    if (distance < nearestDistance) {
                        nearestDistance = distance
                        nearestCity = city.id
                        nearestSlug = city.slug
                    }
                }
            }
            mapOf<String, String>(
                "cityId" to nearestCity,
                "citySlug" to nearestSlug
            )
        }
    }

    // CONFIG
    private val config
        get() = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", value).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null) ?: baseUrl

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = instancePreference,
                summary = { _, content ->
                    content.ifEmpty {
                        baseUrl
                    }
                },
                content = if (instance != baseUrl) instance else null,
                placeholder = baseUrl,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == baseUrl) null else it.ifEmpty { null }
                }
            )
        )
    }
}
