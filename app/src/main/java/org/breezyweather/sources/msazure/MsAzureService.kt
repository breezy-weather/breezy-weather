package org.breezyweather.sources.msazure

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirQualityForecastResponse
import org.breezyweather.sources.msazure.json.alerts.MsAzureWeatherAlertsResponse
import org.breezyweather.sources.msazure.json.current.MsAzureCurrentConditionsResponse
import org.breezyweather.sources.msazure.json.daily.MsAzureDailyForecastResponse
import org.breezyweather.sources.msazure.json.hourly.MsAzureHourlyForecastResponse
import org.breezyweather.sources.msazure.json.minutely.MsAzureMinutelyForecastResponse
import retrofit2.Retrofit
import javax.inject.Inject

class MsAzureService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ConfigurableSource {

    override val id = "msazure"
    override val name = "Microsoft Azure"

    // https://www.microsoft.com/licensing/terms/product/PrivacyandSecurityTerms/all
    override val privacyPolicyUrl = "https://privacy.microsoft.com/en-us/privacystatement"

    override val color = Color.rgb(116, 116, 116)
    override val weatherAttribution =
        "Microsoft Corporation, One Microsoft Way, Redmond, WA 98052-6399"

    private val mApi by lazy {
        client
            .baseUrl(MS_AZURE_WEATHER_BASE_URL)
            .build()
            .create(MsAzureWeatherApi::class.java)
    }

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val lang = SettingsManager.getInstance(context).language.code

        val query = "${location.latitude},${location.longitude}"

        val currentConditionsRequest = mApi.getCurrentConditions(
            apiKey, query, lang
        )
        val dailyForecastRequest = mApi.getDailyForecast(
            apiKey, query, 15, lang
        )
        val hourlyForecastRequest = mApi.getHourlyForecast(
            apiKey, query, 240, lang
        )
        val minutelyForecastRequest = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getMinutelyForecast(
                apiKey, query, 1, lang
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MsAzureMinutelyForecastResponse())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureMinutelyForecastResponse())
            }
        }
        val currentAirQualityRequest = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getCurrentAirQuality(
                apiKey, query, lang
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MsAzureAirQualityForecastResponse())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureAirQualityForecastResponse())
            }
        }
        val hourlyAirQualityForecastRequest = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getHourlyAirQuality(
                apiKey, query, 96, lang
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MsAzureAirQualityForecastResponse())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureAirQualityForecastResponse())
            }
        }
        val alertsRequest = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getWeatherAlerts(
                apiKey, query, lang
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MsAzureWeatherAlertsResponse())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureWeatherAlertsResponse())
            }
        }

        return Observable.zip(
            currentConditionsRequest,
            dailyForecastRequest,
            hourlyForecastRequest,
            minutelyForecastRequest,
            currentAirQualityRequest,
            hourlyAirQualityForecastRequest,
            alertsRequest
        ) { currentConditionsResponse: MsAzureCurrentConditionsResponse,
            dailyForecastResponse: MsAzureDailyForecastResponse,
            hourlyForecastResponse: MsAzureHourlyForecastResponse,
            minutelyForecastResponse: MsAzureMinutelyForecastResponse,
            currentAirQualityResponse: MsAzureAirQualityForecastResponse,
            hourlyAirQualityResponse: MsAzureAirQualityForecastResponse,
            alertsResponse: MsAzureWeatherAlertsResponse
            ->
            convertPrimary(
                location,
                currentConditionsResponse,
                dailyForecastResponse,
                hourlyForecastResponse,
                minutelyForecastResponse,
                currentAirQualityResponse,
                hourlyAirQualityResponse,
                alertsResponse
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_ALLERGEN,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override val airQualityAttribution = weatherAttribution
    override val allergenAttribution = weatherAttribution
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val lang = SettingsManager.getInstance(context).language.code
        val query = "${location.latitude},${location.longitude}"

        val dailyForecastRequest = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALLERGEN)) {
            mApi.getDailyForecast(
                apiKey, query, 15, lang
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureDailyForecastResponse())
            }
        }
        val minutelyForecastRequest = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getMinutelyForecast(
                apiKey, query, 1, lang
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureMinutelyForecastResponse())
            }
        }
        val currentAirQualityRequest = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getCurrentAirQuality(
                apiKey, query, lang
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureAirQualityForecastResponse())
            }
        }
        val hourlyAirQualityForecastRequest = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getHourlyAirQuality(
                apiKey, query, 96, lang
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureAirQualityForecastResponse())
            }
        }
        val alertsRequest = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getWeatherAlerts(
                apiKey, query, lang
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MsAzureWeatherAlertsResponse())
            }
        }

        return Observable.zip(
            dailyForecastRequest,
            minutelyForecastRequest,
            currentAirQualityRequest,
            hourlyAirQualityForecastRequest,
            alertsRequest
        ) { dailyForecastResponse: MsAzureDailyForecastResponse,
            minutelyForecastResponse: MsAzureMinutelyForecastResponse,
            currentAirQualityResponse: MsAzureAirQualityForecastResponse,
            hourlyAirQualityResponse: MsAzureAirQualityForecastResponse,
            alertsResponse: MsAzureWeatherAlertsResponse
            ->
            convertSecondary(
                dailyForecastResponse,
                minutelyForecastResponse,
                currentAirQualityResponse,
                hourlyAirQualityResponse,
                alertsResponse
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
        return apikey.ifEmpty { BuildConfig.MS_AZURE_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_provider_msn_azure_api_key,
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
        private const val MS_AZURE_WEATHER_BASE_URL = "https://atlas.microsoft.com/weather/"
    }
}