package org.breezyweather.sources

import org.breezyweather.BuildConfig
import org.breezyweather.common.source.ConfigurableSource
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
import org.breezyweather.sources.here.HereService
import org.breezyweather.sources.metno.MetNoService
import org.breezyweather.sources.mf.MfService
import org.breezyweather.sources.msazure.MsAzureWeatherService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.openweather.OpenWeatherService
import org.breezyweather.sources.pirateweather.PirateWeatherService
import javax.inject.Inject

class SourceManager @Inject constructor(
    androidLocationSource: AndroidLocationSource,
    baiduIPService: BaiduIPLocationService,
    openMeteoService: OpenMeteoService,
    accuService: AccuService,
    metNoService: MetNoService,
    openWeatherService: OpenWeatherService,
    pirateWeatherService: PirateWeatherService,
    hereService: HereService,
    msAzureService: MsAzureWeatherService,
    mfService: MfService,
    chinaService: ChinaService,
    atmoAuraService: AtmoAuraService
) {
    // TODO: Initialize lazily
    private val sourceList: List<Source> = listOf(
        // Location sources
        androidLocationSource,
        baiduIPService,

        // Weather sources
        openMeteoService,
        accuService,
        metNoService,
        openWeatherService,
        pirateWeatherService,
        hereService,
        msAzureService,
        mfService,
        chinaService,

        // Secondary weather sources
        atmoAuraService
    )

    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id) ?: getLocationSource(DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getWeatherSources(): List<MainWeatherSource> = sourceList.filterIsInstance<MainWeatherSource>()
    fun getWeatherSource(id: String): MainWeatherSource? = getWeatherSources().firstOrNull { it.id == id }
    fun getWeatherSourceOrDefault(id: String): MainWeatherSource = getWeatherSource(id)
        ?: getWeatherSource(BuildConfig.DEFAULT_WEATHER_SOURCE)!!
    fun getConfiguredWeatherSources(): List<MainWeatherSource> = getWeatherSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Secondary weather
    fun getSecondaryWeatherSources(): List<SecondaryWeatherSource> = sourceList.filterIsInstance<SecondaryWeatherSource>()
    fun getSecondaryWeatherSource(id: String): SecondaryWeatherSource? = getSecondaryWeatherSources().firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources().firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id) ?: getLocationSearchSource(DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getDefaultLocationSearchSource(): LocationSearchSource = getLocationSearchSources().firstOrNull { it.id == DEFAULT_LOCATION_SEARCH_SOURCE }!!

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> = sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources().firstOrNull { it.id == id }


    companion object {
        private const val DEFAULT_LOCATION_SOURCE = "native"
        private const val DEFAULT_LOCATION_SEARCH_SOURCE = "openmeteo"
    }
}
