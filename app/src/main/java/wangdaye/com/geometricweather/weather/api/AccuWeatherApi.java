package wangdaye.com.geometricweather.weather.api;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuAqiResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuCurrentResult;

/**
 * Accu api.
 */

public interface AccuWeatherApi {

    @GET("locations/v1/cities/translate.json")
    Call<List<AccuLocationResult>> callWeatherLocation(@Query("alias") String alias,
                                                       @Query("apikey") String apikey,
                                                       @Query("q") String q,
                                                       @Query("language") String language);

    @GET("locations/v1/cities/translate.json")
    Observable<List<AccuLocationResult>> getWeatherLocation(@Query("alias") String alias,
                                                            @Query("apikey") String apikey,
                                                            @Query("q") String q,
                                                            @Query("language") String language);

    @GET("locations/v1/cities/geoposition/search.json")
    Observable<AccuLocationResult> getWeatherLocationByGeoPosition(@Query("alias") String alias,
                                                                   @Query("apikey") String apikey,
                                                                   @Query("q") String q,
                                                                   @Query("language") String language);

    @GET("currentconditions/v1/{city_key}.json")
    Observable<List<AccuCurrentResult>> getCurrent(@Path("city_key") String city_key,
                                                   @Query("apikey") String apikey,
                                                   @Query("language") String language,
                                                   @Query("details") boolean details);

    @GET("forecasts/v1/daily/15day/{city_key}.json")
    Observable<AccuDailyResult> getDaily(@Path("city_key") String city_key,
                                         @Query("apikey") String apikey,
                                         @Query("language") String language,
                                         @Query("metric") boolean metric,
                                         @Query("details") boolean details);

    @GET("forecasts/v1/hourly/24hour/{city_key}.json")
    Observable<List<AccuHourlyResult>> getHourly(@Path("city_key") String city_key,
                                                 @Query("apikey") String apikey,
                                                 @Query("language") String language,
                                                 @Query("metric") boolean metric);

    @GET("forecasts/v1/minute/1minute.json")
    Observable<AccuMinuteResult> getMinutely(@Query("apikey") String apikey,
                                             @Query("language") String language,
                                             @Query("details") boolean details,
                                             @Query("q") String q);

    @GET("airquality/v1/observations/{city_key}.json")
    Observable<AccuAqiResult> getAirQuality(@Path("city_key") String city_key,
                                            @Query("apikey") String apikey);

    @GET("alerts/v1/{city_key}.json")
    Observable<List<AccuAlertResult>> getAlert(@Path("city_key") String city_key,
                                               @Query("apikey") String apikey,
                                               @Query("language") String language,
                                               @Query("details") boolean details);
}
