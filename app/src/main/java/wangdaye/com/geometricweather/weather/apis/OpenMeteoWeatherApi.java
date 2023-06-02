package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoWeatherResult;

/**
 * Open-Meteo API
 */
public interface OpenMeteoWeatherApi {

    // TODO: Check that I remembered correctly that we need this windspeed_unit
    @GET("v1/forecast?windspeed_unit=ms&timezone=auto&timeformat=unixtime")
    Observable<OpenMeteoWeatherResult> getWeather(@Query("latitude") double latitude,
                                                  @Query("longitude") double longitude,
                                                  @Query("daily") String daily,
                                                  @Query("hourly") String hourly,
                                                  @Query("forecast_days") int forecastDays,
                                                  @Query("past_days") int pastDays,
                                                  @Query("current_weather") boolean currentWeather);
}