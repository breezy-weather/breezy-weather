package org.breezyweather.sources.ipsb

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.ipsb.json.IpSbLocationResult
import retrofit2.http.GET

interface IpSbLocationApi {
    @GET("geoip")
    fun getLocation(): Observable<IpSbLocationResult>
}
