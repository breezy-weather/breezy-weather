package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult

interface CaiYunApi {

    @GET("wtr-v3/weather/all")
    suspend fun getMainlyWeather(
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String,
            @Query("isLocated") isLocated: Boolean,
            @Query("locationKey") locationKey: String,
            @Query("days") days: Int,
            @Query("appKey") appKey: String,
            @Query("sign") sign: String,
            @Query("romVersion") romVersion: String,
            @Query("appVersion") appVersion: String,
            @Query("alpha") alpha: Boolean,
            @Query("isGlobal") isGlobal: Boolean,
            @Query("device") device: String,
            @Query("modDevice") modDevice: String,
            @Query("locale") locale: String
    ): CaiYunMainlyResult

    @GET("wtr-v3/weather/xm/forecast/minutely")
    suspend fun getForecastWeather(
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String,
            @Query("locale") locale: String,
            @Query("isGlobal") isGlobal: Boolean,
            @Query("appKey") appKey: String,
            @Query("locationKey") locationKey: String,
            @Query("sign") sign: String
    ): CaiYunForecastResult
}