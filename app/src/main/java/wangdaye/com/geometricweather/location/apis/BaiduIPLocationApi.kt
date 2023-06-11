package wangdaye.com.geometricweather.location.apis

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import wangdaye.com.geometricweather.location.json.BaiduIPLocationResult

interface BaiduIPLocationApi {
    @GET("location/ip")
    fun getLocation(
        @Query("ak") ak: String,
        @Query("coor") coor: String
    ): Observable<BaiduIPLocationResult>
}
