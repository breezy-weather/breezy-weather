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

package org.breezyweather.sources.here

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.here.json.HereGeocodingData
import org.breezyweather.sources.here.json.HereWeatherData
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class HereService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, ConfigurableSource {
    override val id = "here"
    override val name = "HERE"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://legal.here.com/privacy/policy"

    private val mWeatherApi by lazy {
        client
            .baseUrl(if (BreezyWeather.instance.debugMode) HERE_WEATHER_DEV_BASE_URL else HERE_WEATHER_BASE_URL)
            .build()
            .create(HereWeatherApi::class.java)
    }

    /*private val mGeocodingApi by lazy {
        client
            .baseUrl(HERE_GEOCODING_BASE_URL)
            .build()
            .create(HereGeocodingApi::class.java)
    }*/

    private val mRevGeocodingApi by lazy {
        client
            .baseUrl(HERE_REV_GEOCODING_BASE_URL)
            .build()
            .create(HereRevGeocodingApi::class.java)
    }

    private val weatherAttribution = "HERE"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.here.com/"
    )

    /**
     * Returns weather
     */
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val products = buildList {
            if (SourceFeature.CURRENT in requestedFeatures) {
                add("observation")
            }
            if (SourceFeature.FORECAST in requestedFeatures) {
                add("forecast7daysSimple")
                add("forecastHourly")
            }
        }

        return mWeatherApi.getForecast(
            apiKey,
            products.joinToString(separator = ","),
            "${location.latitude},${location.longitude}",
            "metric",
            context.currentLocale.codeWithCountry,
            oneObservation = true
        ).map { hereWeatherForecastResult ->
            if (hereWeatherForecastResult.places.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            WeatherWrapper(
                /*base = Base(
                    publishDate = currentForecast?.time ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    val dailySimpleForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.dailyForecasts?.getOrNull(0)?.forecasts
                    }
                    getDailyForecast(dailySimpleForecasts)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    val hourlyForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.hourlyForecasts?.getOrNull(0)?.forecasts
                    }
                    getHourlyForecast(hourlyForecasts)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    val currentForecast = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.observations?.getOrNull(0)
                    }
                    getCurrentForecast(currentForecast)
                } else {
                    null
                }
            )
        }
    }

    /**
     * Returns current forecast
     */
    private fun getCurrentForecast(result: HereWeatherData?): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherText = result.description,
            weatherCode = getWeatherCode(result.iconId),
            temperature = TemperatureWrapper(
                temperature = result.temperature?.celsius,
                feelsLike = result.comfort?.toDoubleOrNull()?.celsius
            ),
            wind = Wind(
                degree = result.windDirection,
                speed = result.windSpeed?.kilometersPerHour
            ),
            uV = UV(index = result.uvIndex?.toDouble()),
            relativeHumidity = result.humidity?.toDouble(),
            dewPoint = result.dewPoint?.celsius,
            pressure = result.barometerPressure?.hectopascals,
            visibility = result.visibility?.kilometers
        )
    }

    /**
     * Returns daily forecast
     */
    private fun getDailyForecast(
        dailySimpleForecasts: List<HereWeatherData>?,
    ): List<DailyWrapper> {
        if (dailySimpleForecasts.isNullOrEmpty()) return emptyList()
        val dailyList: MutableList<DailyWrapper> = ArrayList(dailySimpleForecasts.size)
        for (i in 0 until dailySimpleForecasts.size - 1) { // Skip last day
            val dailyForecast = dailySimpleForecasts[i]

            dailyList.add(
                DailyWrapper(
                    date = dailyForecast.time,
                    day = HalfDayWrapper(
                        temperature = TemperatureWrapper(
                            temperature = if (!dailyForecast.highTemperature.isNullOrEmpty()) {
                                dailyForecast.highTemperature.toDoubleOrNull()?.celsius
                            } else {
                                null
                            }
                        )
                    ),
                    night = HalfDayWrapper(
                        // low temperature is actually from previous night,
                        // so we try to get low temp from next day if available
                        temperature = TemperatureWrapper(
                            temperature = if (!dailySimpleForecasts.getOrNull(i + 1)?.lowTemperature.isNullOrEmpty()) {
                                dailySimpleForecasts[i + 1].lowTemperature!!.toDoubleOrNull()?.celsius
                            } else {
                                null
                            }
                        )
                    ),
                    uV = UV(index = dailyForecast.uvIndex?.toDouble())
                )
            )
        }
        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: List<HereWeatherData>?,
    ): List<HourlyWrapper> {
        if (hourlyResult.isNullOrEmpty()) return emptyList()
        return hourlyResult.map { result ->
            HourlyWrapper(
                date = result.time,
                weatherText = result.description,
                weatherCode = getWeatherCode(result.iconId),
                temperature = TemperatureWrapper(
                    temperature = result.temperature?.celsius,
                    feelsLike = result.comfort?.toDoubleOrNull()?.celsius
                ),
                precipitation = Precipitation(
                    total = result.precipitation1H?.millimeters ?: (result.rainFall + result.snowFall)?.millimeters,
                    rain = result.rainFall?.millimeters,
                    snow = result.snowFall?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.precipitationProbability?.toDouble()
                ),
                wind = Wind(
                    degree = result.windDirection,
                    speed = result.windSpeed?.kilometersPerHour
                ),
                uV = UV(index = result.uvIndex?.toDouble()),
                relativeHumidity = result.humidity?.toDouble(),
                dewPoint = result.dewPoint?.celsius,
                pressure = result.barometerPressure?.hectopascals,
                visibility = result.visibility?.kilometers
            )
        }
    }

    /**
     * Returns weather code based on icon id
     */
    private fun getWeatherCode(icon: Int?): WeatherCode? {
        return when (icon) {
            1, 2, 13, 14 -> WeatherCode.CLEAR
            3 -> WeatherCode.HAZE
            4, 5, 15, 16 -> WeatherCode.PARTLY_CLOUDY
            6 -> WeatherCode.PARTLY_CLOUDY
            7, 17 -> WeatherCode.CLOUDY
            8, 9, 10, 12 -> WeatherCode.FOG
            11 -> WeatherCode.WIND
            18, 19, 20, 32, 33, 34 -> WeatherCode.RAIN
            21, 22, 23, 25, 26, 35 -> WeatherCode.THUNDERSTORM
            24 -> WeatherCode.HAIL
            27, 28 -> WeatherCode.SLEET
            29, 30, 31 -> WeatherCode.SNOW
            else -> null
        }
    }

    /**
     * Returns cities matching a query
     */
    /*override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        return mGeocodingApi.geoCode(
            apiKey,
            query,
            types = "city",
            limit = 20,
            languageCode,
            show = "tz" // we need timezone info
        ).map {
            if (it.items == null) {
                throw LocationSearchException()
            } else {
                convert(null, it.items)
            }
        }
    }*/

    /**
     * Returns cities near provided coordinates
     */
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()

        return mRevGeocodingApi.revGeoCode(
            apiKey,
            "$latitude,$longitude",
            types = "city",
            limit = 20,
            context.currentLocale.codeWithCountry,
            show = "tz"
        ).map {
            if (it.items == null) {
                throw ReverseGeocodingException()
            }

            it.items.map { item ->
                convertLocation(item)
            }
        }
    }

    /**
     * Converts here.com geocoding result into a list of locations
     */
    private fun convertLocation(
        item: HereGeocodingData,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            latitude = item.position.lat,
            longitude = item.position.lng,
            timeZoneId = item.timeZone.name,
            country = item.address.countryName,
            countryCode = item.address.countryCode,
            admin1 = item.address.state,
            admin1Code = item.address.stateCode,
            admin2 = item.address.county,
            city = item.address.city,
            cityCode = item.id
        )
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.HERE_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_here_api_key,
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

    // Ignore, this source is getting removed on 2025-08-31
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        // private const val HERE_GEOCODING_BASE_URL = "https://geocode.search.hereapi.com/"
        private const val HERE_WEATHER_BASE_URL = "https://weather.cc.api.here.com/"
        private const val HERE_WEATHER_DEV_BASE_URL = "https://weather.cit.cc.api.here.com/"
        private const val HERE_REV_GEOCODING_BASE_URL = "https://revgeocode.search.hereapi.com/"
    }
}
