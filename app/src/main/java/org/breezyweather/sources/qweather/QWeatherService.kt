package org.breezyweather.sources.qweather

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.qweather.json.QWeatherAlertsResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentAQIResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherDailyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherHourlyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherLocationCityResult
import org.breezyweather.sources.qweather.json.QWeatherLocationPOIResult
import org.breezyweather.sources.qweather.json.QWeatherMinutelyPrecipitationResult
import retrofit2.Retrofit
import javax.inject.Inject

class QWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), LocationSearchSource, ConfigurableSource, MainWeatherSource, ReverseGeocodingSource {
    override val id = "qweather"
    override val name = "QWeather"

    override val privacyPolicyUrl = "https://www.qweather.com/terms/tos"

    override val color = Color.rgb(202, 240, 255)
    override val weatherAttribution = "Qweather"
    override val locationSearchAttribution = weatherAttribution

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )


    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        if (location.cityId.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }
        val currentweather = mApi.getCurrentWeather(
            location.cityId,
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        val currentaqi = mApi.getCurrentAQI(
            location.cityId,
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        val alerts = mApi.getCurrentAlerts(
            location.cityId,
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        val hourlyweather = mApi.getHourlyWeather(
            location.cityId,
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        val dailyweather = mApi.getDailyWeather(
            location.cityId,
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        val minutelyprecipitation = mApi.getMinutelyPrecipitation(
            "${location.longitude},${location.latitude}",
            getApiKeyOrDefault(),
            SettingsManager.getInstance(context).language.code
        )
        return Observable.zip(currentweather, currentaqi, alerts, hourlyweather, dailyweather, minutelyprecipitation) {
                currweather: QWeatherCurrentWeatherResult,
                curraqi: QWeatherCurrentAQIResult,
                curralerts: QWeatherAlertsResult,
                hourly: QWeatherHourlyWeatherResult,
                daily: QWeatherDailyWeatherResult,
                minutely: QWeatherMinutelyPrecipitationResult
            ->
            convert(
                currweather,
                curraqi,
                curralerts,
                hourly,
                daily,
                minutely
            )
        }
    }

    override fun requestLocationSearch(
        context: Context, query: String
    ): Observable<List<Location>> {
        val locationList: MutableList<Location> = ArrayList()
        val citysearchresults = localeApi.getCitySearch(
            query,
            getApiKeyOrDefault(),
            5,
            SettingsManager.getInstance(context).language.code
        )
        val poiscenicsearchresults = localeApi.getPoiSearch(
            query,
            "scenic",
            getApiKeyOrDefault(),
            5,
            SettingsManager.getInstance(context).language.code
        )
        val poicstasearchresults = localeApi.getPoiSearch(
            query,
            "CSTA",
            getApiKeyOrDefault(),
            2,
            SettingsManager.getInstance(context).language.code
        )
        val poitstasearchresults = localeApi.getPoiSearch(
            query,
            "TSTA",
            getApiKeyOrDefault(),
            2,
            SettingsManager.getInstance(context).language.code
        )
        return Observable.zip(citysearchresults, poiscenicsearchresults, poicstasearchresults, poitstasearchresults) {
                citysearch: QWeatherLocationCityResult,
                poiscenicsearch: QWeatherLocationPOIResult,
                poicstasearch: QWeatherLocationPOIResult,
                poitstasearch: QWeatherLocationPOIResult
            ->
            if (citysearch.code != "404" && citysearch.code != "204" && citysearch.code != "402") citysearch.location?.forEach {
                locationList.add(convert(it))
            }
            if (poiscenicsearch.code != "404" && poiscenicsearch.code != "204" && poiscenicsearch.code != "402") poiscenicsearch.poi?.forEach {
                locationList.add(convert(it))
            }
            if (poicstasearch.code != "404" && poicstasearch.code != "204" && poicstasearch.code != "402") poicstasearch.poi?.forEach {
                locationList.add(convert(it))
            }
            if (poitstasearch.code != "404" && poitstasearch.code != "204" && poitstasearch.code != "402") poitstasearch.poi?.forEach {
                locationList.add(convert(it))
            }
            locationList
        }
    }

    override fun isUsable(location: Location): Boolean {
        return !location.cityId.isNullOrEmpty()
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        return localeApi.getCitySearch(
            "${location.longitude},${location.latitude}",
            getApiKeyOrDefault(),
            1,
            SettingsManager.getInstance(context).language.code
        ).map { results ->
            val locationList: MutableList<Location> = ArrayList()
            results.location?.forEach {
                locationList.add(convert(it))
            }
            locationList
        }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()
    override val isRestricted = false

    private val localeApi by lazy {
        client
            .baseUrl(QWEATHER_LOCALE_BASE_URL)
            .build()
            .create(QWeatherLocaleApi::class.java)
    }
    private val mApi by lazy {
        client
            .baseUrl(QWEATHER_BASE_URL)
            .build()
            .create(QWeatherApi::class.java)
    }


    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault() = apikey.ifEmpty {
        BuildConfig.QWEATHER_WEATHER_KEY
    }

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_provider_qweather_weather_api_key,
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
        private const val QWEATHER_LOCALE_BASE_URL = "https://geoapi.qweather.com/v2/"
        private const val QWEATHER_BASE_URL = "https://devapi.qweather.com/v7/"
    }
}