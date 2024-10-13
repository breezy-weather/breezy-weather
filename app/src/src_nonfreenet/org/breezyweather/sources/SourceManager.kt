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

package org.breezyweather.sources

import android.content.Context
import breezyweather.domain.location.model.Location
import org.breezyweather.BuildConfig
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.PreferencesParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.source.Source
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.accu.AccuService
import org.breezyweather.sources.android.AndroidLocationService
import org.breezyweather.sources.atmoaura.AtmoAuraService
import org.breezyweather.sources.baiduip.BaiduIPLocationService
import org.breezyweather.sources.breezydatashare.BreezyService
import org.breezyweather.sources.brightsky.BrightSkyService
import org.breezyweather.sources.china.ChinaService
import org.breezyweather.sources.cwa.CwaService
import org.breezyweather.sources.dmi.DmiService
import org.breezyweather.sources.eccc.EcccService
import org.breezyweather.sources.gadgetbridge.GadgetbridgeService
import org.breezyweather.sources.geonames.GeoNamesService
import org.breezyweather.sources.geosphereat.GeoSphereAtService
import org.breezyweather.sources.here.HereService
import org.breezyweather.sources.ims.ImsService
import org.breezyweather.sources.ipsb.IpSbLocationService
import org.breezyweather.sources.meteoam.MeteoAmService
import org.breezyweather.sources.metie.MetIeService
import org.breezyweather.sources.metno.MetNoService
import org.breezyweather.sources.mf.MfService
import org.breezyweather.sources.naturalearth.NaturalEarthService
import org.breezyweather.sources.nws.NwsService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.openweather.OpenWeatherService
import org.breezyweather.sources.pirateweather.PirateWeatherService
import org.breezyweather.sources.recosante.RecosanteService
import org.breezyweather.sources.smhi.SmhiService
import org.breezyweather.sources.wmosevereweather.WmoSevereWeatherService
import javax.inject.Inject

class SourceManager @Inject constructor(
    accuService: AccuService,
    androidLocationService: AndroidLocationService,
    atmoAuraService: AtmoAuraService,
    baiduIPService: BaiduIPLocationService,
    breezyService: BreezyService,
    brightSkyService: BrightSkyService,
    chinaService: ChinaService,
    cwaService: CwaService,
    dmiService: DmiService,
    ecccService: EcccService,
    gadgetbridgeService: GadgetbridgeService,
    geoNamesService: GeoNamesService,
    geoSphereAtService: GeoSphereAtService,
    hereService: HereService,
    imsService: ImsService,
    ipSbService: IpSbLocationService,
    meteoAmService: MeteoAmService,
    metIeService: MetIeService,
    metNoService: MetNoService,
    mfService: MfService,
    naturalEarthService: NaturalEarthService,
    nwsService: NwsService,
    openMeteoService: OpenMeteoService,
    openWeatherService: OpenWeatherService,
    pirateWeatherService: PirateWeatherService,
    recosanteService: RecosanteService,
    smhiService: SmhiService,
    wmoSevereWeatherService: WmoSevereWeatherService
) {
    // TODO: Initialize lazily
    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: List<Source> = listOf(
        // Location sources
        androidLocationService,
        ipSbService,
        baiduIPService,

        // Location search sources
        geoNamesService,

        // Reverse geocoding sources
        naturalEarthService,

        // Weather sources
        openMeteoService,
        accuService,
        metNoService,
        openWeatherService,
        pirateWeatherService,
        hereService,

        // National sources supporting worldwide
        mfService,
        dmiService,
        meteoAmService,

        // National-only sources (sorted by population)
        chinaService,
        nwsService,
        geoSphereAtService, // Austria and nearby
        brightSkyService,
        ecccService,
        cwaService,
        imsService,
        smhiService,
        metIeService,

        // Secondary weather sources
        wmoSevereWeatherService,
        recosanteService,
        atmoAuraService,

        // Broadcast sources
        breezyService,
        gadgetbridgeService
    )

    fun getSource(id: String): Source? = sourceList.firstOrNull { it.id == id }
    fun getHttpSources(): List<HttpSource> = sourceList.filterIsInstance<HttpSource>()

    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getConfiguredLocationSources(): List<LocationSource> = getLocationSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id)
        ?: getLocationSource(BuildConfig.DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getMainWeatherSources(): List<MainWeatherSource> = sourceList.filterIsInstance<MainWeatherSource>()
    fun getMainWeatherSource(id: String): MainWeatherSource? = getMainWeatherSources().firstOrNull { it.id == id }
    fun getConfiguredMainWeatherSources(): List<MainWeatherSource> = getMainWeatherSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Secondary weather
    fun getSecondaryWeatherSources(): List<SecondaryWeatherSource> = sourceList.filterIsInstance<SecondaryWeatherSource>()
    fun getSecondaryWeatherSource(id: String): SecondaryWeatherSource? = getSecondaryWeatherSources().firstOrNull { it.id == id }
    fun getPollenIndexSource(id: String): PollenIndexSource? = sourceList.filterIsInstance<PollenIndexSource>().firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources().firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id)
        ?: getLocationSearchSource(BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getConfiguredLocationSearchSources(): List<LocationSearchSource> = getLocationSearchSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> = sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources().firstOrNull { it.id == id }
    fun getReverseGeocodingSourceOrDefault(id: String): ReverseGeocodingSource = getReverseGeocodingSource(id)
        ?: getReverseGeocodingSource(BuildConfig.DEFAULT_GEOCODING_SOURCE)!!

    // Broadcast
    fun getBroadcastSources(): List<BroadcastSource> = sourceList.filterIsInstance<BroadcastSource>()
    fun isBroadcastSourcesEnabled(context: Context): Boolean {
        return getBroadcastSources().any {
            (SourceConfigStore(context, it.id).getString("packages", null) ?: "").isNotEmpty()
        }
    }

    // Configurables sources
    fun getConfigurableSources(): List<ConfigurableSource> = sourceList.filterIsInstance<ConfigurableSource>()

    fun sourcesWithPreferencesScreen(
        location: Location
    ): List<PreferencesParametersSource> {
        val preferencesScreenSources = mutableListOf<PreferencesParametersSource>()

        val mainSource = getMainWeatherSource(location.weatherSource)
        if (mainSource is PreferencesParametersSource &&
            mainSource.hasPreferencesScreen(location, emptyList())) {
            preferencesScreenSources.add(mainSource)
        }

        with(location) {
            listOf(
                Pair(airQualitySource, SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY),
                Pair(pollenSource, SecondaryWeatherSourceFeature.FEATURE_POLLEN),
                Pair(minutelySource, SecondaryWeatherSourceFeature.FEATURE_MINUTELY),
                Pair(alertSource, SecondaryWeatherSourceFeature.FEATURE_ALERT),
                Pair(normalsSource, SecondaryWeatherSourceFeature.FEATURE_NORMALS)
            ).forEach {
                val secondarySource = getSecondaryWeatherSource(it.first ?: location.weatherSource)
                if (secondarySource is PreferencesParametersSource &&
                    secondarySource.hasPreferencesScreen(location, listOf(it.second)) &&
                    !preferencesScreenSources.contains(secondarySource)) {
                    preferencesScreenSources.add(secondarySource)
                }
            }
        }

        return preferencesScreenSources
            /*.sortedWith { s1, s2 -> // Sort by name because there are now a lot of sources
                Collator.getInstance(
                    SettingsManager.getInstance(context).language.locale
                ).compare(s1.name, s2.name)
            })*/
    }
}
