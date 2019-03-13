package wangdaye.com.geometricweather.location;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.location.BaiduIPLocationResult;

public interface BaiduIPLocationApi {

    @GET("location/ip")
    Observable<BaiduIPLocationResult> getLocation(@Query("ak") String ak,
                                                  @Query("coor") String coor);
}
