package org.breezyweather.sources.metno

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoSunResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject

class MetNoService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource {

    override val id = "metno"
    override val name = "MET Norway"
    override val privacyPolicyUrl = "https://www.met.no/en/About-us/privacy"

    override val color = -0xdba791
    override val weatherAttribution = "MET Norway, Norwegian license for public data (NLOD) / Creative Commons 4.0 BY International"

    private val mApi by lazy {
        client
            .baseUrl(METNO_BASE_URL)
            .build()
            .create(MetNoApi::class.java)
    }

    override fun requestWeather(
        context: Context,
        location: Location
    ): Observable<WeatherResultWrapper> {
        val forecast = mApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble()
        )

        val formattedDate = Date().getFormattedDate(location.timeZone, "yyyy-MM-dd")
        val sun = mApi.getSun(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            formattedDate
        )
        val moon = mApi.getMoon(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            formattedDate
        )

        // Nowcast only for Norway, Sweden, Finland and Denmark
        // Covered area is slightly larger as per https://api.met.no/doc/nowcast/datamodel
        // but safer to limit to guaranteed countries
        val nowcast = if (!location.countryCode.isNullOrEmpty()
            && location.countryCode in arrayOf("NO", "SE", "FI", "DK")) {
            mApi.getNowcast(
                userAgent,
                location.latitude.toDouble(),
                location.longitude.toDouble()
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MetNoNowcastResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MetNoNowcastResult())
            }
        }

        // Air quality only for Norway
        val airQuality = if (!location.countryCode.isNullOrEmpty()
            && location.countryCode == "NO") {
            mApi.getAirQuality(
                userAgent,
                location.latitude.toDouble(),
                location.longitude.toDouble()
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MetNoAirQualityResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MetNoAirQualityResult())
            }
        }

        return Observable.zip(forecast, sun, moon, nowcast, airQuality) {
                metNoForecast: MetNoForecastResult,
                metNoSun: MetNoSunResult,
                metNoMoon: MetNoMoonResult,
                metNoNowcast: MetNoNowcastResult,
                metNoAirQuality: MetNoAirQualityResult
            ->
            convert(
                context,
                location,
                metNoForecast,
                metNoSun,
                metNoMoon,
                metNoNowcast,
                metNoAirQuality
            )
        }
    }

    companion object {
        private const val METNO_BASE_URL = "https://api.met.no/weatherapi/"
        private val userAgent = "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}