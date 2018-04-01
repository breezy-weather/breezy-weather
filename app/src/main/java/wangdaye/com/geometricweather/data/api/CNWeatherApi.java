package wangdaye.com.geometricweather.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;

/**
 * CN weather api.
 * */

public interface CNWeatherApi {

    @GET("v4/{city_id}.json")
    Call<CNWeatherResult> getWeather(@Path("city_id") String city_id);
}
