package org.breezyweather.sources.veduris

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.veduris.json.VedurIsAlertRegionsResult
import org.breezyweather.sources.veduris.json.VedurIsAlertResult
import org.breezyweather.sources.veduris.json.VedurIsResult
import org.breezyweather.sources.veduris.json.VedurIsStationResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VedurIsApi {
    @GET("_next/data/{page_id}/en/vedur/spar/{id}.json")
    fun getForecast(
        @Path("page_id") pageId: String,
        @Path("id") id: String,
    ): Observable<VedurIsResult>

    @GET("_next/data/{page_id}/en/vedur/athuganir/{id}.json")
    fun getCurrent(
        @Path("page_id") pageId: String,
        @Path("id") id: String,
    ): Observable<VedurIsResult>

    @GET("api/map/forecast/timeline/")
    fun getStations(
        @Query("x1") x1: Double,
        @Query("x2") x2: Double,
        @Query("y1") y1: Double,
        @Query("y2") y2: Double,
    ): Observable<VedurIsStationResult>

    @GET("api/alerts/")
    fun getAlerts(
        @Query("lang") lang: String = "en",
    ): Observable<VedurIsAlertResult>

    // This endpoint returns a GeoJson object.
    // Instead of reinventing the wheel,
    // we will dump the entire JSON property as a String,
    // and reconstruct the GeoJson object using GeoJsonParser.
    @GET("api/alerts/alertRegions/")
    fun getAlertRegions(
        @Query("lang") lang: String = "en",
    ): Observable<VedurIsAlertRegionsResult>
}
