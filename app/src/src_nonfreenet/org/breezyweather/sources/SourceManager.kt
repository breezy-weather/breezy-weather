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
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.BuildConfig
import org.breezyweather.common.extensions.currentLocale
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
import org.breezyweather.sources.bmkg.BmkgService
import org.breezyweather.sources.brightsky.BrightSkyService
import org.breezyweather.sources.china.ChinaService
import org.breezyweather.sources.cwa.CwaService
import org.breezyweather.sources.dmi.DmiService
import org.breezyweather.sources.eccc.EcccService
import org.breezyweather.sources.gadgetbridge.GadgetbridgeService
import org.breezyweather.sources.geonames.GeoNamesService
import org.breezyweather.sources.geosphereat.GeoSphereAtService
import org.breezyweather.sources.here.HereService
import org.breezyweather.sources.hko.HkoService
import org.breezyweather.sources.imd.ImdService
import org.breezyweather.sources.ims.ImsService
import org.breezyweather.sources.ipma.IpmaService
import org.breezyweather.sources.ipsb.IpSbLocationService
import org.breezyweather.sources.jma.JmaService
import org.breezyweather.sources.meteoam.MeteoAmService
import org.breezyweather.sources.meteolux.MeteoLuxService
import org.breezyweather.sources.metie.MetIeService
import org.breezyweather.sources.metno.MetNoService
import org.breezyweather.sources.metoffice.MetOfficeService
import org.breezyweather.sources.mf.MfService
import org.breezyweather.sources.mgm.MgmService
import org.breezyweather.sources.namem.NamemService
import org.breezyweather.sources.naturalearth.NaturalEarthService
import org.breezyweather.sources.nws.NwsService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.openweather.OpenWeatherService
import org.breezyweather.sources.pagasa.PagasaService
import org.breezyweather.sources.pirateweather.PirateWeatherService
import org.breezyweather.sources.recosante.RecosanteService
import org.breezyweather.sources.smg.SmgService
import org.breezyweather.sources.smhi.SmhiService
import org.breezyweather.sources.wmosevereweather.WmoSevereWeatherService
import java.text.Collator
import javax.inject.Inject

class SourceManager @Inject constructor(
    @ApplicationContext context: Context,
    accuService: AccuService,
    androidLocationService: AndroidLocationService,
    atmoAuraService: AtmoAuraService,
    baiduIPService: BaiduIPLocationService,
    bmkgService: BmkgService,
    brightSkyService: BrightSkyService,
    chinaService: ChinaService,
    cwaService: CwaService,
    dmiService: DmiService,
    ecccService: EcccService,
    gadgetbridgeService: GadgetbridgeService,
    geoNamesService: GeoNamesService,
    geoSphereAtService: GeoSphereAtService,
    hereService: HereService,
    hkoService: HkoService,
    imdService: ImdService,
    imsService: ImsService,
    ipmaService: IpmaService,
    ipSbService: IpSbLocationService,
    jmaService: JmaService,
    meteoAmService: MeteoAmService,
    meteoLuxService: MeteoLuxService,
    metIeService: MetIeService,
    metNoService: MetNoService,
    metOfficeService: MetOfficeService,
    mfService: MfService,
    mgmService: MgmService,
    namemService: NamemService,
    naturalEarthService: NaturalEarthService,
    nwsService: NwsService,
    openMeteoService: OpenMeteoService,
    openWeatherService: OpenWeatherService,
    pagasaService: PagasaService,
    pirateWeatherService: PirateWeatherService,
    recosanteService: RecosanteService,
    smgService: SmgService,
    smhiService: SmhiService,
    wmoSevereWeatherService: WmoSevereWeatherService,
) {
    // TODO: Initialize lazily

    // Location sources
    private val locationSourceList = listOf(
        androidLocationService,
        ipSbService,
        baiduIPService
    )

    // Location search sources
    private val locationSearchSourceList = listOf(
        geoNamesService
    )

    // Reverse geocoding sources
    private val reverseGeocodingSourceList = listOf(
        naturalEarthService
    )

    // Worldwide weather sources, excluding national sources with worldwide support,
    // with the exception of MET Norway
    private val worldwideWeatherSourceList = listOf(
        openMeteoService,
        accuService,
        hereService,
        metNoService,
        openWeatherService,
        pirateWeatherService
    )

    // Region-specific or national weather sources
    private val nationalWeatherSourceList = listOf(
        bmkgService,
        brightSkyService,
        chinaService,
        cwaService,
        dmiService,
        ecccService,
        geoSphereAtService,
        hkoService,
        imdService,
        imsService,
        ipmaService,
        jmaService,
        meteoAmService,
        meteoLuxService,
        metIeService,
        metOfficeService,
        mfService,
        mgmService,
        namemService,
        nwsService,
        pagasaService,
        smgService,
        smhiService
    )

    // Secondary weather sources
    private val secondaryWeatherSourceList = listOf(
        wmoSevereWeatherService,
        atmoAuraService,
        recosanteService
    )

    // Broadcast sources
    private val broadcastSourceList = listOf(
        gadgetbridgeService
    )

    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: List<Source> = buildList {
        addAll(locationSourceList)
        addAll(locationSearchSourceList)
        addAll(reverseGeocodingSourceList)
        addAll(worldwideWeatherSourceList)
        addAll(
            nationalWeatherSourceList
                .sortedWith { ws1, ws2 ->
                    // Sort by name because there are now a lot of sources
                    Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
                }
        )
        addAll(secondaryWeatherSourceList)
        addAll(broadcastSourceList)
    }

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
    fun getConfiguredMainWeatherSources(): List<MainWeatherSource> = getMainWeatherSources()
        .filter { it !is ConfigurableSource || it.isConfigured }

    // Secondary weather
    fun getSecondaryWeatherSources(): List<SecondaryWeatherSource> =
        sourceList.filterIsInstance<SecondaryWeatherSource>()
    fun getSecondaryWeatherSource(id: String): SecondaryWeatherSource? = getSecondaryWeatherSources()
        .firstOrNull { it.id == id }
    fun getPollenIndexSource(id: String): PollenIndexSource? = sourceList.filterIsInstance<PollenIndexSource>()
        .firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources()
        .firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id)
        ?: getLocationSearchSource(BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getConfiguredLocationSearchSources(): List<LocationSearchSource> = getLocationSearchSources()
        .filter { it !is ConfigurableSource || it.isConfigured }

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> =
        sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources()
        .firstOrNull { it.id == id }
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
        location: Location,
    ): List<PreferencesParametersSource> {
        val preferencesScreenSources = mutableListOf<PreferencesParametersSource>()

        val mainSource = getMainWeatherSource(location.weatherSource)
        if (mainSource is PreferencesParametersSource && mainSource.hasPreferencesScreen(location, emptyList())) {
            preferencesScreenSources.add(mainSource)
        }

        with(location) {
            listOf(
                Pair(currentSource, SecondaryWeatherSourceFeature.FEATURE_CURRENT),
                Pair(airQualitySource, SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY),
                Pair(pollenSource, SecondaryWeatherSourceFeature.FEATURE_POLLEN),
                Pair(minutelySource, SecondaryWeatherSourceFeature.FEATURE_MINUTELY),
                Pair(alertSource, SecondaryWeatherSourceFeature.FEATURE_ALERT),
                Pair(normalsSource, SecondaryWeatherSourceFeature.FEATURE_NORMALS)
            ).forEach {
                val secondarySource = getSecondaryWeatherSource(it.first ?: location.weatherSource)
                if (secondarySource is PreferencesParametersSource &&
                    secondarySource.hasPreferencesScreen(location, listOf(it.second)) &&
                    !preferencesScreenSources.contains(secondarySource)
                ) {
                    preferencesScreenSources.add(secondarySource)
                }
            }
        }

        return preferencesScreenSources
            /*.sortedWith { s1, s2 ->
                // Sort by name because there are now a lot of sources
                Collator.getInstance(
                    SettingsManager.getInstance(context).language.locale
                ).compare(s1.name, s2.name)
            })*/
    }
}
