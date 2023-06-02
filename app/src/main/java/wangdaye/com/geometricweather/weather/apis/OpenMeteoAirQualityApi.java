package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult;

/**
 * Open-Meteo API
 */
public interface OpenMeteoAirQualityApi {

    @GET("v1/air-quality?hourly=pm10,pm2_5")
    Observable<OpenMeteoAirQualityResult> getAirQuality(@Query("latitude") double latitude,
                                                        @Query("longitude") double longitude);
}