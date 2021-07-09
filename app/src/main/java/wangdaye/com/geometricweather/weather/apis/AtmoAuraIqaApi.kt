package wangdaye.com.geometricweather.weather.apis

import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraQAResult

/**
 * API Atmo AURA
 * Covers Auvergne-Rhône-Alpes
 */
interface AtmoAuraIqaApi {

    @GET("air2go/full_request")
    suspend fun getQAFull(
            @Query("api_token") api_token: String,
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String
    ): AtmoAuraQAResult
}