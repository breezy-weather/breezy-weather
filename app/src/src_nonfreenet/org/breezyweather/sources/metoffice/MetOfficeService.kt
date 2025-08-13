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

package org.breezyweather.sources.metoffice

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.metoffice.json.MetOfficeDaily
import org.breezyweather.sources.metoffice.json.MetOfficeForecast
import org.breezyweather.sources.metoffice.json.MetOfficeHourly
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class MetOfficeService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "metoffice"
    override val name = "Met Office (${context.currentLocale.getCountryName("GB")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.metoffice.gov.uk/policies/privacy"

    private val mApi by lazy {
        client
            .baseUrl(MET_OFFICE_BASE_URL)
            .build()
            .create(MetOfficeApi::class.java)
    }

    private val weatherAttribution = "Met Office"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.metoffice.gov.uk/"
    )

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            arrayOf("GB", "GG", "IM", "JE", "GI", "FK").any {
                location.countryCode.equals(it, ignoreCase = true)
            } -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        return Observable.zip(
            mApi.getHourlyForecast(apiKey, location.latitude, location.longitude),
            mApi.getDailyForecast(apiKey, location.latitude, location.longitude)
        ) { hourly, daily ->
            if (hourly.features.isEmpty() || daily.features.isEmpty()) {
                throw InvalidOrIncompleteDataException()
            }
            WeatherWrapper(
                dailyForecast = getDailyForecast(daily, context),
                hourlyForecast = getHourlyForecast(hourly, context)
            )
        }
    }

    private fun getDailyForecast(
        dailyResult: MetOfficeForecast<MetOfficeDaily>,
        context: Context,
    ): List<DailyWrapper> {
        val feature = dailyResult.features[0] // should only be one feature for this kind of API call
        return feature.properties.timeSeries.map { result ->
            val (dayText, dayCode) = convertWeatherCode(result.daySignificantWeatherCode, context)
                ?: Pair(null, null)
            val (nightText, nightCode) = convertWeatherCode(result.nightSignificantWeatherCode, context)
                ?: Pair(null, null)
            DailyWrapper(
                date = result.time,
                day = HalfDayWrapper(
                    weatherText = dayText,
                    weatherCode = dayCode,
                    temperature = TemperatureWrapper(
                        temperature = result.dayMaxScreenTemperature,
                        feelsLike = result.dayMaxFeelsLikeTemp
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = result.dayProbabilityOfPrecipitation?.toDouble(),
                        rain = result.dayProbabilityOfRain?.toDouble(),
                        snow = result.dayProbabilityOfSnow?.toDouble(),
                        thunderstorm = result.dayProbabilityOfSferics?.toDouble()
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = nightText,
                    weatherCode = nightCode,
                    temperature = TemperatureWrapper(
                        temperature = result.nightMinScreenTemperature,
                        feelsLike = result.nightMinFeelsLikeTemp
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = result.nightProbabilityOfPrecipitation?.toDouble(),
                        rain = result.nightProbabilityOfRain?.toDouble(),
                        snow = result.nightProbabilityOfSnow?.toDouble(),
                        thunderstorm = result.nightProbabilityOfSferics?.toDouble()
                    )
                ),
                uV = UV(index = result.maxUvIndex?.toDouble())
            )
        }
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: MetOfficeForecast<MetOfficeHourly>,
        context: Context,
    ): List<HourlyWrapper> {
        val feature = hourlyResult.features[0] // should only be one feature for this kind of API call
        return feature.properties.timeSeries.map { result ->
            val (weatherText, weatherCode) = convertWeatherCode(result.significantWeatherCode, context)
                ?: Pair(null, null)
            HourlyWrapper(
                date = result.time,
                weatherText = weatherText,
                weatherCode = weatherCode,
                temperature = TemperatureWrapper(
                    temperature = result.screenTemperature,
                    feelsLike = result.feelsLikeTemperature
                ),
                precipitation = Precipitation(
                    total = result.totalPrecipAmount,
                    snow = result.totalSnowAmount
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.probOfPrecipitation?.toDouble()
                ),
                wind = Wind(
                    degree = result.windDirectionFrom10m?.toDouble(),
                    speed = result.windSpeed10m,
                    gusts = result.windGustSpeed10m
                ),
                uV = UV(
                    index = result.uvIndex?.toDouble()
                ),
                relativeHumidity = result.screenRelativeHumidity,
                dewPoint = result.screenDewPointTemperature,
                pressure = result.mslp?.toDouble()?.pascals,
                visibility = result.visibility?.toDouble()?.meters
            )
        }
    }

    private fun convertWeatherCode(
        significantWeatherCode: Int?,
        context: Context,
    ): Pair<String, WeatherCode>? {
        return when (significantWeatherCode) {
            -1 -> Pair("Trace rain", WeatherCode.CLOUDY)
            0 -> Pair(context.getString(R.string.common_weather_text_clear_sky), WeatherCode.CLEAR)
            1 -> Pair(context.getString(R.string.common_weather_text_clear_sky), WeatherCode.CLEAR)
            2, 3 -> Pair(context.getString(R.string.common_weather_text_partly_cloudy), WeatherCode.PARTLY_CLOUDY)
            5 -> Pair(context.getString(R.string.common_weather_text_mist), WeatherCode.FOG)
            6 -> Pair(context.getString(R.string.common_weather_text_fog), WeatherCode.FOG)
            7 -> Pair(context.getString(R.string.common_weather_text_cloudy), WeatherCode.CLOUDY)
            8 -> Pair(context.getString(R.string.common_weather_text_overcast), WeatherCode.CLOUDY)
            9, 10 -> Pair(context.getString(R.string.common_weather_text_rain_showers_light), WeatherCode.RAIN)
            11 -> Pair(context.getString(R.string.common_weather_text_drizzle), WeatherCode.RAIN)
            12 -> Pair(context.getString(R.string.common_weather_text_rain_light), WeatherCode.RAIN)
            13, 14 -> Pair(context.getString(R.string.common_weather_text_rain_showers_heavy), WeatherCode.RAIN)
            15 -> Pair(context.getString(R.string.common_weather_text_rain_heavy), WeatherCode.RAIN)
            16, 17 -> Pair(context.getString(R.string.metno_weather_text_sleetshowers), WeatherCode.SLEET)
            18 -> Pair(context.getString(R.string.metno_weather_text_sleet), WeatherCode.SLEET)
            19, 20 -> Pair("Hail shower", WeatherCode.HAIL)
            21 -> Pair(context.getString(R.string.weather_kind_hail), WeatherCode.HAIL)
            22, 23 -> Pair(context.getString(R.string.common_weather_text_snow_showers_light), WeatherCode.SNOW)
            24 -> Pair(context.getString(R.string.common_weather_text_snow_light), WeatherCode.SNOW)
            25, 26 -> Pair(context.getString(R.string.common_weather_text_snow_showers_heavy), WeatherCode.SNOW)
            27 -> Pair(context.getString(R.string.common_weather_text_snow_heavy), WeatherCode.SNOW)
            28, 29 -> Pair(context.getString(R.string.weather_kind_thunderstorm), WeatherCode.THUNDERSTORM)
            30 -> Pair(context.getString(R.string.weather_kind_thunder), WeatherCode.THUNDER)
            else -> null
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
        return apikey.ifEmpty { BuildConfig.MET_OFFICE_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_met_office_api_key,
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
        private const val MET_OFFICE_BASE_URL =
            "https://data.hub.api.metoffice.gov.uk/sitespecific/v0/"
    }
}
