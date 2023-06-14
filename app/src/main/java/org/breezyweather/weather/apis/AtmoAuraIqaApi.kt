package org.breezyweather.weather.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import org.breezyweather.weather.json.atmoaura.AtmoAuraPointResult

/**
 * API Atmo AURA
 * Covers Auvergne-Rhône-Alpes
 */
interface AtmoAuraIqaApi {
    @GET("air2go/v3/point?with_list=true")
    fun getPointDetails(
        @Query("api_token") apiToken: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("datetime_echeance") datetimeEcheance: String
    ): Observable<AtmoAuraPointResult>
}