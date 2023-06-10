package wangdaye.com.geometricweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.nominatim.NominatimLocationResult

/**
 * Nominatim API.
 */
interface NominatimApi {
    @GET("search")
    fun callWeatherLocation(
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("featuretype") featureType: String,
        @Query("addressdetails") addressDetails: Boolean,
        @Query("accept-language") acceptLanguage: String,
        @Query("format") format: String
    ): Call<List<NominatimLocationResult>>

    @GET("search")
    fun getWeatherLocation(
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("featuretype") featureType: String,
        @Query("addressdetails") addressDetails: Boolean,
        @Query("accept-language") acceptLanguage: String,
        @Query("format") format: String
    ): Observable<List<NominatimLocationResult>>

    @GET("search")
    fun getWeatherLocationByGeoPosition(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("featuretype") featureType: String,
        @Query("addressdetails") addressDetails: Boolean,
        @Query("accept-language") acceptLanguage: String,
        @Query("format") format: String
    ): Observable<NominatimLocationResult>
}