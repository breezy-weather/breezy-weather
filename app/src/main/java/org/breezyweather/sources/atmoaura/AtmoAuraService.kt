package org.breezyweather.sources.atmoaura

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.atmoaura.json.AtmoAuraPointResult
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject

/**
 * Atmo Aura air quality service.
 */
class AtmoAuraService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), SecondaryWeatherSource, ConfigurableSource {

    override val id = "atmoaura"
    override val name = "Atmo Auvergne-Rhône-Alpes"
    override val privacyPolicyUrl = "https://www.atmo-auvergnerhonealpes.fr/article/politique-de-confidentialite"

    private val mApi by lazy {
        client
            .baseUrl(ATMO_AURA_BASE_URL)
            .build()
            .create(AtmoAuraAirQualityApi::class.java)
    }

    override val supportedFeatures = listOf(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
    override fun isFeatureSupportedForLocation(
        feature: SecondaryWeatherSourceFeature, location: Location
    ): Boolean {
        return (feature == SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY
                && (!location.countryCode.isNullOrEmpty() && location.countryCode.equals("FR", ignoreCase = true))
                && !location.provinceCode.isNullOrEmpty()
                && location.provinceCode in arrayOf("01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74"))
    }
    override val airQualityAttribution = "Atmo Auvergne-Rhône-Alpes"
    override val allergenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        if (!isFeatureSupportedForLocation(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY, location)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        val calendar = Date().toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DATE, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return mApi.getPointDetails(
            getApiKeyOrDefault(),
            location.longitude.toDouble(),
            location.latitude.toDouble(),  // Tomorrow because it gives access to D-1 and D+1
            calendar.time.getFormattedDate(location.timeZone, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(AtmoAuraPointResult())
            }
        }.map {
            convert(it)
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
        return apikey.ifEmpty { BuildConfig.ATMO_AURA_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_atmo_aura_api_key,
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
        private const val ATMO_AURA_BASE_URL = "https://api.atmo-aura.fr/"
    }
}