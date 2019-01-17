package wangdaye.com.geometricweather.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.data.entity.result.location.BaiduIPLocationResult;

public interface BaiduLocationApi {

    @GET("location/ip")
    Call<BaiduIPLocationResult> getLocation(@Query("ak") String ak,
                                            @Query("coor") String coor);
}
