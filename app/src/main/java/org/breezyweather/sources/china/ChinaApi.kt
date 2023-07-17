package org.breezyweather.sources.china

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult

interface ChinaApi {
    @GET("weather/all")
    fun getForecastWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("isLocated") isLocated: Boolean,
        @Query("locationKey") locationKey: String,
        @Query("days") days: Int,
        @Query("appKey") appKey: String,
        @Query("sign") sign: String,
        @Query("isGlobal") isGlobal: Boolean,
        @Query("locale") locale: String
    ): Observable<ChinaForecastResult>

    @GET("weather/xm/forecast/minutely")
    fun getMinutelyWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("locale") locale: String,
        @Query("isGlobal") isGlobal: Boolean,
        @Query("appKey") appKey: String,
        @Query("locationKey") locationKey: String,
        @Query("sign") sign: String
    ): Observable<ChinaMinutelyResult>
}
