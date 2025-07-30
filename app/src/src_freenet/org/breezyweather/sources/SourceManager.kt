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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.BreezyWeather
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.android.AndroidLocationService
import org.breezyweather.sources.brightsky.BrightSkyService
import org.breezyweather.sources.climweb.AnamBfService
import org.breezyweather.sources.climweb.AnametService
import org.breezyweather.sources.climweb.DccmsService
import org.breezyweather.sources.climweb.DmnNeService
import org.breezyweather.sources.climweb.DwrGmService
import org.breezyweather.sources.climweb.EthioMetService
import org.breezyweather.sources.climweb.GMetService
import org.breezyweather.sources.climweb.IgebuService
import org.breezyweather.sources.climweb.InmgbService
import org.breezyweather.sources.climweb.MaliMeteoService
import org.breezyweather.sources.climweb.MeteoBeninService
import org.breezyweather.sources.climweb.MeteoTchadService
import org.breezyweather.sources.climweb.MettelsatService
import org.breezyweather.sources.climweb.MsdZwService
import org.breezyweather.sources.climweb.SmaScService
import org.breezyweather.sources.climweb.SmaSuService
import org.breezyweather.sources.climweb.SsmsService
import org.breezyweather.sources.debug.DebugService
import org.breezyweather.sources.fpas.FpasService
import org.breezyweather.sources.gadgetbridge.GadgetbridgeService
import org.breezyweather.sources.naturalearth.NaturalEarthService
import org.breezyweather.sources.nominatim.NominatimService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.pirateweather.PirateWeatherService
import org.breezyweather.sources.recosante.RecosanteService
import javax.inject.Inject

class SourceManager @Inject constructor(
    androidLocationService: AndroidLocationService,
    anamBfService: AnamBfService,
    anametService: AnametService,
    brightSkyService: BrightSkyService,
    dccmsService: DccmsService,
    debugService: DebugService,
    dmnNeService: DmnNeService,
    dwrGmService: DwrGmService,
    ethioMetService: EthioMetService,
    fpasService: FpasService,
    gadgetbridgeService: GadgetbridgeService,
    gMetService: GMetService,
    igebuService: IgebuService,
    inmgbService: InmgbService,
    maliMeteoService: MaliMeteoService,
    meteoBeninService: MeteoBeninService,
    meteoTchadService: MeteoTchadService,
    mettelsatService: MettelsatService,
    msdZwService: MsdZwService,
    naturalEarthService: NaturalEarthService,
    nominatimService: NominatimService,
    openMeteoService: OpenMeteoService,
    pirateWeatherService: PirateWeatherService,
    recosanteService: RecosanteService,
    smaScService: SmaScService,
    smaSuService: SmaSuService,
    ssmsService: SsmsService,
) {
    // TODO: Initialize lazily

    // Location sources
    private val locationSourceList = persistentListOf(
        androidLocationService
    )

    // Reverse geocoding sources
    private val reverseGeocodingSourceList = persistentListOf(
        naturalEarthService,
        nominatimService
    )

    // Worldwide weather sources, excluding national sources with worldwide support
    private val worldwideWeatherSourceList = persistentListOf(
        fpasService,
        openMeteoService,
        pirateWeatherService
    )

    // Region-specific or national weather sources
    private val nationalWeatherSourceList = persistentListOf(
        anamBfService,
        anametService,
        brightSkyService,
        dccmsService,
        dmnNeService,
        dwrGmService,
        ethioMetService,
        gMetService,
        igebuService,
        inmgbService,
        maliMeteoService,
        meteoBeninService,
        meteoTchadService,
        mettelsatService,
        msdZwService,
        smaScService,
        smaSuService,
        ssmsService,
        recosanteService
    )

    // Broadcast sources
    private val broadcastSourceList = persistentListOf(
        gadgetbridgeService
    )

    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: ImmutableList<Source> = buildList {
        addAll(locationSourceList)
        addAll(reverseGeocodingSourceList)
        addAll(worldwideWeatherSourceList)
        if (BreezyWeather.instance.debugMode) {
            add(debugService)
        }
        addAll(
            nationalWeatherSourceList
            // Only one source in the freenet flavor, so no need to do that atm
            /*.sortedWith { ws1, ws2 ->
                // Sort by name because there are now a lot of sources
                Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
            }*/
        )
        addAll(broadcastSourceList)
    }.toImmutableList()

    fun getSource(id: String): Source? = sourceList.firstOrNull { it.id == id }
    fun getHttpSources(): ImmutableList<HttpSource> = sourceList
        .filterIsInstance<HttpSource>()
        .toImmutableList()

    // Location
    fun getLocationSources(): ImmutableList<LocationSource> = sourceList
        .filterIsInstance<LocationSource>()
        .toImmutableList()

    fun getWeatherSources(): ImmutableList<WeatherSource> = sourceList
        .filterIsInstance<WeatherSource>()
        .toImmutableList()

    // Secondary weather
    fun getPollenIndexSource(id: String): PollenIndexSource? = sourceList
        .filterIsInstance<PollenIndexSource>()
        .firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): ImmutableList<LocationSearchSource> = sourceList
        .filterIsInstance<LocationSearchSource>()
        .toImmutableList()

    // Reverse geocoding
    fun getReverseGeocodingSources(): ImmutableList<ReverseGeocodingSource> = sourceList
        .filterIsInstance<ReverseGeocodingSource>()
        .toImmutableList()

    // Broadcast
    fun getBroadcastSources(): ImmutableList<BroadcastSource> = sourceList
        .filterIsInstance<BroadcastSource>()
        .toImmutableList()

    // Configurables sources
    fun getConfigurableSources(): ImmutableList<ConfigurableSource> = sourceList
        .filterIsInstance<ConfigurableSource>()
        .toImmutableList()
}
