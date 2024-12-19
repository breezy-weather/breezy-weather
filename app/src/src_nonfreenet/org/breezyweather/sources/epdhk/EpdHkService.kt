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

package org.breezyweather.sources.epdhk

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class EpdHkService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), WeatherSource {
    override val id = "epdhk"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "環境保護署"
                startsWith("zh") -> "环境保护署"
                else -> "EPD"
            }
        } + " (${Locale(context.currentLocale.code, "HK").displayCountry})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                equals(
                    "zh-tw"
                ) ||
                    equals("zh-hk") ||
                    equals("zh-mo") -> "https://www.aqhi.gov.hk/tc/privacy-policy.html"
                startsWith("zh") -> "https://www.aqhi.gov.hk/sc/privacy-policy.html"
                else -> "https://www.aqhi.gov.hk/en/privacy-policy.html"
            }
        }
    }
    override val color = Color.rgb(78, 184, 72)

    private val mApi by lazy {
        xmlClient
            .baseUrl(EPD_HK_BASE_URL)
            .build()
            .create(EpdHkApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "環境保護署"
                startsWith("zh") -> "环境保护署"
                else -> "Environmental Protection Department"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("HK", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val concentrations = mApi.getConcentrations().execute().body()
        return Observable.just(
            convert(location, concentrations)
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val EPD_HK_BASE_URL = "https://www.aqhi.gov.hk/"
    }
}
