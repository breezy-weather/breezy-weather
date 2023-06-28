package org.breezyweather.weather

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.weather.accu.AccuWeatherService
import org.breezyweather.weather.china.ChinaWeatherService
import org.breezyweather.weather.metno.MetNoWeatherService
import org.breezyweather.weather.mf.MfWeatherService
import org.breezyweather.weather.openmeteo.OpenMeteoWeatherService
import org.breezyweather.weather.openweather.OpenWeatherWeatherService
import javax.inject.Inject

class WeatherServiceSet @Inject constructor(
    openMeteoWeatherService: OpenMeteoWeatherService,
    accuWeatherService: AccuWeatherService,
    metNoWeatherService: MetNoWeatherService,
    openWeatherWeatherService: OpenWeatherWeatherService,
    mfWeatherService: MfWeatherService,
    chinaWeatherService: ChinaWeatherService
) {
    val all: Array<WeatherService> = arrayOf(
        openMeteoWeatherService,
        accuWeatherService,
        metNoWeatherService,
        openWeatherWeatherService,
        mfWeatherService,
        chinaWeatherService
    )

    operator fun get(source: WeatherSource) = when (source) {
        WeatherSource.OPEN_METEO -> all[0]
        WeatherSource.ACCU -> all[1]
        WeatherSource.METNO -> all[2]
        WeatherSource.OPEN_WEATHER -> all[3]
        WeatherSource.MF -> all[4]
        WeatherSource.CHINA -> all[5]
    }
}
