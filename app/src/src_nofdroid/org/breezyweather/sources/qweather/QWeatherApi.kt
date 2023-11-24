package org.breezyweather.sources.qweather

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.qweather.json.QWeatherAlertsResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentAQIResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherDailyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherHourlyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherLocationCityResult
import org.breezyweather.sources.qweather.json.QWeatherLocationPOIResult
import org.breezyweather.sources.qweather.json.QWeatherMinutelyPrecipitationResult
import retrofit2.http.GET
import retrofit2.http.Query

interface QWeatherApi {
    @GET("weather/now")
    fun getCurrentWeather(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherCurrentWeatherResult>

    @GET("air/now")
    fun getCurrentAQI(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherCurrentAQIResult>

    @GET("warning/now")
    fun getCurrentAlerts(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherAlertsResult>

    @GET("weather/24h")
    fun getHourlyWeather(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherHourlyWeatherResult>

    @GET("weather/7d")
    fun getDailyWeather(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherDailyWeatherResult>

    @GET("minutely/5m")
    fun getMinutelyPrecipitation(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String,
    ): Observable<QWeatherMinutelyPrecipitationResult>
}

interface QWeatherLocaleApi {
    @GET("city/lookup")
    fun getCitySearch(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("number") number: Int,
        @Query("lang") lang: String,
    ): Observable<QWeatherLocationCityResult>

    @GET("poi/lookup")
    fun getPoiSearch(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("key") key: String,
        @Query("number") number: Int,
        @Query("lang") lang: String,
    ): Observable<QWeatherLocationPOIResult>

}