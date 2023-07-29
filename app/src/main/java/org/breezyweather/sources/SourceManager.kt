package org.breezyweather.sources

import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.accu.AccuService
import org.breezyweather.sources.android.AndroidLocationSource
import org.breezyweather.sources.baiduip.BaiduIPLocationService
import org.breezyweather.sources.china.ChinaService
import org.breezyweather.sources.here.HereService
import org.breezyweather.sources.metno.MetNoService
import org.breezyweather.sources.mf.MfService
import org.breezyweather.sources.noreversegeocoding.NoReverseGeocodingService
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
    mfService: MfService,
    chinaService: ChinaService,
    noReverseGeocodingService: NoReverseGeocodingService
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
        mfService,
        chinaService,

        // Reverse geocoding
        noReverseGeocodingService
    )

    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id) ?: getLocationSource(DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getWeatherSources(): List<WeatherSource> = sourceList.filterIsInstance<WeatherSource>()
    fun getWeatherSource(id: String): WeatherSource? = getWeatherSources().firstOrNull { it.id == id }
    fun getWeatherSourceOrDefault(id: String): WeatherSource = getWeatherSource(id) ?: getWeatherSource(DEFAULT_WEATHER_SOURCE)!!
    fun getConfiguredWeatherSources(): List<WeatherSource> = getWeatherSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources().firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id) ?: getLocationSearchSource(DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getDefaultLocationSearchSource(): LocationSearchSource = getLocationSearchSources().firstOrNull { it.id == DEFAULT_LOCATION_SEARCH_SOURCE }!!

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> = sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources().firstOrNull { it.id == id }
    fun getReverseGeocodingSourceOrDefault(id: String): ReverseGeocodingSource = getReverseGeocodingSource(id) ?: getReverseGeocodingSource(DEFAULT_REVERSE_GEOCODING_SOURCE)!!


    companion object {
        // TODO: At least this one should be configurable, F-Droid probably wants "openmeteo"
        const val DEFAULT_WEATHER_SOURCE = "accu"
        private const val DEFAULT_LOCATION_SOURCE = "native"
        private const val DEFAULT_LOCATION_SEARCH_SOURCE = "openmeteo"
        private const val DEFAULT_REVERSE_GEOCODING_SOURCE = "noreversegeocoding"
    }
}
