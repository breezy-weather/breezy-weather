package wangdaye.com.geometricweather.weather.apis;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallHistoryResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;

/**
 * OpenWeather API.
 */

public interface OwmApi {

    @GET("geo/1.0/direct")
    Call<List<OwmLocationResult>> callWeatherLocation(@Query("appid") String apikey,
                                                      @Query("q") String q);

    @GET("geo/1.0/direct")
    Observable<List<OwmLocationResult>> getWeatherLocation(@Query("appid") String apikey,
                                                           @Query("q") String q);

    @GET("geo/1.0/reverse")
    Observable<List<OwmLocationResult>> getWeatherLocationByGeoPosition(@Query("appid") String apikey,
                                                                  @Query("lat") double lat,
                                                                  @Query("lon") double lon);

    // Contains current weather, minute forecast for 1 hour, hourly forecast for 48 hours, daily forecast for 7 days (8 for 3.0) and government weather alerts
    @GET("data/{version}/onecall")
    Observable<OwmOneCallResult> getOneCall(@Path("version") String version,
                                            @Query("appid") String apikey,
                                            @Query("lat") double lat,
                                            @Query("lon") double lon,
                                            @Query("units") String units,
                                            @Query("lang") String lang);

    @GET("data/2.5/air_pollution")
    Observable<OwmAirPollutionResult> getAirPollutionCurrent(@Query("appid") String apikey,
                                                             @Query("lat") double lat,
                                                             @Query("lon") double lon);

    @GET("data/2.5/air_pollution/forecast")
    Observable<OwmAirPollutionResult> getAirPollutionForecast(@Query("appid") String apikey,
                                                              @Query("lat") double lat,
                                                              @Query("lon") double lon);
}
