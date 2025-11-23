/*
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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.epdhk.xml.EpdHkConcentrationsResult
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class EpdHkService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : EpdHkServiceStub(context) {

    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo")
                -> "https://www.aqhi.gov.hk/tc/privacy-policy.html"
                startsWith("zh") -> "https://www.aqhi.gov.hk/sc/privacy-policy.html"
                else -> "https://www.aqhi.gov.hk/en/privacy-policy.html"
            }
        }
    }

    private val mApi by lazy {
        xmlClient
            .baseUrl(EPD_HK_BASE_URL)
            .build()
            .create(EpdHkApi::class.java)
    }

    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.aqhi.gov.hk/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getConcentrations().map {
            convert(location, it)
        }
    }

    private fun convert(
        location: Location,
        concentrationsResult: EpdHkConcentrationsResult?,
    ): WeatherWrapper {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val nearestStation = LatLng(location.latitude, location.longitude).getNearestLocation(EPD_HK_STATIONS)
        var airQuality = AirQuality()
        concentrationsResult?.pollutantConcentration?.filter {
            it.stationName.value == nearestStation
        }?.maxByOrNull { formatter.parse(it.dateTime.value) }?.let {
            airQuality = AirQuality(
                pM25 = it.pM25?.value?.toDoubleOrNull()?.microgramsPerCubicMeter,
                pM10 = it.pM10?.value?.toDoubleOrNull()?.microgramsPerCubicMeter,
                sO2 = it.sO2?.value?.toDoubleOrNull()?.microgramsPerCubicMeter,
                nO2 = it.nO2?.value?.toDoubleOrNull()?.microgramsPerCubicMeter,
                o3 = it.o3?.value?.toDoubleOrNull()?.microgramsPerCubicMeter,
                cO = it.cO?.value?.toDoubleOrNull()?.microgramsPerCubicMeter
            )
        }
        return WeatherWrapper(
            airQuality = AirQualityWrapper(
                current = airQuality
            )
        )
    }

    companion object {
        private const val EPD_HK_BASE_URL = "https://www.aqhi.gov.hk/"
    }
}
