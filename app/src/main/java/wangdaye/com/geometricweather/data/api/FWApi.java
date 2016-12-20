package wangdaye.com.geometricweather.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import wangdaye.com.geometricweather.data.entity.result.FWResult;

/**
 * Flyme api.
 * */

public interface FWApi {

    @GET("{city_id}.json")
    Call<FWResult> getFWeather(@Path("city_id") long city_id);
}
