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

package org.breezyweather.sources.lhmt

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.lhmt.json.LhmtAlertsResult
import org.breezyweather.sources.lhmt.json.LhmtLocationsResult
import org.breezyweather.sources.lhmt.json.LhmtWeatherResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class LhmtService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "lhmt"
    override val name = "LHMT (${context.currentLocale.getCountryName("LT")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.meteo.lt/istaiga/asmens-duomenu-apsauga/privatumo-politika/"

    private val mApi by lazy {
        client.baseUrl(LHMT_BASE_URL)
            .build()
            .create(LhmtApi::class.java)
    }

    private val mWwwApi by lazy {
        client.baseUrl(LHMT_WWW_BASE_URL)
            .build()
            .create(LhmtWwwApi::class.java)
    }

    private val weatherAttribution = "Lietuvos hidrometeorologijos tarnyba"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to LHMT_WWW_BASE_URL
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("LT", ignoreCase = true)
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
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
        val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }
        if (forecastLocation.isNullOrEmpty() ||
            currentLocation.isNullOrEmpty() ||
            municipality.isNullOrEmpty() ||
            county.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(forecastLocation).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(LhmtWeatherResult())
            }
        } else {
            Observable.just(LhmtWeatherResult())
        }
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(currentLocation).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(LhmtWeatherResult())
            }
        } else {
            Observable.just(LhmtWeatherResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mWwwApi.getAlertList().map { list ->
                val path = list.first().substringAfter(LHMT_WWW_BASE_URL)
                mWwwApi.getAlerts(path).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(LhmtAlertsResult())
                }.blockingFirst()
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(LhmtAlertsResult())
            }
        } else {
            Observable.just(LhmtAlertsResult())
        }

        return Observable.zip(current, forecast, alerts) {
                currentResult: LhmtWeatherResult,
                forecastResult: LhmtWeatherResult,
                alertsResult: LhmtAlertsResult,
            ->
            val hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                getHourlyForecast(context, forecastResult)
            } else {
                null
            }
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(hourlyForecast)
                } else {
                    null
                },
                hourlyForecast = hourlyForecast,
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, location, alertsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestNearestLocation(
        context: Context,
        location: Location,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getForecastLocations().map {
            convertLocation(location, it)
        }
    }

    // reverse geocoding
    private fun convertLocation(
        location: Location,
        forecastLocations: List<LhmtLocationsResult>,
    ): List<LocationAddressInfo> {
        val locationList = mutableListOf<LocationAddressInfo>()
        val forecastLocationMap = forecastLocations
            .filter { it.countryCode == null || it.countryCode.equals("LT", ignoreCase = true) }
            .associate { it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude) }
        val forecastLocation = LatLng(location.latitude, location.longitude)
            .getNearestLocation(forecastLocationMap, 50000.0)
        forecastLocations.firstOrNull { it.code == forecastLocation }?.let {
            val municipalityName = it.administrativeDivision
            val municipalityCode = MUNICIPALITIES.firstOrNull { pair ->
                pair.second == municipalityName
            }?.first
            val countyCode = COUNTIES_MUNICIPALITIES.firstOrNull { pair ->
                pair.second == municipalityCode
            }?.first
            val countyName = COUNTIES.firstOrNull { pair ->
                pair.first == countyCode
            }?.second
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = "Europe/Vilnius",
                    countryCode = "LT",
                    admin1 = countyName,
                    admin1Code = countyCode,
                    admin2 = municipalityName,
                    admin2Code = municipalityCode,
                    city = it.name
                )
            )
        }
        return locationList
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
        val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }

        return forecastLocation.isNullOrEmpty() ||
            currentLocation.isNullOrEmpty() ||
            municipality.isNullOrEmpty() ||
            county.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val forecastLocations = mApi.getForecastLocations()
        val currentLocations = mApi.getCurrentLocations()
        return Observable.zip(forecastLocations, currentLocations) {
                forecastLocationsResult: List<LhmtLocationsResult>,
                currentLocationsResult: List<LhmtLocationsResult>,
            ->
            convert(location, forecastLocationsResult, currentLocationsResult)
        }
    }

    // location parameters
    private fun convert(
        location: Location,
        forecastLocations: List<LhmtLocationsResult>,
        currentLocations: List<LhmtLocationsResult>,
    ): Map<String, String> {
        val forecastLocationMap = forecastLocations
            .filter { it.countryCode == null || it.countryCode.equals("LT", ignoreCase = true) }
            .associate { it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude) }
        val forecastLocation = LatLng(location.latitude, location.longitude)
            .getNearestLocation(forecastLocationMap, 50000.0)

        val currentLocationMap = currentLocations
            .filter { it.countryCode == null || it.countryCode.equals("LT", ignoreCase = true) }
            .associate { it.code to LatLng(it.coordinates.latitude, it.coordinates.longitude) }
        val currentLocation = LatLng(location.latitude, location.longitude)
            .getNearestLocation(currentLocationMap, 50000.0)

        val municipalityName = forecastLocations.firstOrNull { it.code == forecastLocation }?.administrativeDivision
        val municipalityCode = MUNICIPALITIES.firstOrNull { pair ->
            pair.second == municipalityName
        }?.first
        val countyCode = COUNTIES_MUNICIPALITIES.firstOrNull { pair ->
            pair.second == municipalityCode
        }?.first

        if (forecastLocation == null || currentLocation == null || municipalityCode == null || countyCode == null) {
            throw InvalidLocationException()
        }

        return mapOf(
            "forecastLocation" to forecastLocation,
            "currentLocation" to currentLocation,
            "municipality" to municipalityCode,
            "county" to countyCode
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val LHMT_BASE_URL = "https://api.meteo.lt/"
        private const val LHMT_WWW_BASE_URL = "https://www.meteo.lt/"

        // The municipality codes used by LHMT is obtained from any of their warning files,
        // e.g. https://www.meteo.lt/meteo_jobs/pavojingi_met_reisk_ibl/20240910115424-00000280
        //
        // The codes are then matched to the names given by this endpoint:
        // https://api.meteo.lt/v1/places
        //
        // We use the long names from that endpoint rather than the shortened names in the warning file,
        // so that we can directly assign location parameters without further manipulation of the output.
        //
        // These codes are used only by LHMT for identifying whether an alert applies to a municipality.
        // They are not related to Lithuania's ISO 3166-2 subdivision codes.
        private val MUNICIPALITIES = listOf(
            Pair("LT032", "Akmenės rajono savivaldybė"),
            Pair("LT011", "Alytaus miesto savivaldybė"),
            Pair("LT033", "Alytaus rajono savivaldybė"),
            Pair("LT034", "Anykščių rajono savivaldybė"),
            Pair("LT012", "Birštono savivaldybė"),
            Pair("LT036", "Biržų rajono savivaldybė"),
            Pair("LT015", "Druskininkų savivaldybė"),
            Pair("LT042", "Elektrėnų savivaldybė"),
            Pair("LT045", "Ignalinos rajono savivaldybė"),
            Pair("LT046", "Jonavos rajono savivaldybė"),
            Pair("LT047", "Joniškio rajono savivaldybė"),
            Pair("LT094", "Jurbarko rajono savivaldybė"),
            Pair("LT049", "Kaišiadorių rajono savivaldybė"),
            Pair("LT048", "Kalvarijos savivaldybė"),
            Pair("LT019", "Kauno miesto savivaldybė"),
            Pair("LT052", "Kauno rajono savivaldybė"),
            Pair("LT058", "Kazlų Rūdos savivaldybė"),
            Pair("LT053", "Kelmės rajono savivaldybė"),
            Pair("LT054", "Klaipėdos miesto savivaldybė"),
            Pair("LT021", "Klaipėdos rajono savivaldybė"),
            Pair("LT055", "Kretingos rajono savivaldybė"),
            Pair("LT056", "Kupiškio rajono savivaldybė"),
            Pair("LT057", "Kėdainių rajono savivaldybė"),
            Pair("LT059", "Lazdijų rajono savivaldybė"),
            Pair("LT018", "Marijampolės savivaldybė"),
            Pair("LT061", "Mažeikių rajono savivaldybė"),
            Pair("LT062", "Molėtų rajono savivaldybė"),
            Pair("LT023", "Neringos miesto savivaldybė"),
            Pair("LT063", "Pagėgių savivaldybė"),
            Pair("LT065", "Pakruojo rajono savivaldybė"),
            Pair("LT025", "Palangos miesto savivaldybė"),
            Pair("LT027", "Panevėžio miesto savivaldybė"),
            Pair("LT066", "Panevėžio rajono savivaldybė"),
            Pair("LT067", "Pasvalio rajono savivaldybė"),
            Pair("LT068", "Plungės rajono savivaldybė"),
            Pair("LT069", "Prienų rajono savivaldybė"),
            Pair("LT071", "Radviliškio rajono savivaldybė"),
            Pair("LT072", "Raseinių rajono savivaldybė"),
            Pair("LT074", "Rietavo savivaldybė"),
            Pair("LT073", "Rokiškio rajono savivaldybė"),
            Pair("LT084", "Šakių rajono savivaldybė"),
            Pair("LT085", "Šalčininkų rajono savivaldybė"),
            Pair("LT029", "Šiaulių miesto savivaldybė"),
            Pair("LT091", "Šiaulių rajono savivaldybė"),
            Pair("LT087", "Šilalės rajono savivaldybė"),
            Pair("LT088", "Šilutės rajono savivaldybė"),
            Pair("LT089", "Širvintų rajono savivaldybė"),
            Pair("LT075", "Skuodo rajono savivaldybė"),
            Pair("LT086", "Švenčionių rajono savivaldybė"),
            Pair("LT077", "Tauragės rajono savivaldybė"),
            Pair("LT078", "Telšių rajono savivaldybė"),
            Pair("LT079", "Trakų rajono savivaldybė"),
            Pair("LT081", "Ukmergės rajono savivaldybė"),
            Pair("LT082", "Utenos rajono savivaldybė"),
            Pair("LT038", "Varėnos rajono savivaldybė"),
            Pair("LT039", "Vilkaviškio rajono savivaldybė"),
            Pair("LT013", "Vilniaus miesto savivaldybė"),
            Pair("LT041", "Vilniaus rajono savivaldybė"),
            Pair("LT030", "Visagino savivaldybė"),
            Pair("LT043", "Zarasų rajono savivaldybė")
        )

        // The county codes used by LHMT is obtained from any of their warning files,
        // e.g. https://www.meteo.lt/meteo_jobs/pavojingi_met_reisk_ibl/20240910115424-00000280
        //
        // These codes are used only by LHMT for identifying whether an alert applies to a municipality.
        // They are not related to Lithuania's ISO 3166-2 subdivision codes.
        private val COUNTIES = listOf(
            Pair("LT001", "Alytaus apskritis"),
            Pair("LT002", "Kauno apskritis"),
            Pair("LT003", "Klaipėdos apskritis"),
            Pair("LT004", "Marijampolės apskritis"),
            Pair("LT005", "Panevėžio apskritis"),
            Pair("LT006", "Šiaulių apskritis"),
            Pair("LT007", "Tauragės apskritis"),
            Pair("LT008", "Telšių apskritis"),
            Pair("LT009", "Utenos apskritis"),
            Pair("LT010", "Vilniaus apskritis")
        )

        // The above LHMT county and municipality codes are matched according to this table:
        // https://en.wikipedia.org/wiki/Municipalities_of_Lithuania#Municipalities
        private val COUNTIES_MUNICIPALITIES = listOf(
            Pair("LT006", "LT032"), // Šiaulių apskritis -> Akmenės rajono savivaldybė
            Pair("LT001", "LT011"), // Alytaus apskritis -> Alytaus miesto savivaldybė
            Pair("LT001", "LT033"), // Alytaus apskritis -> Alytaus rajono savivaldybė
            Pair("LT009", "LT034"), // Utenos apskritis -> Anykščių rajono savivaldybė
            Pair("LT002", "LT012"), // Kauno apskritis -> Birštono savivaldybė
            Pair("LT005", "LT036"), // Panevėžio apskritis -> Biržų rajono savivaldybė
            Pair("LT001", "LT015"), // Alytaus apskritis -> Druskininkų savivaldybė
            Pair("LT010", "LT042"), // Vilniaus apskriti -> Elektrėnų savivaldybė
            Pair("LT009", "LT045"), // Utenos apskritis -> Ignalinos rajono savivaldybė
            Pair("LT002", "LT046"), // Kauno apskritis -> Jonavos rajono savivaldybė
            Pair("LT006", "LT047"), // Šiaulių apskritis -> Joniškio rajono savivaldybė
            Pair("LT007", "LT094"), // Tauragės apskritis -> Jurbarko rajono savivaldybė
            Pair("LT002", "LT049"), // Kauno apskritis -> Kaišiadorių rajono savivaldybė
            Pair("LT004", "LT048"), // Marijampolės apskritis -> Kalvarijos savivaldybė
            Pair("LT002", "LT019"), // Kauno apskritis -> Kauno miesto savivaldybė
            Pair("LT002", "LT052"), // Kauno apskritis -> Kauno rajono savivaldybė
            Pair("LT004", "LT058"), // Marijampolės apskritis -> Kazlų Rūdos savivaldybė
            Pair("LT006", "LT053"), // Šiaulių apskritis -> Kelmės rajono savivaldybė
            Pair("LT003", "LT054"), // Klaipėdos apskritis -> Klaipėdos miesto savivaldybė
            Pair("LT003", "LT021"), // Klaipėdos apskritis -> Klaipėdos rajono savivaldybė
            Pair("LT003", "LT055"), // Klaipėdos apskritis -> Kretingos rajono savivaldybė
            Pair("LT005", "LT056"), // Panevėžio apskritis -> Kupiškio rajono savivaldybė
            Pair("LT002", "LT057"), // Kauno apskritis -> Kėdainių rajono savivaldybė
            Pair("LT001", "LT059"), // Alytaus apskritis -> Lazdijų rajono savivaldybė
            Pair("LT004", "LT018"), // Marijampolės apskritis -> Marijampolės savivaldybė
            Pair("LT008", "LT061"), // Telšių apskritis -> Mažeikių rajono savivaldybė
            Pair("LT009", "LT062"), // Utenos apskritis -> Molėtų rajono savivaldybė
            Pair("LT003", "LT023"), // Klaipėdos apskritis -> Neringos miesto savivaldybė
            Pair("LT007", "LT063"), // Tauragės apskritis -> Pagėgių savivaldybė
            Pair("LT006", "LT065"), // Šiaulių apskritis -> Pakruojo rajono savivaldybė
            Pair("LT003", "LT025"), // Klaipėdos apskritis -> Palangos miesto savivaldybė
            Pair("LT005", "LT027"), // Panevėžio apskritis -> Panevėžio miesto savivaldybė
            Pair("LT005", "LT066"), // Panevėžio apskritis -> Panevėžio rajono savivaldybė
            Pair("LT005", "LT067"), // Panevėžio apskritis -> Pasvalio rajono savivaldybė
            Pair("LT008", "LT068"), // Telšių apskritis -> Plungės rajono savivaldybė
            Pair("LT002", "LT069"), // Kauno apskritis -> Prienų rajono savivaldybė
            Pair("LT006", "LT071"), // Šiaulių apskritis -> Radviliškio rajono savivaldybė
            Pair("LT002", "LT072"), // Kauno apskritis -> Raseinių rajono savivaldybė
            Pair("LT008", "LT074"), // Telšių apskritis -> Rietavo savivaldybė
            Pair("LT005", "LT073"), // Panevėžio apskritis -> Rokiškio rajono savivaldybė
            Pair("LT004", "LT084"), // Marijampolės apskritis -> Šakių rajono savivaldybė
            Pair("LT010", "LT085"), // Vilniaus apskriti -> Šalčininkų rajono savivaldybė
            Pair("LT006", "LT029"), // Šiaulių apskritis -> Šiaulių miesto savivaldybė
            Pair("LT006", "LT091"), // Šiaulių apskritis -> Šiaulių rajono savivaldybė
            Pair("LT007", "LT087"), // Tauragės apskritis -> Šilalės rajono savivaldybė
            Pair("LT003", "LT088"), // Klaipėdos apskritis -> Šilutės rajono savivaldybė
            Pair("LT010", "LT089"), // Vilniaus apskriti -> Širvintų rajono savivaldybė
            Pair("LT003", "LT075"), // Klaipėdos apskritis -> Skuodo rajono savivaldybė
            Pair("LT010", "LT086"), // Vilniaus apskriti -> Švenčionių rajono savivaldybė
            Pair("LT007", "LT077"), // Tauragės apskritis -> Tauragės rajono savivaldybė
            Pair("LT008", "LT078"), // Telšių apskritis -> Telšių rajono savivaldybė
            Pair("LT010", "LT079"), // Vilniaus apskriti -> Trakų rajono savivaldybė
            Pair("LT010", "LT081"), // Vilniaus apskriti -> Ukmergės rajono savivaldybė
            Pair("LT009", "LT082"), // Utenos apskritis -> Utenos rajono savivaldybė
            Pair("LT001", "LT038"), // Alytaus apskritis -> Varėnos rajono savivaldybė
            Pair("LT004", "LT039"), // Marijampolės apskritis -> Vilkaviškio rajono savivaldybė
            Pair("LT010", "LT013"), // Vilniaus apskriti -> Vilniaus miesto savivaldybė
            Pair("LT010", "LT041"), // Vilniaus apskriti -> Vilniaus rajono savivaldybė
            Pair("LT009", "LT030"), // Utenos apskritis -> Visagino savivaldybė
            Pair("LT009", "LT043") // Utenos apskritis -> Zarasų rajono savivaldyb
        )
    }
}
