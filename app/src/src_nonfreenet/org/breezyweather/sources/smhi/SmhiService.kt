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

package org.breezyweather.sources.smhi

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class SmhiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "smhi"
    override val name = "SMHI (${Locale(context.currentLocale.code, "SE").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl =
        "https://www.smhi.se/omsmhi/hantering-av-personuppgifter/hantering-av-personuppgifter-1.135429"

    private val mApi by lazy {
        client
            .baseUrl(SMHI_BASE_URL)
            .build()
            .create(SmhiApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to "SMHI (Creative commons Erkännande 4.0 SE)"
    )
    override val attributionLinks = mapOf(
        "SMHI" to "https://www.smhi.se/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("SE", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            location.longitude,
            location.latitude
        ).map {
            // If the API doesn’t return data, consider data as garbage and keep cached data
            if (it.timeSeries.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            WeatherWrapper(
                dailyForecast = getDailyForecast(location, it.timeSeries),
                hourlyForecast = getHourlyForecast(it.timeSeries)
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val SMHI_BASE_URL = "https://opendata-download-metfcst.smhi.se/api/"
    }
}
