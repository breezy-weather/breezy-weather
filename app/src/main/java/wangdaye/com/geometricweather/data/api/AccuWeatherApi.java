package wangdaye.com.geometricweather.data.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuAlertResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuAqiResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuLocationResult;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;

/**
 * Accu api.
 * */

public interface AccuWeatherApi {

    @GET("locations/v1/cities/search.json")
    Call<List<AccuLocationResult>> getWeatherLocation(@Query("alias") String alias,
                                                      @Query("apikey") String apikey,
                                                      @Query("q") String q,
                                                      @Query("language") String language);

    @GET("locations/v1/cities/geoposition/search.json")
    Call<AccuLocationResult> getWeatherLocationByGeoPosition(@Query("alias") String alias,
                                                             @Query("apikey") String apikey,
                                                             @Query("q") String q,
                                                             @Query("language") String language);

    @GET("currentconditions/v1/{city_key}.json")
    Call<List<AccuRealtimeResult>> getRealtime(@Path("city_key") String city_key,
                                               @Query("apikey") String apikey,
                                               @Query("language") String language,
                                               @Query("details") boolean details);

    @GET("forecasts/v1/daily/15day/{city_key}.json")
    Call<AccuDailyResult> getDaily(@Path("city_key") String city_key,
                                   @Query("apikey") String apikey,
                                   @Query("language") String language,
                                   @Query("metric") boolean metric,
                                   @Query("details") boolean details);

    @GET("forecasts/v1/hourly/24hour/{city_key}.json")
    Call<List<AccuHourlyResult>> getHourly(@Path("city_key") String city_key,
                                           @Query("apikey") String apikey,
                                           @Query("language") String language,
                                           @Query("metric") boolean metric);

    @GET("alerts/v1/{city_key}.json")
    Call<List<AccuAlertResult>> getAlert(@Path("city_key") String city_key,
                                         @Query("apikey") String apikey,
                                         @Query("language") String language,
                                         @Query("details") boolean details);

    @GET("airquality/v1/observations/{city_key}.json")
    Call<AccuAqiResult> getAqi(@Path("city_key") String city_key,
                               @Query("apikey") String apikey);
}
