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

package org.breezyweather.sources.brightsky

import android.content.Context
import android.graphics.Color
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import org.breezyweather.sources.brightsky.json.BrightSkyWeatherResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BrightSkyService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource {

    override val id = "brightsky"
    override val name = "Bright Sky (DWD)"
    override val privacyPolicyUrl = "https://brightsky.dev/"

    override val color = Color.rgb(240, 177, 138)
    // Mandatory mentions from DWD terms:
    override val weatherAttribution = "Bright Sky, Data basis: Deutscher Wetterdienst, reproduced graphically and with missing data computed or extrapolated by Breezy Weather"

    private val mApi by lazy {
        client
            .baseUrl(BRIGHT_SKY_BASE_URL)
            .build()
            .create(BrightSkyApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val initialDate = Date().toTimezoneNoHour(location.timeZone)
        val date = initialDate!!.toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time
        val lastDate = initialDate!!.toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_YEAR, 12)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time

        val weather = mApi.getWeather(
            location.latitude,
            location.longitude,
            date.getFormattedDate(location.timeZone, "yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH),
            lastDate.getFormattedDate(location.timeZone, "yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        )

        val currentWeather = mApi.getCurrentWeather(
            location.latitude,
            location.longitude
        )

        val alerts = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                location.latitude,
                location.longitude
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(BrightSkyAlertsResult())
            }
        }

        return Observable.zip(
            weather,
            currentWeather,
            alerts
        ) {
            brightSkyWeather: BrightSkyWeatherResult,
            brightSkyCurrentWeather: BrightSkyCurrentWeatherResult,
            brightSkyAlerts: BrightSkyAlertsResult
            ->
            val languageCode = SettingsManager.getInstance(context).language.code
            convert(
                brightSkyWeather,
                brightSkyCurrentWeather,
                brightSkyAlerts,
                location.timeZone,
                languageCode
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedForLocation(
        feature: SecondaryWeatherSourceFeature, location: Location
    ): Boolean {
        return location.countryCode.equals("DE", ignoreCase = true)
    }
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val alertsResult = mApi.getAlerts(
            location.latitude,
            location.longitude
        )

        return alertsResult.map {
            val languageCode = SettingsManager.getInstance(context).language.code
            convertSecondary(it, languageCode)
        }
    }

    companion object {
        private const val BRIGHT_SKY_BASE_URL = "https://api.brightsky.dev/"
    }
}