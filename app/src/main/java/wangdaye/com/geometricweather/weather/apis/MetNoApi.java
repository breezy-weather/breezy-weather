package wangdaye.com.geometricweather.weather.apis;

import java.util.Date;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.metno.MetNoLocationForecastResult;
import wangdaye.com.geometricweather.weather.json.metno.MetNoSunsetResult;

/**
 * MET Weather API.
 */

public interface MetNoApi {

    @GET("locationforecast/2.0/compact.json")
    Observable<MetNoLocationForecastResult> getLocationForecast(@Header("User-Agent") String userAgent,
                                                                @Query("lat") Float lat,
                                                                @Query("lon") Float lon);

    @GET("sunrise/2.0/.json")
    Observable<MetNoSunsetResult> getSunset(@Header("User-Agent") String userAgent,
                                            @Query("date") String date,
                                            @Query("days") int days,
                                            @Query("lat") Float lat,
                                            @Query("lon") Float lon,
                                            @Query("offset") String offset);

    // Only available in Nordic area
    /*@GET("nowcast/2.0/complete.json")
    Observable<MetNoLocationForecastResult> getMinutely(@Header("User-Agent") String userAgent,
                                                        @Query("lat") Float lat,
                                                        @Query("lon") Float lon);*/

    /*@GET("airqualityforecast/0.1/")
    Observable<MetNoAqiResult> getAirQuality(@Header("User-Agent") String userAgent,
                                             @Query("lat") Float lat,
                                             @Query("lon") Float lon);*/

    /*@GET("metalerts/1.1/")
    Observable<List<MetNoAlertResult>> getAlerts(@Header("User-Agent") String userAgent,
                                                 @Query("lat") Float lat,
                                                 @Query("lon") Float lon);*/
}