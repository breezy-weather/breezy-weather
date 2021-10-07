package wangdaye.com.geometricweather.location.services.ip;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BaiduIPLocationApi {

    @GET("location/ip")
    Observable<BaiduIPLocationResult> getLocation(@Query("ak") String ak,
                                                  @Query("coor") String coor);
}
