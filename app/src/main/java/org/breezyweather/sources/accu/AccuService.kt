package org.breezyweather.sources.accu

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.accu.json.*
import org.breezyweather.sources.accu.preferences.AccuDaysPreference
import org.breezyweather.sources.accu.preferences.AccuHoursPreference
import org.breezyweather.sources.accu.preferences.AccuPortalPreference
import retrofit2.Retrofit
import javax.inject.Inject

class AccuService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, LocationSearchSource, ReverseGeocodingSource, ConfigurableSource {

    override val id = "accu"
    override val name = "AccuWeather"
    override val privacyPolicyUrl = "https://www.accuweather.com/en/privacy"

    override val color = -0x10a7dd
    override val weatherAttribution = "AccuWeather"
    override val locationSearchAttribution = weatherAttribution

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

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val mApi = if (portal.id == "enterprise") mEnterpriseApi else mDeveloperApi

        val languageCode = SettingsManager.getInstance(context).language.code
        val current = mApi.getCurrent(
            location.cityId,
            apiKey,
            languageCode,
            details = true
        )
        val daily = mApi.getDaily(
            days.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val hourly = mApi.getHourly(
            hours.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val minute = if (mApi is AccuEnterpriseApi) {
            mApi.getMinutely(
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(AccuMinutelyResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(AccuMinutelyResult())
            }
        }
        val alert = if (mApi is AccuEnterpriseApi) {
            mApi.getAlertsByPosition(
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(ArrayList())
                }
            }
        } else {
            mApi.getAlertsByCityKey(
                apiKey,
                location.cityId,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(ArrayList())
                }
            }
        }
        val airQuality = if (mApi is AccuEnterpriseApi) {
            mApi.getAirQuality(
                location.cityId, apiKey,
                pollutants = true,
                languageCode
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(AccuAirQualityResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(AccuAirQualityResult())
            }
        }
        return Observable.zip(current, daily, hourly, minute, alert, airQuality) {
                accuRealtimeResults: List<AccuCurrentResult>,
                accuDailyResult: AccuForecastDailyResult,
                accuHourlyResults: List<AccuForecastHourlyResult>,
                accuMinutelyResult: AccuMinutelyResult,
                accuAlertResults: List<AccuAlertResult>,
                accuAirQualityResult: AccuAirQualityResult
            ->
            convert(
                context,
                location,
                accuRealtimeResults[0],
                accuDailyResult,
                accuHourlyResults,
                accuMinutelyResult,
                accuAlertResults,
                accuAirQualityResult
            )
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code
        val mApi = if (portal.id == "enterprise") mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocation(
            apiKey,
            query,
            languageCode,
            details = false,
            alias = "Always"
        ).map { results ->
            // TODO: Why? This will use searched terms as zip code even if the zip code is incomplete
            val zipCode = if (query.matches("[a-zA-Z0-9]*".toRegex())) query else null

            results.map {
                convert(null, it, zipCode)
            }
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code
        val mApi = if (portal.id == "enterprise") mEnterpriseApi else mDeveloperApi
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).map {
            val locationList: MutableList<Location> = ArrayList()
            locationList.add(convert(location, it, null))
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
            config.getString("portal", null) ?: "enterprise"
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
            config.getString("hours", null) ?: "240"
        )

    private fun getApiKeyOrDefault() = apikey.ifEmpty { BuildConfig.ACCU_WEATHER_KEY }
    private fun isConfigured() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            ListPreference(
                titleId = R.string.settings_weather_source_portal,
                selectedKey = portal.id,
                valueArrayId = R.array.accu_preference_portal_values,
                nameArrayId = R.array.accu_preference_portal,
                onValueChanged = {
                    portal = AccuPortalPreference.getInstance(it)
                },
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_provider_accu_api_key,
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
                },
            ),
            ListPreference(
                titleId = R.string.setting_weather_source_accu_hours,
                selectedKey = hours.id,
                valueArrayId = R.array.accu_preference_hour_values,
                nameArrayId = R.array.accu_preference_hours,
                onValueChanged = {
                    hours = AccuHoursPreference.getInstance(it)
                },
            )
        )
    }

    companion object {
        private const val ACCU_DEVELOPER_BASE_URL = "https://dataservice.accuweather.com/"
        private const val ACCU_ENTERPRISE_BASE_URL = "https://api.accuweather.com/"
    }
}