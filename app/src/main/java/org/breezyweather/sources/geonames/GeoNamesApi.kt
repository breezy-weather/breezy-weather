package org.breezyweather.sources.geonames

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.geonames.json.GeoNamesSearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoNamesApi {

    @GET("searchJSON")
    fun getLocation(
        @Query("q") query: String,
        @Query("fuzzy") fuzzy: Double,
        @Query("maxRows") maxRows: Int,
        @Query("username") username: String,
        @Query("style") style: String
    ): Observable<GeoNamesSearchResult>
}