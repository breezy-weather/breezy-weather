package wangdaye.com.geometricweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult
import wangdaye.com.geometricweather.weather.json.metno.MetNoSunsetResult

/**
 * MET Norway Weather API.
 */
interface MetNoApi {
    @GET("locationforecast/2.0/complete.json")
    fun getLocationForecast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoLocationForecastResult>

    @GET("sunrise/2.0/.json")
    fun getSunset(
        @Header("User-Agent") userAgent: String,
        @Query("date") date: String,
        @Query("days") days: Int,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("offset") offset: String
    ): Observable<MetNoSunsetResult>

    // Only available in Nordic area
    /*@GET("nowcast/2.0/complete.json")
    fun getMinutely(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoLocationForecastResult>*/

    /*@GET("airqualityforecast/0.1/")
    fun getAirQuality(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoAqiResult>*/

    /*@GET("metalerts/1.1/")
    fun getAlerts(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<List<MetNoAlertResult>>*/
}