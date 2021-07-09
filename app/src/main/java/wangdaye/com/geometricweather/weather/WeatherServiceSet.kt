package wangdaye.com.geometricweather.weather

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.weather.services.*
import javax.inject.Inject

class WeatherServiceSet @Inject constructor(accuWeatherService: AccuWeatherService,
                                            cnWeatherService: CNWeatherService,
                                            caiYunWeatherService: CaiYunWeatherService,
                                            mfWeatherService: MfWeatherService,
                                            owmWeatherService: OwmWeatherService) {
    val all = arrayOf(
            accuWeatherService,
            cnWeatherService,
            caiYunWeatherService,
            mfWeatherService,
            owmWeatherService
    )

    operator fun get(source: WeatherSource?): WeatherService {
        return when (source) {
            WeatherSource.OWM -> all[4]
            WeatherSource.MF -> all[3]
            WeatherSource.CAIYUN -> all[2]
            WeatherSource.CN -> all[1]
            else -> all[0]
        }
    }
}