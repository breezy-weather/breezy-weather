package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResults;

/**
 * Open-Meteo API
 */
public interface OpenMeteoGeocodingApi {

    @GET("v1/search?format=json")
    Call<OpenMeteoLocationResults> callWeatherLocation(@Query("name") String name,
                                                       @Query("count") int count,
                                                       @Query("language") String language);

    @GET("v1/search?format=json")
    Observable<OpenMeteoLocationResults> getWeatherLocation(@Query("name") String name,
                                                            @Query("count") int count,
                                                            @Query("language") String language);

}