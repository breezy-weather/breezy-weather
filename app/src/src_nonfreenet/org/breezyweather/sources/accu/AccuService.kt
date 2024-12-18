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

package org.breezyweather.sources.accu

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.accu.json.AccuAirQualityResult
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuClimoSummaryResult
import org.breezyweather.sources.accu.json.AccuCurrentResult
import org.breezyweather.sources.accu.json.AccuForecastDailyResult
import org.breezyweather.sources.accu.json.AccuForecastHourlyResult
import org.breezyweather.sources.accu.json.AccuMinutelyResult
import org.breezyweather.sources.accu.preferences.AccuDaysPreference
import org.breezyweather.sources.accu.preferences.AccuHoursPreference
import org.breezyweather.sources.accu.preferences.AccuPortalPreference
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class AccuService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(),
    WeatherSource,
    LocationSearchSource,
    ReverseGeocodingSource,
    ConfigurableSource,
    LocationParametersSource {

    override val id = "accu"
    override val name = "AccuWeather"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://www.accuweather.com/en/privacy"

    override val color = Color.rgb(240, 85, 20)

    private val mDeveloperApi by lazy {
        client
            .baseUrl(ACCU_DEVELOPER_BASE_URL)
            .build()
            .create(AccuDeveloperApi::class.java)
    }
    private val mEnterpriseApi by lazy {
        client
            .baseUrl(ACCU_ENTERPRISE_BASE_URL)
            .build()
            .create(AccuEnterpriseApi::class.java)
    }

    private val weatherAttribution = "AccuWeather"
    override val locationSearchAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.POLLEN to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return portal == AccuPortalPreference.ENTERPRISE ||
            feature == SourceFeature.CURRENT ||
            feature == SourceFeature.ALERT
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        val locationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }
        if (
            (
                SourceFeature.FORECAST in requestedFeatures ||
                    SourceFeature.CURRENT in requestedFeatures ||
                    SourceFeature.AIR_QUALITY in requestedFeatures ||
                    SourceFeature.POLLEN in requestedFeatures ||
                    SourceFeature.NORMALS in requestedFeatures ||
                    (SourceFeature.ALERT in requestedFeatures && mApi is AccuEnterpriseApi)
                ) &&
            locationKey.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val metric = SettingsManager.getInstance(context).precipitationUnit != PrecipitationUnit.IN
        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                locationKey!!,
                apiKey,
                languageCode,
                details = true
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(
                days.id,
                locationKey!!,
                apiKey,
                languageCode,
                details = true,
                metric = metric
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(AccuForecastDailyResult())
            }
        } else {
            Observable.just(AccuForecastDailyResult())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(
                hours.id,
                locationKey!!,
                apiKey,
                languageCode,
                details = true,
                metric = metric
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val minute = if (SourceFeature.MINUTELY in requestedFeatures &&
            mApi is AccuEnterpriseApi
        ) {
            mApi.getMinutely(
                minutes = 1,
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.MINUTELY)
                Observable.just(AccuMinutelyResult())
            }
        } else {
            Observable.just(AccuMinutelyResult())
        }
        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            if (mApi is AccuEnterpriseApi) {
                mApi.getAlertsByPosition(
                    apiKey,
                    location.latitude.toString() + "," + location.longitude,
                    languageCode,
                    details = true
                ).onErrorResumeNext {
                    mApi.getAlertsByCityKey(
                        locationKey!!,
                        apiKey,
                        languageCode,
                        details = true
                    ).onErrorResumeNext {
                        failedFeatures.add(SourceFeature.ALERT)
                        Observable.just(emptyList())
                    }
                }
            } else {
                mApi.getAlertsByCityKey(
                    locationKey!!,
                    apiKey,
                    languageCode,
                    details = true
                ).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.ALERT)
                    Observable.just(emptyList())
                }
            }
        } else {
            Observable.just(emptyList())
        }
        val airQuality = if (
            SourceFeature.AIR_QUALITY in requestedFeatures &&
            mApi is AccuEnterpriseApi
        ) {
            mApi.getAirQuality(
                locationKey!!,
                apiKey,
                pollutants = true,
                languageCode
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.AIR_QUALITY)
                Observable.just(AccuAirQualityResult())
            }
        } else {
            Observable.just(AccuAirQualityResult())
        }
        // TODO: Only call once a month, unless it’s current position
        val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
        val climoSummary = if (
            SourceFeature.NORMALS in requestedFeatures &&
            mApi is AccuEnterpriseApi
        ) {
            mApi.getClimoSummary(
                cal[Calendar.YEAR],
                cal[Calendar.MONTH] + 1,
                locationKey!!,
                apiKey,
                languageCode,
                details = false
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.NORMALS)
                Observable.just(AccuClimoSummaryResult())
            }
        } else {
            Observable.just(AccuClimoSummaryResult())
        }
        return Observable.zip(
            current,
            daily,
            hourly,
            minute,
            alert,
            airQuality,
            climoSummary
        ) {
                accuRealtimeResults: List<AccuCurrentResult>,
                accuDailyResult: AccuForecastDailyResult,
                accuHourlyResults: List<AccuForecastHourlyResult>,
                accuMinutelyResult: AccuMinutelyResult,
                accuAlertResults: List<AccuAlertResult>,
                accuAirQualityResult: AccuAirQualityResult,
                accuClimoResult: AccuClimoSummaryResult,
            ->
            WeatherWrapper(
                /*base = Base(
                    publishDate = currentResult.EpochTime.seconds.inWholeMilliseconds.toDate(),
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(accuDailyResult.DailyForecasts, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(accuHourlyResults)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(accuRealtimeResults.getOrNull(0), accuDailyResult, accuMinutelyResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    getAirQualityWrapper(accuAirQualityResult.data)
                } else {
                    null
                },
                pollen = if (SourceFeature.POLLEN in requestedFeatures) {
                    getPollenWrapper(accuDailyResult.DailyForecasts, location)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(accuMinutelyResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(accuAlertResults)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures &&
                    accuClimoResult.Normals?.Temperatures != null
                ) {
                    Normals(
                        month = cal[Calendar.MONTH],
                        daytimeTemperature = accuClimoResult.Normals.Temperatures.Maximum.Metric?.Value,
                        nighttimeTemperature = accuClimoResult.Normals.Temperatures.Minimum.Metric?.Value
                    )
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<Location>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocation(
            apiKey,
            query,
            languageCode,
            details = false,
            alias = "Always"
        ).map { results ->
            results.map {
                convert(null, it)
            }
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it))
            locationList
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private var portal: AccuPortalPreference
        set(value) {
            config.edit().putString("portal", value.id).apply()
        }
        get() = AccuPortalPreference.getInstance(
            if (apikey.isEmpty() || apikey == BuildConfig.ACCU_WEATHER_KEY) {
                // Force portal to make sure a user didn’t select a portal incompatible with the default key
                "enterprise"
            } else {
                config.getString("portal", null) ?: "enterprise"
            }
        )

    private var days: AccuDaysPreference
        set(value) {
            config.edit().putString("days", value.id).apply()
        }
        get() = AccuDaysPreference.getInstance(
            config.getString("days", null) ?: "15"
        )

    private var hours: AccuHoursPreference
        set(value) {
            config.edit().putString("hours", value.id).apply()
        }
        get() = AccuHoursPreference.getInstance(
            (config.getString("hours", null) ?: "240").let {
                if (portal != AccuPortalPreference.ENTERPRISE && it == "240") {
                    "120" // 120 hours is the max on developer portal
                } else {
                    it
                }
            }
        )

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.ACCU_WEATHER_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            ListPreference(
                titleId = R.string.settings_weather_source_portal,
                selectedKey = portal.id,
                valueArrayId = R.array.accu_preference_portal_values,
                nameArrayId = R.array.accu_preference_portal,
                onValueChanged = {
                    portal = AccuPortalPreference.getInstance(it)
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_accu_api_key,
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
            ListPreference(
                titleId = R.string.setting_weather_source_accu_days,
                selectedKey = days.id,
                valueArrayId = R.array.accu_preference_day_values,
                nameArrayId = R.array.accu_preference_days,
                onValueChanged = {
                    days = AccuDaysPreference.getInstance(it)
                }
            ),
            ListPreference(
                titleId = R.string.setting_weather_source_accu_hours,
                selectedKey = hours.id,
                valueArrayId = R.array.accu_preference_hour_values,
                nameArrayId = R.array.accu_preference_hours,
                onValueChanged = {
                    hours = AccuHoursPreference.getInstance(it)
                }
            )
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        return if (SourceFeature.FORECAST in features ||
            SourceFeature.CURRENT in features ||
            SourceFeature.AIR_QUALITY in features ||
            SourceFeature.POLLEN in features ||
            SourceFeature.NORMALS in features ||
            SourceFeature.ALERT in features
        ) {
            val currentLocationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }
            currentLocationKey.isNullOrEmpty()
        } else {
            false
        } // If we request alerts or minutely, we don't need locationKey
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = if (supportedLanguages.contains(context.currentLocale.codeWithCountry)) {
            context.currentLocale.codeWithCountry
        } else if (supportedLanguages.contains(context.currentLocale.code)) {
            context.currentLocale.code
        } else if (context.currentLocale.code.startsWith("iw")) {
            "he"
        } else {
            "en"
        }
        val mApi = if (portal == AccuPortalPreference.ENTERPRISE) mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).map {
            mapOf(
                "locationKey" to it.Key
            )
        }
    }

    companion object {
        private const val ACCU_DEVELOPER_BASE_URL = "https://dataservice.accuweather.com/"
        private const val ACCU_ENTERPRISE_BASE_URL = "https://api.accuweather.com/"

        // Extracted from: https://developer.accuweather.com/localizations-by-language
        // Leads to failure to refresh otherwise
        private val supportedLanguages = setOf(
            "ar", "ar-dz", "ar-bh", "ar-eg", "ar-iq", "ar-jo", "ar-kw", "ar-lb", "ar-ly", "ar-ma",
            "ar-om", "ar-qa", "ar-sa", "ar-sd", "ar-sy", "ar-tn", "ar-ae", "ar-ye",
            "az", "az-latn", "az-latn-az",
            "bn", "bn-bd", "bn-in",
            "bs", "bs-ba",
            "bg", "bg-bg",
            "ca", "ca-es",
            "zh", "zh-hk", "zh-mo", "zh-cn", "zh-hans", "zh-hans-cn", "zh-hans-hk", "zh-hans-mo",
            "zh-hans-sg", "zh-sg", "zh-tw", "zh-hant", "zh-hant-hk", "zh-hant-mo", "zh-hant-tw",
            "hr", "hr-hr",
            "cs", "cs-cz",
            "da", "da-dk",
            "nl", "nl-aw", "nl-be", "nl-cw", "nl-nl", "nl-sx",
            "en", "en-as", "en-us", "en-au", "en-bb", "en-be", "en-bz", "en-bm", "en-bw", "en-cm",
            "en-ca", "en-gh", "en-gu", "en-gy", "en-hk", "en-in", "en-ie", "en-jm", "en-ke",
            "en-mw", "en-my", "en-mt", "en-mh", "en-mu", "en-na", "en-nz", "en-ng", "en-mp",
            "en-pk", "en-ph", "en-rw", "en-sg", "en-za", "en-tz", "en-th", "en-tt", "en-um",
            "en-vi", "en-ug", "en-gb", "en-zm", "en-zw",
            "et", "et-ee",
            "fa", "fa-af", "fa-ir",
            "fil", "fil-ph",
            "fi", "fi-fi",
            "fr", "fr-dz", "fr-be", "fr-bj", "fr-bf", "fr-bi", "fr-cm", "fr-ca", "fr-cf", "fr-td",
            "fr-km", "fr-cg", "fr-cd", "fr-ci", "fr-dj", "fr-gq", "fr-fr", "fr-gf", "fr-ga",
            "fr-gp", "fr-gn", "fr-lu", "fr-mg", "fr-ml", "fr-mq", "fr-mu", "fr-yt", "fr-mc",
            "fr-ma", "fr-ne", "fr-re", "fr-rw", "fr-bl", "fr-mf", "fr-sn", "fr-sc", "fr-ch",
            "fr-tg", "fr-tn",
            "de", "de-at", "de-be", "de-de", "de-li", "de-lu", "de-ch",
            "el", "el-cy", "el-gr",
            "gu",
            "he", "he-il",
            "hi", "hi-in",
            "hu", "hu-hu",
            "is", "is-is",
            "id", "id-id",
            "it", "it-it", "it-ch",
            "ja", "ja-jp",
            "kn",
            "kk", "kk-kz",
            "ko", "ko-kr",
            "lv", "lv-lv",
            "lt", "lt-lt",
            "mk", "mk-mk",
            "ms", "ms-bn", "ms-my",
            "mr",
            "nb",
            "pl", "pl-pl",
            "pt", "pt-ao", "pt-br", "pt-cv", "pt-gw", "pt-mz", "pt-pt", "pt-st",
            "pa", "pa-in",
            "ro", "ro-md", "ro-mo", "ro-ro",
            "ru", "ru-md", "ru-mo", "ru-ru", "ru-ua",
            "sr", "sr-latn", "sr-latn-ba", "sr-me", "sr-rs",
            "sk", "sk-sk",
            "sl", "sl-sl",
            "es", "es-ar", "es-bo", "es-cl", "es-co", "es-cr", "es-do", "es-ec", "es-sv", "es-gq",
            "es-gt", "es-hn", "es-419", "es-mx", "es-ni", "es-pa", "es-py", "es-pe", "es-pr",
            "es-es", "es-us", "es-uy", "es-ve",
            "sw", "sw-cd", "sw-ke", "sw-tz", "sw-ug",
            "sv", "sv-fi", "sv-se",
            "tl",
            "ta", "ta-in", "ta-lk",
            "te", "te-in",
            "th", "th-th",
            "tr", "tr-tr",
            "uk", "uk-ua",
            "ur", "ur-bd", "ur-in", "ur-np", "ur-pk",
            "uz", "uz-latn", "uz-latn-uz",
            "vi", "vi-vn"
        )
    }
}
