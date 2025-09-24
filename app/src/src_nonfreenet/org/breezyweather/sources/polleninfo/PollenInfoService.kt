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

package org.breezyweather.sources.polleninfo

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGH
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.polleninfo.json.PollenContamination
import org.breezyweather.sources.polleninfo.json.PollenInfoResult
import org.breezyweather.unit.pollen.PollenConcentration.Companion.pollenIndex
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class PollenInfoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, PollenIndexSource, ConfigurableSource {

    override val id = "polleninfo"
    private val countryName = context.currentLocale.getCountryName("AT")
    override val name = "Pollen Information Service".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.polleninformation.at/en/our-terms-of-use"

    private val mPollenApi by lazy {
        client
            .baseUrl(POLLENFINO_BASE_URL)
            .build()
            .create(PollenInfoApi::class.java)
    }

    private val weatherAttribution = "Österreichischer Polleninformationsdienst (Creative Commons Attribution 4.0)"
    override val supportedFeatures = mapOf(
        SourceFeature.POLLEN to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Österreichischer Polleninformationsdienst" to "www.polleninformation.at"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.POLLEN -> SUPPORTED_COUNTRY_CODES.any { it.equals(location.countryCode, ignoreCase = true) }
            else -> false
        }
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            feature == SourceFeature.POLLEN &&
                location.countryCode.equals("AT")
            -> PRIORITY_HIGH
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        return mPollenApi.getData(
            twoLetterIsoCountryCode = location.countryCode ?: "AT",
            twoLetterIsoLanguageCode = "en",
            latitude = location.latitude,
            longitude = location.longitude,
            apikey = apiKey
        ).map {
            WeatherWrapper(
                pollen = getPollen(location, it)
            )
        }
    }

    private fun getPollen(
        location: Location,
        result: PollenInfoResult,
    ): PollenWrapper? {
        val dailyForecast = mutableMapOf<Date, Pollen>()
        val today = Date().toTimezoneSpecificHour(location.timeZone)

        // The API provides contamination data for up to 4 days (today, tomorrow, etc.)
        for (dayIndex in 0..3) {
            val currentDate = today.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_MONTH, dayIndex)
            }.time

            val pollenForDay = getPollenForDay(result.contamination, dayIndex + 1)
            if (pollenForDay != null) {
                dailyForecast[currentDate] = pollenForDay
            }
        }

        return PollenWrapper(
            dailyForecast = dailyForecast
        )
    }

    private fun getPollenForDay(
        contaminationList: List<PollenContamination>?,
        dayNumber: Int, // 1 for today (contamination_1), 2 for tomorrow (contamination_2), etc.
    ): Pollen? {
        if (contaminationList.isNullOrEmpty()) {
            return null
        }

        fun PollenContamination.getContaminationForDay(day: Int): Int? =
            when (day) {
                1 -> contamination1
                2 -> contamination2
                3 -> contamination3
                4 -> contamination4
                else -> null
            }

        var alder: Int? = null
        var birch: Int? = null
        var cypress: Int? = null
        var grass: Int? = null
        var hazel: Int? = null
        var mugwort: Int? = null
        var olive: Int? = null
        var plane: Int? = null
        var ragweed: Int? = null
        var urticaceae: Int? = null
        var mold: Int? = null

        contaminationList.forEach { contamination ->
            val value = contamination.getContaminationForDay(dayNumber)
            if (value != null) {
                when (contamination.pollId) {
                    1 -> alder = value // alder (Alnus)
                    2 -> birch = value // birch (Betula)
                    3 -> hazel = value // hazel (Corylus)
                    5 -> grass = value // grasses (Poaceae)
                    6 -> ragweed = value // ragweed (Ambrosia)
                    7 -> mugwort = value // mugwort (Artemisia)
                    15 -> urticaceae = value // nettle family (Urticaceae)
                    16 -> plane = value // plane tree (Platanus)
                    17 -> cypress = value // cypress family (Cupressaceae)
                    18 -> olive = value // olive (Olea)
                    23 -> mold = value // fungal spores (Alternaria)
                    //  291 -> rye = value // rye (Secale) - does not exist in BW, could be part of grasses
                }
            }
        }

        return Pollen(
            alder = alder?.pollenIndex,
            birch = birch?.pollenIndex,
            cypress = cypress?.pollenIndex,
            grass = grass?.pollenIndex,
            hazel = hazel?.pollenIndex,
            mugwort = mugwort?.pollenIndex,
            olive = olive?.pollenIndex,
            plane = plane?.pollenIndex,
            ragweed = ragweed?.pollenIndex,
            urticaceae = urticaceae?.pollenIndex,
            mold = mold?.pollenIndex
        )
    }

    override val pollenLabels = R.array.pollen_levels_polleninfo
    override val pollenColors = R.array.pollen_level_colors

    // CONFIG

    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""
    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.POLLENINFO_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_polleninfo_api_key,
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
        private const val POLLENFINO_BASE_URL = "https://www.polleninformation.at/"
        private val SUPPORTED_COUNTRY_CODES: List<String> = listOf(
            "AT", // Austria
            "CH", // Switzerland
            "DE", // Germany
            "ES", // Spain
            "FR", // France
            "GB", // United Kingdom
            "IT", // Italy
            "LT", // Lithuania
            "LV", // Latvia
            "PL", // Poland
            "SE", // Sweden
            "TR", // Turkey
            "UA" // Ukraine
        )
    }
}
