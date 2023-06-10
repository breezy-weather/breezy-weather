package wangdaye.com.geometricweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult

/**
 * Open-Meteo API
 */
interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality?hourly=pm10,pm2_5")
    fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Observable<OpenMeteoAirQualityResult>
}