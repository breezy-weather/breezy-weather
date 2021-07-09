package wangdaye.com.geometricweather.location.services.ip

import retrofit2.http.GET
import retrofit2.http.Query

interface BaiduIPLocationApi {

    @GET("location/ip")
    suspend fun getLocation(
            @Query("ak") ak: String,
            @Query("coor") coor: String
    ): BaiduIPLocationResult
}