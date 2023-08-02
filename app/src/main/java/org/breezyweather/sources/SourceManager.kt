package org.breezyweather.sources

import org.breezyweather.BuildConfig
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.accu.AccuService
import org.breezyweather.sources.android.AndroidLocationSource
import org.breezyweather.sources.atmoaura.AtmoAuraService
import org.breezyweather.sources.baiduip.BaiduIPLocationService
import org.breezyweather.sources.china.ChinaService
import org.breezyweather.sources.geonames.GeoNamesService
import org.breezyweather.sources.here.HereService
import org.breezyweather.sources.ipsb.IpSbLocationService
import org.breezyweather.sources.metno.MetNoService
import org.breezyweather.sources.mf.MfService
import org.breezyweather.sources.msazure.MsAzureWeatherService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.openweather.OpenWeatherService
import org.breezyweather.sources.pirateweather.PirateWeatherService
import javax.inject.Inject

class SourceManager @Inject constructor(
    accuService: AccuService,
    androidLocationSource: AndroidLocationSource,
    atmoAuraService: AtmoAuraService,
    baiduIPService: BaiduIPLocationService,
    chinaService: ChinaService,
    geoNamesService: GeoNamesService,
    hereService: HereService,
    ipSbService: IpSbLocationService,
    metNoService: MetNoService,
    mfService: MfService,
    msAzureService: MsAzureWeatherService,
    openMeteoService: OpenMeteoService,
    openWeatherService: OpenWeatherService,
    pirateWeatherService: PirateWeatherService
) {
    // TODO: Initialize lazily
    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: List<Source> = listOf(
        // Location sources
        androidLocationSource,
        ipSbService,
        baiduIPService,

        // Location search sources
        geoNamesService,

        // Weather sources
        openMeteoService,
        accuService,
        metNoService,
        openWeatherService,
        pirateWeatherService,
        msAzureService,
        hereService,
        mfService,
        chinaService,

        // Secondary weather sources
        atmoAuraService
    )

    fun getSource(id: String): Source? = sourceList.firstOrNull { it.id == id }
    fun getHttpSources(): List<HttpSource> = sourceList.filterIsInstance<HttpSource>()


    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id)
        ?: getLocationSource(BuildConfig.DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getMainWeatherSources(): List<MainWeatherSource> = sourceList.filterIsInstance<MainWeatherSource>()
    fun getMainWeatherSource(id: String): MainWeatherSource? = getMainWeatherSources().firstOrNull { it.id == id }
    fun getMainWeatherSourceOrDefault(id: String): MainWeatherSource = getMainWeatherSource(id)
        ?: getMainWeatherSource(BuildConfig.DEFAULT_WEATHER_SOURCE)!!
    fun getConfiguredMainWeatherSources(): List<MainWeatherSource> = getMainWeatherSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Secondary weather
    fun getSecondaryWeatherSources(): List<SecondaryWeatherSource> = sourceList.filterIsInstance<SecondaryWeatherSource>()
    fun getSecondaryWeatherSource(id: String): SecondaryWeatherSource? = getSecondaryWeatherSources().firstOrNull { it.id == id }

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

    // Configurables sources
    fun getConfigurableSources(): List<ConfigurableSource> = sourceList.filterIsInstance<ConfigurableSource>()

}
