package wangdaye.com.geometricweather.weather.apis;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallHistoryResult;

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

    // Contains current weather, minute forecast for 1 hour, hourly forecast for 48 hours, daily forecast for 7 days and government weather alerts
    @GET("data/2.5/onecall")
    Observable<OwmOneCallResult> getOneCall(@Query("appid") String apikey,
                                            @Query("lat") double lat,
                                            @Query("lon") double lon,
                                            @Query("units") String units,
                                            @Query("lang") String lang);


    @GET("data/2.5/onecall/timemachine")
    Observable<OwmOneCallHistoryResult> getOneCallHistory(@Query("appid") String apikey,
                                                          @Query("lat") double lat,
                                                          @Query("lon") double lon,
                                                          @Query("dt") long dt,
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
