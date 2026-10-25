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

package org.breezyweather.sources.cymeteodep

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.cymeteodep.xml.CyMeteoDepartmentStation
import org.breezyweather.sources.cymeteodep.xml.CyMeteoDepartmentStationObservations
import org.breezyweather.sources.cymeteodep.xml.ObservationName
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class CyMeteoDepartmentService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") client: Retrofit.Builder,
) : HttpSource(),
    WeatherSource {

    override val id = "cymeteodep"
    override val name = "Department of Meteorology (${context.currentLocale.getCountryName("CY")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(CY_METEO_DEPARTMENT_BASE_URL)
            .build()
            .create(CyMeteoDepartmentApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.CURRENT to name
    )
    override val attributionLinks = mapOf(
        name to "https://www.moa.gov.cy/moa/dm/dm.nsf/home_en/home_en?openform"
    )

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            location.countryCode.equals("CY", ignoreCase = true) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getCurrent().map {
            WeatherWrapper(
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(it.observations, getNearestStationCode(it.stations, location))
                } else {
                    null
                }
            )
        }
    }

    private fun getNearestStationCode(stations: List<CyMeteoDepartmentStation>, location: Location): String? {
        val nearestStation = stations.minByOrNull { station ->
            computeDistanceBetween(
                LatLng(station.latitude, station.longitude),
                LatLng(location.latitude, location.longitude)
            )
        }
        return nearestStation?.code
    }

    /**
     * Returns current weather
     */
    private fun getCurrent(
        stationObservations: List<CyMeteoDepartmentStationObservations>,
        stationCode: String?,
    ): CurrentWrapper? {
        if (stationCode == null) return null
        var stationObservation = stationObservations.find { stationObs -> stationObs.stationCode == stationCode }
        if (stationObservation == null) return null
        var temperature: Double? = null
        var windSpeed: Double? = null
        var relHumidity: Double? = null
        var pressure: Double? = null
        stationObservation.observations.forEach { observation ->
            when (observation.name) {
                ObservationName.TEMPERATURE.apiName -> temperature = observation.value.toDoubleOrNull()
                ObservationName.WIND_SPEED.apiName -> windSpeed = observation.value.toDoubleOrNull()
                ObservationName.RELATIVE_HUMIDITY.apiName -> relHumidity = observation.value.toDoubleOrNull()
                ObservationName.PRESSURE.apiName -> pressure = observation.value.toDoubleOrNull()
            }
        }
        return CurrentWrapper(
            temperature = TemperatureWrapper(
                temperature = temperature?.celsius
            ),
            wind = Wind(
                speed = windSpeed?.metersPerSecond
            ),
            relativeHumidity = relHumidity?.fraction,
            pressure = pressure?.hectopascals
        )
    }

    companion object {
        private const val CY_METEO_DEPARTMENT_BASE_URL = "https://dom.org.cy/AWS/OpenData/"
    }
}
