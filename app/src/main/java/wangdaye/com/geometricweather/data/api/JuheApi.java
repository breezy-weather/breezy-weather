package wangdaye.com.geometricweather.data.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.data.entity.result.JuheResult;

/**
 * Juhe api.
 * */

public interface JuheApi {

    String BASE_URL = "http://op.juhe.cn/onebox/weather/";
    String APP_KEY ="5bf9785af8c13ea44ab55442d63bc0ad";

    @GET("query")
    Call<JuheResult> getJuheWeather(@Query("cityname") String location,
                                    @Query("key") String key);
}
