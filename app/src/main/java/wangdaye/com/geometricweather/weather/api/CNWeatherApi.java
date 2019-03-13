package wangdaye.com.geometricweather.weather.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;

/**
 * CN weather api.
 * */

public interface CNWeatherApi {

    @GET("v4/{city_id}.json")
    Observable<CNWeatherResult> getWeather(@Path("city_id") String city_id);
}
