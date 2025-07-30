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

package org.breezyweather.sources.ekuk

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.ekuk.json.EkukObservationsResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class EkukService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource {
    override val id = "ekuk"
    override val name = "EKUK (${Locale(context.currentLocale.code, "EE").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""
    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("et") -> "Eesti Keskkonnauuringute Keskus"
                startsWith("ru") -> "Эстонский центр экологических исследований"
                startsWith("uk") -> "Естонський центр екологічних досліджень"
                else -> "Estonian Environmental Research Center"
            }
        }
    }

    private val mApi by lazy {
        client.baseUrl(EKUK_BASE_URL)
            .build()
            .create(EkukApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to weatherAttribution
        // TODO at pollen season: SourceFeature.POLLEN to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.ohuseire.ee/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("EE", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Tallinn")
        val now = Calendar.getInstance()
        val today = formatter.format(now.time)
        now.add(Calendar.DATE, -1)
        val yesterday = formatter.format(now.time)

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            val airQualityStation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityStation") { null }
            if (!airQualityStation.isNullOrEmpty()) {
                mApi.getObservations(
                    station = airQualityStation,
                    indicators = EKUK_AIR_QUALITY_INDICATORS,
                    range = "$yesterday,$today"
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.AIR_QUALITY] = it
                    Observable.just(emptyList())
                }
            } else {
                // Do not fail: airQualityStation is empty if the nearest one is > 50km away
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val pollen = if (SourceFeature.POLLEN in requestedFeatures) {
            val pollenStation = location.parameters.getOrElse(id) { null }?.getOrElse("pollenStation") { null }
            if (!pollenStation.isNullOrEmpty()) {
                mApi.getObservations(
                    station = pollenStation,
                    indicators = EKUK_POLLEN_INDICATORS,
                    range = "$yesterday,$today"
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.POLLEN] = it
                    Observable.just(emptyList())
                }
            } else {
                // Do not fail: pollenStation is empty if the nearest one is > 50km away
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(airQuality, pollen) {
                airQualityResult: List<EkukObservationsResult>,
                pollenResult: List<EkukObservationsResult>,
            ->
            WeatherWrapper(
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(current = getAirQuality(airQualityResult))
                } else {
                    null
                },
                pollen = if (SourceFeature.POLLEN in requestedFeatures) {
                    PollenWrapper(current = getPollen(pollenResult))
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val airQualityStation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityStation") { null }
        val pollenStation = location.parameters.getOrElse(id) { null }?.getOrElse("pollenStation") { null }

        // Do not check for empty, because a valid station should be within 50km of selected location
        return (SourceFeature.AIR_QUALITY in features && airQualityStation == null) ||
            (SourceFeature.POLLEN in features && pollenStation == null)
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getStations().map {
            convert(location, it)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val EKUK_BASE_URL = "https://www.ohuseire.ee/"
        private const val EKUK_AIR_QUALITY_INDICATORS = "1,3,4,6,21,23"
        private const val EKUK_POLLEN_INDICATORS = "43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63"
    }
}
