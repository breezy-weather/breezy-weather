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
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
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
) : PollenInfoServiceStub(context) {

    override val privacyPolicyUrl = "https://www.polleninformation.at/en/our-terms-of-use"

    private val mPollenApi by lazy {
        client
            .baseUrl(POLLENINFO_BASE_URL)
            .build()
            .create(PollenInfoApi::class.java)
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

    /**
     * @param contaminationList
     * @param dayNumber: 1 for today (contamination_1), 2 for tomorrow (contamination_2), etc.
     */
    private fun getPollenForDay(
        contaminationList: List<PollenContamination>?,
        dayNumber: Int,
    ): Pollen? {
        if (contaminationList.isNullOrEmpty()) {
            return null
        }

        return Pollen(
            alder = contaminationList.firstOrNull { it.pollId == 1 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            birch = contaminationList.firstOrNull { it.pollId == 2 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            cypress = contaminationList.firstOrNull { it.pollId == 17 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            grass = contaminationList.firstOrNull { it.pollId == 5 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            hazel = contaminationList.firstOrNull { it.pollId == 3 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            mugwort = contaminationList.firstOrNull { it.pollId == 7 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            olive = contaminationList.firstOrNull { it.pollId == 18 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            plane = contaminationList.firstOrNull { it.pollId == 16 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            ragweed = contaminationList.firstOrNull { it.pollId == 6 }?.getContaminationForDay(dayNumber)?.pollenIndex,
            urticaceae = contaminationList.firstOrNull { it.pollId == 15 }
                ?.getContaminationForDay(dayNumber)?.pollenIndex,
            // TODO: Replace with Alternaria:
            mold = contaminationList.firstOrNull { it.pollId == 23 }?.getContaminationForDay(dayNumber)?.pollenIndex
            // TODO: Add rye (Secale)
            // rye = contaminationList.firstOrNull { it.pollId == 291 }?.getContaminationForDay(dayNumber)?.pollenIndex
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
        return apikey.ifEmpty { BuildConfig.POLLENINFO_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

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
        private const val POLLENINFO_BASE_URL = "https://www.polleninformation.at/"
    }
}
